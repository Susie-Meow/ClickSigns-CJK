package de.clickism.clicksigns.ui.components.sign;

import de.clickism.clicksigns.sign.color.ColorResolver;
import de.clickism.clicksigns.sign.element.SignElement;
import de.clickism.clicksigns.sign.element.TextElement;
import de.clickism.clicksigns.ui.UiUtil;
import de.clickism.clickui.UiColor;
import de.clickism.clickui.elements.input.TextField;
import de.clickism.clickui.event.events.KeyEvent;
import de.clickism.clickui.layout.Point;
import de.clickism.clickui.layout.Size;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.UiScreenHandler;
import de.clickism.clickui.util.Util;
import de.clickism.clickui.util.versioning.KeyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import static de.clickism.clicksigns.render.element.TextRenderer.TEXT_RENDER_SCALE;
import static de.clickism.clicksigns.ui.UiConstants.UI_SCALE;
import static de.clickism.clicksigns.util.ComponentUtil.t;
import static de.clickism.clicksigns.util.Constants.BLOCK_PIXELS;

/**
 * A specialized TextField for editing the text of a SignElement.
 * <p>
 * Handles rendering of the text with scaling and background color, and custom styling.
 */
public class SignTextField extends TextField implements ElementProvider {
    /**
     * Blink period of the self-drawn caret, in milliseconds.
     */
    private static final int CARET_BLINK_INTERVAL = 500;
    /**
     * How long the caret stays solid after the last edit, in milliseconds.
     * <p>
     * Without this the caret can blink out exactly while the user is typing,
     * which reads as "the caret disappears / lags behind input".
     */
    private static final int CARET_SOLID_AFTER_EDIT = 450;

    private TextElement element;
    private ColorResolver colorResolver;

    /** Timestamp (ms) of the last edit, used to keep the caret solid while typing. */
    private long lastEditTime = Long.MIN_VALUE;

    /** Cached result of {@link #elementToShow()}, rebuilt whenever the text or element changes. */
    private @Nullable TextElement showCache = null;
    /** The text {@link #showCache} was built from, used as the cache key. */
    private @Nullable String showCacheText = null;

    /**
     * Hidden native {@link EditBox} used purely as an IME (Input Method Editor) sink.
     * <p>
     * clickui's custom text field is not a native Minecraft text-input widget, so on platforms
     * such as macOS the OS IME has no input context and silently falls back to inserting raw
     * Latin characters (the "auto switch to English input method" symptom). By focusing a native
     * {@link EditBox} — which Minecraft's IME system recognises — composed text (e.g. Chinese)
     * is delivered through {@code charTyped} and mirrored back into this field. It is never
     * rendered; only its position is used so the IME candidate window appears above the field.
     * Shared across all SignTextField instances because only one field is focused at a time.
     */
    private static @Nullable EditBox imeBox = null;
    private static @Nullable SignTextField activeField = null;
    private static boolean imeSync = false;
    /** Whether THIS field currently has the hidden IME box attached (per-instance). */
    private boolean imeActive = false;
    /** True while the IME responder is pushing composed text into this field. */
    private boolean imeCommitting = false;

    /**
     * Creates a new SignTextField for the given TextElement and ColorResolver.
     *
     * @param element       The TextElement to display and edit in this text field.
     * @param colorResolver The ColorResolver to use for resolving colors for the text and background.
     */
    public SignTextField(TextElement element, ColorResolver colorResolver) {
        this.element = element;
        this.colorResolver = colorResolver;
        this.scrolling(false);
        this.placeholder(t("clicksigns.textPlaceholder").getString());
        this.value(element.text());
        // Set up style
        this.overrideStyle(style()
            .backgroundColor(null));
        // Recalculate width whenever the value changes
        this.onValueChanged(value -> {
            this.invalidateLayout();
            this.lastEditTime = System.currentTimeMillis();
            // Keep the hidden IME box in sync, so that its own caret index (which we
            // mirror back to position the visible caret) stays valid after edits that
            // originate from this field - e.g. inserting a line break with Enter.
            pushToIme(value);
        });
        // A mouse click moves the caret. Registered after the field's own click handler
        // (which runs first and updates cursorPos), so we can push the clicked position
        // into the IME box; otherwise the next keystroke would land at the old index.
        this.onClick(event -> pushCaretToIme());
        // Set up padding
        this.padding(0);
        this.multiLine(true);
    }

    /**
     * Attaches a hidden native {@link EditBox} as the IME input target for this field.
     * <p>
     * Focusing the native widget makes Minecraft's IME system (Windows/macOS/Linux) recognise an
     * active text input, so composed characters (Chinese, Japanese, Korean, …) are delivered and
     * mirrored back into this custom field. The box is positioned over this field only so the IME
     * candidate window shows above the correct location; it is never drawn.
     */
    private void activateIme() {
        var screen = UiScreenHandler.current();
        if (screen == null) return;
        var bounds = this.bounds();
        var font = Minecraft.getInstance().font;
        var box = new EditBox(
            font,
            bounds.x(),
            bounds.y(),
            Math.max(1, bounds.width()),
            Math.max(1, bounds.height()),
            Component.literal("")
        );
        box.setMaxLength(Integer.MAX_VALUE);
        // Initialise with the current text. setValue keeps newlines that insertText would strip,
        // which matters for multi-line signs.
        box.setValue(this.value());
        // Adopt the caret this field is currently showing, otherwise the first mirrored
        // keystroke would be inserted at the wrong index.
        box.setCursorPosition(this.cursorPos);
        box.setHighlightPos(this.highlightPos);
        box.setResponder(this::mirrorFromIme);
        imeBox = box;
        activeField = this;
        screen.setFocused(box);
        // Show the caret immediately when the field gains focus.
        this.lastEditTime = System.currentTimeMillis();
    }

    /**
     * Mirrors text coming from the hidden native {@link EditBox} into this field.
     * <p>
     * Only the changed region is replaced (instead of swapping the whole value), so the
     * caret keeps a meaningful position and line breaks survive. Afterwards the caret is
     * taken from the native box, which is what the OS/IME actually moves when the user
     * types, deletes or presses the arrow keys — including right after an IME candidate
     * has been committed.
     */
    private void mirrorFromIme(String text) {
        if (activeField != this || imeSync) return;
        imeSync = true;
        imeCommitting = true;
        try {
            String old = this.value();
            if (!old.equals(text)) {
                // Push the text through insertText() instead of value().
                // value() is a plain setter that does NOT notify onValueChanged listeners,
                // and the editor relies on exactly that notification to write the text back
                // into the road sign (see SignView#createViewFor).
                int start = commonPrefix(old, text);
                int oldEnd = commonSuffixEnd(old, text, start);
                int newEnd = text.length() - (old.length() - oldEnd);
                this.cursorPos = start;
                this.highlightPos = oldEnd;
                this.insertText(text.substring(start, Math.max(start, newEnd)));
            }
            // Adopt the caret position of the native box (clamped to this field's length,
            // which can be longer because the box drops some characters).
            var box = imeBox;
            int cursor = box != null
                ? Mth.clamp(box.getCursorPosition(), 0, this.value().length())
                : this.value().length();
            this.cursorPos = cursor;
            this.highlightPos = cursor;
            this.handleCursorMove();
        } finally {
            imeCommitting = false;
            imeSync = false;
        }
        this.lastEditTime = System.currentTimeMillis();
    }

    /**
     * Adopts the caret position of the hidden IME box.
     * <p>
     * Arrow keys, Home/End and mouse-free navigation move the native caret without changing
     * the text, so the box's responder is never invoked for them. Polling once per frame
     * keeps the drawn caret exactly where the native one is.
     */
    private void syncCaretFromIme() {
        var box = imeBox;
        if (box == null || activeField != this || imeSync) return;
        int cursor = Mth.clamp(box.getCursorPosition(), 0, this.value().length());
        if (cursor == this.cursorPos) return;
        this.cursorPos = cursor;
        this.highlightPos = cursor;
        this.handleCursorMove();
        this.lastEditTime = System.currentTimeMillis();
    }

    /**
     * Pushes this field's value (and caret) into the hidden IME box.
     * <p>
     * Used for edits that originate from this field, i.e. inserting a line break with
     * Enter. The sync flag suppresses the responder so this doesn't bounce back.
     */
    private void pushToIme(String value) {
        var box = imeBox;
        if (box == null || activeField != this || imeSync) return;
        imeSync = true;
        try {
            if (!value.equals(box.getValue())) {
                box.setValue(value);
            }
            pushCaretToIme();
        } finally {
            imeSync = false;
        }
    }

    /**
     * Copies this field's caret/selection into the hidden IME box, so the next mirrored
     * keystroke is inserted at the position the user sees.
     */
    private void pushCaretToIme() {
        var box = imeBox;
        if (box == null || activeField != this) return;
        int length = box.getValue().length();
        box.setCursorPosition(Math.min(this.cursorPos, length));
        box.setHighlightPos(Math.min(this.highlightPos, length));
    }

    /**
     * Returns the length of the common prefix of the two strings.
     */
    private static int commonPrefix(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    /**
     * Returns the index in {@code a} where the common suffix (shared with {@code b}) starts,
     * never going below {@code min}.
     */
    private static int commonSuffixEnd(String a, String b, int min) {
        int i = a.length();
        int j = b.length();
        while (i > min && j > min && a.charAt(i - 1) == b.charAt(j - 1)) {
            i--;
            j--;
        }
        return Math.max(i, min);
    }

    /**
     * Detaches the native IME input box when this field loses focus.
     */
    private void deactivateIme() {
        if (activeField != this) return;
        var screen = UiScreenHandler.current();
        if (screen != null) {
            screen.setFocused(null);
        }
        imeBox = null;
        activeField = null;
    }

    /**
     * While the hidden native {@link EditBox} owns text input (IME active), it mirrors composed
     * text into this field through its responder. We therefore skip duplicate insertion here for
     * characters and backspace deletion, but still allow newline insertion so multi-line signs
     * keep working while the IME box is attached.
     */
    @Override
    protected void insertText(String string) {
        // While the IME box owns input, composed characters already arrive through the
        // responder above; ignore duplicate insertions coming from clickui's own input
        // path. Always let the responder's own call through (imeCommitting), and always
        // allow newlines so multi-line signs keep working while the IME box is attached.
        if (!imeCommitting && activeField == this && imeBox != null && !string.equals("\n")) {
            return;
        }
        super.insertText(string);
    }

    /**
     * Updates the text element this field renders from.
     * <p>
     * This is called on every rebuild, not just once, because rendering properties such as the
     * font size ({@code scale}), style and alignment live in the element rather than in the field.
     * Only those properties trigger a cache clear and a layout pass - the text itself is owned by
     * the field and handled by {@link #elementToShow()}.
     *
     * @param element the latest version of the text element
     * @return this field, for chaining
     */
    public SignTextField textElement(TextElement element) {
        boolean visualChange = this.element == null
            || this.element.scale() != element.scale()
            || !this.element.style().equals(element.style())
            || this.element.alignment() != element.alignment()
            || this.element.x() != element.x()
            || this.element.y() != element.y();
        this.element = element;
        if (visualChange) {
            this.showCache = null;
            this.showCacheText = null;
            this.invalidateLayout();
        }
        return this;
    }

    public SignTextField colorResolver(ColorResolver colorResolver) {
        this.colorResolver = colorResolver;
        return this;
    }

    @Override
    public Size intrinsicSize() {
        return new Size(
            Mth.floor(currentWidth()),
            Mth.floor(currentHeight())
        );
    }

    /**
     * Calculates the current width of the text field based on the text to show and the render scale.
     *
     * @return The calculated width of the text field in pixels.
     */
    protected float currentWidth() {
        float width = elementToLayout().width();
        return width * UI_SCALE;
    }

    /**
     * Calculates the current height of the text field based on the text to show and the render scale.
     *
     * @return The calculated height of the text field in pixels.
     */
    protected float currentHeight() {
        var height = elementToLayout().height();
        return height * UI_SCALE;
    }

    /**
     * Returns the text element that should be used for layout calculations.
     * <p>
     * Identical to {@link #elementToShow()} - both wrap {@link #textToShow()} into the current
     * element - so it simply delegates to the cached version instead of allocating again.
     *
     * @return the text element to use for layout
     */
    protected TextElement elementToLayout() {
        return elementToShow();
    }

    /**
     * Returns the text element that will be rendered in the text field.
     * <p>
     * This is called several times per frame (rendering, cursor maths, layout), and building
     * it splits the text and re-measures glyphs, so the result is cached until either the
     * shown text or the underlying element changes.
     *
     * @return the text element to render
     */
    protected TextElement elementToShow() {
        var text = textToShow();
        if (showCache == null || !text.equals(showCacheText)) {
            showCacheText = text;
            showCache = element.withText(text);
        }
        return showCache;
    }

    @Override
    public SignElement element() {
        return element.withText(textToShow());
    }

    /**
     * Moves the cursor vertically by the specified delta (number of lines).
     *
     * @param delta The number of lines to move the cursor. Positive values move down, negative values move up.
     */
    protected void moveCursorVertically(int delta) {
        var linePos = linePosOf(cursorPos);
        var lineText = elementToShow().lines();
        int newLine = Mth.clamp(linePos.line + delta, 0, lineText.size() - 1);
        // Calculate new position in the line visually, based on the x position of the cursor in the current line
        int cursorX = formattedWidth(lineText.get(linePos.line).substring(0, linePos.pos))
                      + element.lineXOffset(lineText.get(linePos.line));
        int newPos = 0;
        for (int i = 0; i < lineText.get(newLine).length(); i++) {
            int charWidth = formattedWidth(lineText.get(newLine).substring(i, i + 1));
            int charX = formattedWidth(lineText.get(newLine).substring(0, i))
                        + element.lineXOffset(lineText.get(newLine));
            if (charX + charWidth / 2 >= cursorX) {
                newPos = i;
                break;
            } else {
                newPos = lineText.get(newLine).length();
            }
        }
        cursorPos = new LinePos(newLine, newPos).toCursorPos();
        if (!KeyUtil.hasShiftDown()) {
            highlightPos = cursorPos;
        }
    }

    @Override
    protected boolean handleKeyPress(KeyEvent event) {
        int code = event.code();
        if (code == KeyUtil.KEY_ENTER || code == KeyUtil.KEY_KP_ENTER) {
            // Insert newline
            insertText("\n");
            return true;
        }
        if (code == KeyUtil.KEY_UP) {
            // Move cursor up a line
            moveCursorVertically(-1);
            pushCaretToIme();
            return true;
        }
        if (code == KeyUtil.KEY_DOWN) {
            // Move cursor down a line
            moveCursorVertically(1);
            pushCaretToIme();
            return true;
        }
        return super.handleKeyPress(event);
    }

    @Override
    protected int textColor(boolean placeholder) {
        var color = colorResolver.resolve(element.style().color());
        if (placeholder) {
            color = UiColor.rgba(color).multiplyAlpha(0.5f).color();
        }
        return color;
    }

    @Override
    protected float textHeight() {
        return element.height() * UI_SCALE;
    }

    @Override
    protected Point textPosition() {
        var bounds = bounds();
        var x = bounds.x();
        var y = bounds.y();
        // Apply text offset
        var textOffset = element.textOffset();
        float offsetX = textOffset.x;
        // Flip y to convert to UI space
        float offsetY = element.totalSize().height() - textOffset.y - element.unpaddedSize().height();
        // Add text offset
        x += (int) offsetX;
        y += (int) offsetY;
        return new Point(x, y);
    }

    @Override
    protected int cursorPosAt(int mouseX, int mouseY) {
        var scale = renderScale();
        var bounds = bounds();
        // Undo the render transform around the field's top-left corner.
        float localMouseX = bounds.x() + (mouseX - bounds.x()) / scale;
        float localMouseY = bounds.y() + (mouseY - bounds.y()) / scale;
        var textPos = textPosition();
        int x = Mth.ceil(localMouseX - textPos.x());
        int y = Mth.ceil(localMouseY - textPos.y());
        // Calculate line based on y position
        var lines = elementToShow().lines();
        int lineHeight = Util.font().lineHeight + element.style().lineGap();
        // Keep index within bounds
        int lineIndex = Mth.clamp(y / lineHeight, 0, lines.size() - 1);
        // Calculate character index based on x position
        var lineText = lines.get(lineIndex);
        x -= element.lineXOffset(lineText); // Adjust x based on line offset
        int charIndex = Util.font()
            .getSplitter()
            //? if >=26.1 {
            .headByWidth(FormattedText.of(lineText), x, element.style().asComponentStyle())
            .getString()
            .length();
            //?} else
            //.formattedHeadByWidth(lineText, x, element.style().asComponentStyle()).length();

        // Calculate cursor position
        int lineCursorPos = 0;
        for (int i = 0; i < lineIndex; i++) {
            lineCursorPos += lines.get(i).length() + 1; // +1 for newline
        }
        return lineCursorPos + charIndex;
    }

    @Override
    public void render(RenderContext context) {
        // Attach/detach the hidden native IME input box to follow this field's focus.
        // clickui has no public focus-enter/exit events, so we poll focused() each frame
        // and only act on the transition (so we don't rebuild the box every frame).
        boolean focused = this.focused();
        if (focused && !imeActive) {
            imeActive = true;
            activateIme();
        } else if (!focused && imeActive) {
            imeActive = false;
            deactivateIme();
        }
        // The native box owns the caret for arrow keys / Home / End, which move it without
        // changing the text, so its responder never fires for them. Pull the caret every
        // frame instead, so the drawn caret follows those keys too.
        syncCaretFromIme();
        // Render the text field
        var bounds = bounds();
        // Apply render scale
        var renderScale = renderScale();
        context.graphics().withScaleAbout(bounds.x(), bounds.y(), renderScale, renderScale, () -> {
            // Render background
            renderBackground(context);
            // Render outline
            renderOutline(context);
            // Render text and highlight
            super.render(context);
            // Draw the caret on top of the text, so it is never covered by it.
            drawCaret(context);
        });
    }

    /**
     * Draws the text caret (insertion cursor).
     * <p>
     * The caret is <strong>self-drawn</strong>: clickui has no native text widget and no CSS,
     * the caret is a filled rectangle painted through {@code UiGraphics}. clickui's own
     * implementation ({@code AbstractField#renderTextField} -> {@code Field#renderCursor}) is
     * suppressed because it is gated behind a private {@code isCursorVisible()} that we cannot
     * influence from a subclass, and it is only a single pixel wide in the text colour.
     * <p>
     * Conditions handled here:
     * <ul>
     *     <li>Visible whenever the field has focus.</li>
     *     <li>Stays solid for {@link #CARET_SOLID_AFTER_EDIT} ms after every edit, so typing
     *         never has to wait for the blink phase (blinking is render-only, it does not
     *         touch input).</li>
     *     <li>Positioned from {@code cursorPos}, which is mirrored from the native IME box,
     *         so it is exact for mixed CJK/Latin text and after an IME commit.</li>
     *     <li>Drawn as a bright core with a 1px contrasting outline, so it stays readable on
     *         both dark and light sign backgrounds.</li>
     * </ul>
     */
    private void drawCaret(RenderContext context) {
        if (!focused() || disabled()) return;
        long time = System.currentTimeMillis();
        boolean recentlyEdited = time - lastEditTime < CARET_SOLID_AFTER_EDIT;
        if (!recentlyEdited && time / CARET_BLINK_INTERVAL % 2 != 0) {
            return;
        }
        var element = elementToShow();
        var lines = element.lines();
        var font = Util.font();
        var textPos = textPosition();
        int lineIndex = 0;
        int beforeWidth = 0;
        int lineOffset = 0;
        if (!lines.isEmpty()) {
            var linePos = linePosOf(cursorPos);
            lineIndex = Mth.clamp(linePos.line, 0, lines.size() - 1);
            var line = lines.get(lineIndex);
            var before = line.substring(0, Math.min(linePos.pos, line.length()));
            beforeWidth = formattedWidth(before);
            lineOffset = element.lineXOffset(line);
        }
        int x = textPos.x() + beforeWidth + lineOffset;
        int y = textPos.y() + lineIndex * (font.lineHeight + this.element.style().lineGap());
        int height = font.lineHeight + 1;
        int color = caretColor();
        int outline = isDark(color) ? 0xFFFFFFFF : 0xFF000000;
        var graphics = context.graphics();
        // Contrasting outline first, then the core on top: readable on any background.
        graphics.fill(x - 1, y - 1, x + 2, y + height + 1, outline);
        graphics.fill(x, y, x + 1, y + height, color);
    }

    /**
     * Returns the caret colour, chosen to contrast with the background behind the text.
     */
    private int caretColor() {
        int background = 0xFF000000; // Editor panels are dark by default
        try {
            var style = element.style();
            if (style.isBackgroundShown()) {
                var bg = style.backgroundColor();
                if (bg.isPresent()) {
                    background = colorResolver.resolve(bg.get());
                }
            }
        } catch (RuntimeException ignored) {
            // Fall back to the default above
        }
        return isDark(background) ? 0xFFFFFFFF : 0xFF101010;
    }

    /**
     * Returns whether the given ARGB colour is dark, using Rec. 709 luma.
     */
    private static boolean isDark(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return 0.2126f * r + 0.7152f * g + 0.0722f * b < 128;
    }

    /**
     * Renders the background of the text field based on the style of the associated TextElement.
     *
     * @param context The render context used for rendering.
     */
    private void renderBackground(RenderContext context) {
        if (!element.style().isBackgroundShown()) {
            return;
        }
        var element = elementToLayout();
        var bounds = bounds();
        var background = element.paddedSize();
        var x = bounds.x() + element.backgroundOffset();
        var y = bounds.y() + element.backgroundOffset();
        var color = colorResolver.resolve(element.style().backgroundColor().orElseThrow());
        // Fill background
        context.graphics().fill(
            x,
            y,
            x + background.width(),
            y + background.height(),
            color
        );
    }

    /**
     * Renders the outline of the text field if the outline is enabled in the style.
     *
     * @param context The render context used for rendering.
     */
    private void renderOutline(RenderContext context) {
        if (!element.style().isOutlineShown()) {
            return;
        }
        var element = elementToLayout();
        var bounds = bounds();
        var background = element.paddedSize();
        var thickness = element.style().outlineWidth();
        var color = colorResolver.resolve(element.style().outlineColor().orElseThrow());
        // Render outline
        UiUtil.renderOutline(
            context.graphics(),
            bounds.x(),
            bounds.y(),
            background.width() + thickness * 2,
            background.height() + thickness * 2,
            thickness,
            color
        );
    }

    @Override
    protected void renderHighlight(RenderContext context, int x, int y, int width) {
        // Render based on lines
        var lines = elementToShow().lines();
        var highlightStart = linePosOf(highlightStart());
        var highlightEnd = linePosOf(highlightEnd());
        // Render each line of the highlight
        for (int lineIndex = highlightStart.line; lineIndex <= highlightEnd.line; lineIndex++) {
            var line = lines.get(lineIndex);
            int startChar = (lineIndex == highlightStart.line)
                ? highlightStart.pos
                : 0;
            int endChar = (lineIndex == highlightEnd.line)
                ? highlightEnd.pos
                : line.length();
            var font = context.font();
            var lineText = line.substring(startChar, endChar);
            var textPos = textPosition();
            int highlightX = textPos.x() + formattedWidth(line.substring(0, startChar)) + element.lineXOffset(line);
            int highlightY = textPos.y() + lineIndex * font.lineHeight + (lineIndex * element.style().lineGap());
            int highlightWidth = formattedWidth(lineText);
            super.renderHighlight(context, highlightX, highlightY, highlightWidth);
        }
    }

    /**
     * Suppresses clickui's built-in caret.
     * <p>
     * clickui's {@code AbstractField#renderTextField} only calls this while its private
     * {@code isCursorVisible()} allows it, which is a plain
     * {@code System.currentTimeMillis()} blink that is not tied to typing. The caret is
     * instead painted by {@link #drawCaret(RenderContext)} after the text has been drawn,
     * which keeps it visible and correctly positioned at all times.
     */
    @Override
    protected void renderCursor(RenderContext context, int x, int y, boolean inline) {
        // Intentionally empty - see drawCaret(RenderContext)
    }

    @Override
    protected void renderText(RenderContext context, String text, int x, int y, boolean placeholder, String suggestion) {
        var color = textColor(placeholder);
        var graphics = context.graphics();
        var font = context.font();
        // Render the main text
        var element = elementToShow();
        var lines = element.lines();
        var lineY = y;
        for (var line : lines) {
            var lineX = x + element.lineXOffset(line);
            var formatted = element.formattedText(line);
            graphics.text(formatted, lineX, lineY, color, false); // No shadow
            lineY += font.lineHeight + this.element.style().lineGap();
        }
        // No suggestion support
    }

    /**
     * Calculates the render scale for the text field based on the block pixels,
     * text render scale, element scale, and UI scale.
     *
     * @return The calculated render scale.
     */
    private float renderScale() {
        return BLOCK_PIXELS
               * TEXT_RENDER_SCALE
               * element.scale()
               * UI_SCALE;
    }

    private int formattedWidth(String text) {
        return Util.font().width(elementToShow().formattedText(text));
    }

    /**
     * Converts a cursor position in the text to a line and position within that line.
     *
     * @param cursorPos The cursor position in the text.
     * @return A LinePos object containing the line index and position within that line.
     */
    private LinePos linePosOf(int cursorPos) {
        var lines = elementToShow().lines();
        if (lines.isEmpty()) {
            return new LinePos(0, 0);
        }
        int charCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (cursorPos <= charCount + line.length()) {
                return new LinePos(i, cursorPos - charCount);
            }
            charCount += line.length() + 1; // +1 for newline
        }
        int lastLine = lines.size() - 1;
        return new LinePos(lastLine, lines.get(lastLine).length());
    }

    /**
     * A helper class representing a position in the text as a line index and a character position within that line.
     */
    protected class LinePos {
        protected final int line;
        protected final int pos;

        /**
         * Constructs a new LinePos object.
         *
         * @param line The index of the line.
         * @param pos  The position within the line.
         */
        protected LinePos(int line, int pos) {
            this.line = line;
            this.pos = pos;
        }

        /**
         * Converts the LinePos to a cursor position in the text.
         *
         * @return The cursor position corresponding to this LinePos.
         */
        protected int toCursorPos() {
            var lines = elementToShow().lines();
            int cursorPos = 0;
            for (int i = 0; i < line; i++) {
                cursorPos += lines.get(i).length() + 1; // +1 for newline
            }
            cursorPos += pos;
            return cursorPos;
        }
    }
}
