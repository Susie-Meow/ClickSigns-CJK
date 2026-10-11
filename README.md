![banner](https://cdn.modrinth.com/data/cached_images/d418f45c6a9a85c1d5f12821ec8440961ed9d8fd_0.webp)

![fabric](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/fabric_vector.svg)

> 本文件为中英对照版本：**每个段落先写中文，再写英文**，内容完全一致。  
> This file is bilingual: **each paragraph is given in Chinese first, then in English**, with identical content.

> [!NOTE]  
> **本项目是分叉版。** ClickSigns-CJK 修改自 [Clickism 的 ClickSigns](https://github.com/Clickism/ClickSigns)（GPL-3.0），修改日期 2026-10。上游作者 Clickism 的版权声明与 GPL-3.0 许可全文均予保留，本项目不暗示上游背书。完整改动清单见「本分支相对上游的改动」。  
> **This is a fork.** ClickSigns-CJK is derived from [ClickSigns by Clickism](https://github.com/Clickism/ClickSigns) (GPL-3.0); modifications dated 2026-10. Clickism's copyright notice and the full GPL-3.0 text are preserved, and nothing here implies upstream endorsement. The full change list is under "Changes in this fork".

> [!TIP]  
> **支持的不只是中文：日文和韩文同样可用。** 随附的 GNU Unifont 字形包对三种语言都是全覆盖，日文与韩文路牌可以直接输入、直接显示——**日本語・한국어도 그대로 입력하고 표시할 수 있습니다.**  
> **It is not just Chinese: Japanese and Korean work too.** The bundled GNU Unifont pack covers all three completely, so Japanese and Korean signs can be typed and displayed just as directly.

### 什么是 ClickSigns？/ What is ClickSigns?

ClickSigns 是一个简单的 Fabric 模组，用来制作自定义的高速公路与街道指示牌。虽然重点是路牌，但你实际上可以做出任何能动态叠加文字的图案，非常适合用来建造城市和村镇。

ClickSigns is a simple fabric mod that lets you create custom highway & street signs. Even though the main focus is street signs, you can create any type of image that can dynamically have text on it ingame. Ideal for building cities and towns!

- **制作你自己的路牌（资源包指南）：** [Wiki](https://github.com/Clickism/ClickSigns/wiki/Making-Custom-Road-Sign-Templates)
- *温馨提示：随着后续更新与扩展，会有越来越多的路牌加入进来。*
- **Make your own road signs (Resourcepack Guide):** [Wiki](https://github.com/Clickism/ClickSigns/wiki/Making-Custom-Road-Sign-Templates)
- *P.S: There will be more and more road signs added through regular updates & extensions*

> [!IMPORTANT]  
> ClickSigns 2.0 已完全重写。现在的 ClickSigns 是全自动、无限制的，可以做出比以往更复杂、更真实的路牌。  
> 本文件中的信息大部分还是针对旧版本撰写的，已经过时，后续会更新。
>
> 2.0 目前处于 beta 阶段，很快会发布正式版。
>
> 下面是 2.0 能做到什么的一个小展示：
>
> ![](https://cdn.modrinth.com/data/xaXWiLzT/images/035b3f5761c075339fb6ff47c37772dd8c149f8b.png)
>
> ClickSigns 2.0 has been completely rewritten from scratch. ClickSigns is now fully dynamic, with no limitations, allowing you to create much more complex and realistic signs than ever before.  
> The information in this readme is mostly outdated for the new version, and will be updated soon.
>
> 2.0 is currently in beta, and will soon be released as a full version
>
> Here's a tiny showcase of what's possible in 2.0:


### 本分支相对上游的改动 / Changes in this fork

本分支基于上游 [Clickism/ClickSigns](https://github.com/Clickism/ClickSigns) 的 `a9fde35`（`2.0-beta-6` 之后的 master HEAD）。以下所有改动都是在这份代码之上做的，共涉及 33 个文件（+603 / -105），并新增 4 个字体资源文件（Unifont 字形包、两个字体配置与一份许可文本）。

This fork is based on upstream [Clickism/ClickSigns](https://github.com/Clickism/ClickSigns) at `a9fde35` (master HEAD, just after `2.0-beta-6`). Everything below is a change on top of that tree: 33 files touched (+603 / -105) plus 4 new font assets (the Unifont glyph package, two font configs and one licence file).

#### 1. 改名与许可合规 / Renaming & licence compliance

分叉版改名为 **ClickSigns-CJK**（MC 双语路牌 Mod）。名字里的「双语」指的是中英双语路牌这个主要用途；字形本身对中文、日文、韩文都是全覆盖，详见第 2 节。GPL-3.0 约束的是许可与署名，并不约束名字；GNU 对长期独立发展的分叉反而**建议**换名字，以免混淆。上游仓库中关于 Chinese / CJK / font 的 issue 数为 0，中文支持不在其计划内，因此本项目的定位是独立分叉，而不是"上游的另一种皮肤"。

This fork is renamed **ClickSigns-CJK** (a bilingual road sign mod for Minecraft). "Bilingual" here describes the main use case - Chinese/English road signs; the glyph coverage itself spans Chinese, Japanese and Korean in full, see section 2. GPL-3.0 governs licensing and attribution, not names, and GNU actually *recommends* that a fork which will develop independently pick a different name to avoid confusion. The upstream repo has zero issues mentioning Chinese, CJK or fonts, so Chinese support is not on its roadmap - this is an independent fork rather than another skin for upstream.

**但 mod id 与资源命名空间刻意保持 `clicksigns` 不变。** `MOD_ID` 决定了方块注册 ID `clicksigns:road_sign`；一旦改动，已存档里放置的所有路牌会被 Minecraft 判定为缺失方块并直接清除，第三方使用 `clicksigns:` 命名空间的模板资源包也会全部失效。所以本项目是**上游的 drop-in 替换**：旧存档和现有资源包都能继续使用。Java 包名 `de.clickism.clicksigns` 同样保留——既减小 diff、方便后续合并上游补丁，也算一种对原作者的署名。

**The mod id and resource namespace are deliberately still `clicksigns`.** `MOD_ID` determines the block id `clicksigns:road_sign`; changing it would turn every sign in an existing world into a missing block that Minecraft clears away, and would break third-party template packs using the `clicksigns:` namespace. This fork is therefore a **drop-in replacement** for upstream. The Java package `de.clickism.clicksigns` is kept for the same reasons - a smaller diff, easier upstream merges, and a form of attribution to the original author.

合规方面做了三件事：保留 Clickism 的版权声明与 GPL-3.0 许可全文；在本文件显著位置标明「修改自 Clickism 的 ClickSigns」及修改日期（GPL §5(a)）；为随附的 GNU Unifont 补上许可与署名（见「第三方组件」）。此外 `check_updates` 默认改为关闭——它按 mod id 去 Modrinth 查询，会命中上游项目并提示玩家安装，从而覆盖掉中文化改动。

Three compliance items: Clickism's copyright notice and the full GPL-3.0 text are preserved; this file states prominently that the work is modified from Clickism's ClickSigns, with the modification date (GPL §5(a)); and the bundled GNU Unifont now ships with its licence and attribution (see "Third-party components"). In addition `check_updates` now defaults to off - it looks the mod up on Modrinth by mod id, which resolves to the upstream project and would tell players to install a build without the CJK work.

#### 2. 中文输入与 CJK 字形 / Chinese input & CJK glyphs

Minecraft 26.2 自带字形不包含汉字，因此首先补了字体：新增 `assets/minecraft/font/default.json` 与 `uniform.json`，把 `unihex` 提供器接入 `minecraft:default` / `minecraft:uniform` 两个全局字体，并随包附带 `clicksigns_unifont.zip`（GNU Unifont 15.0.01，57,086 个字形，压缩后 938 KB）。不需要额外装资源包，牌子、按钮、提示框和聊天栏都会用同一套像素字形。

Minecraft 26.2 ships no CJK glyphs, so the font comes first: new `assets/minecraft/font/default.json` and `uniform.json` wire a `unihex` provider into the global `minecraft:default` / `minecraft:uniform` fonts, backed by a bundled `clicksigns_unifont.zip` (GNU Unifont 15.0.01, 57,086 glyphs, 938 KB zipped). No extra resource pack is needed - signs, buttons, tooltips and the chat all use the same pixel look.

这套字形覆盖的不只是中文。**日文**：平假名与片假名各 96 字、半角片假名 63 字，全覆盖。**韩文**：谚文音节 11,172 字（现代韩文全部音节）加谚文字母 256 字，全覆盖。**汉字**：CJK 统一表意文字全部 20,992 字、扩展 A 全部 6,592 字，简体、繁体与日文汉字共用同一批字形。输入法与显示走的是同一条链路，所以日文和韩文的牌子不需要额外配置。

The pack covers more than Chinese. **Japanese**: fully covered (96 hiragana, 96 katakana, 63 halfwidth katakana). **Korean**: fully covered (all 11,172 modern hangul syllables plus 256 conjoining jamo). **Ideographs**: the complete CJK Unified block (20,992) and Ext-A (6,592), shared by Simplified, Traditional and Japanese kanji. Input and rendering share the same path, so Japanese and Korean signs need no extra setup.

一个要知道的边界：字形包只含基本多文种平面（BMP），U+20000 以上的扩展 B 及更晚的罕用汉字没有收录——这与 Minecraft 自带的 unifont 覆盖范围相同。所以日本人名用汉字（如「𠮷」）或极罕见的繁体字会显示成缺字方块，常用字则不受影响。

One limit worth knowing: the pack is BMP-only, so Ext-B and later rare ideographs above U+20000 are not included - the same coverage as Minecraft's own bundled unifont. Rare Japanese name kanji (e.g. 「𠮷」) or very rare Traditional characters will render as missing-glyph boxes; common characters are unaffected.

`SignTextField` 里搭了一层输入法桥接。clickui 的文本框不是 Minecraft 的原生输入控件，macOS 下系统输入法拿不到输入上下文，会自动退回英文输入（也就是"一切输入就跳英文输入法"的现象）。现在会聚焦一个隐藏的原生 `EditBox` 作为输入法接收端，Minecraft 的 IME 系统能识别它，合成文本通过 `setResponder` 回传；`mirrorFromIme()` 用公共前后缀做差分，只替换真正变化的区段，换行和光标位置因此得以保留。

`SignTextField` now bridges to a real IME. clickui's text field is not a native Minecraft input widget, so on macOS the OS input method gets no input context and silently falls back to Latin input (the "auto-switch to English input method" symptom). A hidden native `EditBox` is focused instead - Minecraft's IME system recognises it - and composed text is mirrored back through `setResponder`. `mirrorFromIme()` diffs by common prefix/suffix and replaces only the changed region, so line breaks and the caret position survive.

插入光标改为自绘：`renderCursor()` 被置空以屏蔽 clickui 自带的光标（它只有 1 像素宽，且由私有的 `isCursorVisible()` 用一条与输入无关的定时器控制）。新的 `drawCaret()` 在获得焦点时可见，编辑后 450 ms 内保持实心、之后按 500 ms 闪烁，颜色按背景亮度自动取白色或深灰，并加 1 像素反色描边，深浅背景都看得清。

The caret is self-drawn: `renderCursor()` is overridden to nothing to suppress clickui's own cursor (1 pixel wide, gated by a private `isCursorVisible()` whose timer is unrelated to typing). `drawCaret()` is visible whenever the field has focus, stays solid for 450 ms after an edit and then blinks every 500 ms, and picks white or dark grey from the background luma with a 1px contrasting outline, so it reads on both dark and light signs.

另外 `syncCaretFromIme()` 每帧同步一次原生光标：方向键、`Home`/`End`、鼠标点击只移动光标而不改变文本，responder 不会触发，所以必须轮询才能跟上。中英文混排、以及 IME 候选词上屏之后，光标位置都是精确的。

`syncCaretFromIme()` also polls the native caret once per frame: arrow keys, `Home`/`End` and mouse clicks move the caret without changing the text, so the responder never fires for them and polling is the only way to follow them. The caret position is exact for mixed Chinese/Latin text and right after an IME commit.

#### 3. 光影包（Iris）兼容 / Shader pack (Iris) compatibility

路牌使用自己的渲染管线（基于原版实体管线改造的半透明管线），好让底板、符号和文字能分层叠放而互不穿透。Iris 这类会替换 Minecraft 渲染层的光影包，会重编译所有它不认识的着色器程序，重编译过程中 ClickSigns 自定义管线里的 `ALPHA_CUTOUT` define 会丢失，路牌于是整块变全透明。

Road signs use their own render pipelines (a custom translucent pipeline derived from the vanilla entity one) so that plates, symbols and text can be layered without punching holes in each other. Shader packs such as Iris recompile every shader program they don't know about, and that recompile drops the `ALPHA_CUTOUT` define from the ClickSigns pipelines - the signs then come out fully transparent.

现在 `SignRenderPipelines` 会在启动时用 `Class.forName` 探测 Iris 与 VanillaPipelines，命中就回落到 Minecraft 原版的 `entity_translucent_cull` 管线。代价是分层深度处理被简化，重叠的底板与符号排序可能与不开光影时略有差异；关掉光影（游戏内按 `V` → *Shaders: OFF*）下次启动即恢复自定义管线。回退生效时日志会打一条 WARN，方便确认当前走的是哪条路径。

`SignRenderPipelines` now probes for Iris and VanillaPipelines at startup with `Class.forName` and, when present, falls back to Minecraft's vanilla `entity_translucent_cull` pipeline. The trade-off is that per-layer depth handling is simplified, so overlapping plates and symbols may sort slightly differently; turning shader packs off (`V` in game, then *Shaders: OFF*) restores the custom pipelines on the next start. A WARN line is logged when the fallback activates, so you can confirm which path is in use.

#### 4. 网络安全与健壮性 / Network safety & robustness

`RoadSignUpdatePacket` 增加了服务端校验：玩家必须站在牌子 8 格以内（`MAX_EDIT_DISTANCE_SQUARED = 64`），并且 `isValid()` 会检查尺寸落在 `MIN/MAX_SIGN_SIZE` 之间、元素数不超过 256。之前被改过的客户端可以修改世界上任意位置的牌子，现在服务端会拒绝。客户端侧也做同样的校验，防止恶意服务端下发畸形数据。读写异常改为 rethrow，避免半截 buffer 让整条连接错位。

`RoadSignUpdatePacket` gained server-side validation: the player must stand within 8 blocks of the sign (`MAX_EDIT_DISTANCE_SQUARED = 64`), and `isValid()` checks that the dimensions stay within `MIN/MAX_SIGN_SIZE` and that the sign has at most 256 elements. Previously a modified client could edit signs anywhere in the world, which the server now rejects. The client handler validates as well, to defend against a hostile server. Write/read failures now rethrow instead of being swallowed, which would otherwise desync the whole connection with a half-written buffer.

`PacketCodec` 在读列表前先校验长度（`MAX_LIST_SIZE = 4096`），并按 `min(size, 64)` 预分配，避免一个恶意长度值直接触发超大内存分配。`RoadSignCodec` 给 packet reader 里的 `Alignment.values()[ordinal]` 加了范围检查，越界不再抛 `ArrayIndexOutOfBoundsException`。`Network` 的 `handleClient` / `handleServer` 参数改为 `@Nullable` 并做空值早返回。

`PacketCodec` now bounds-checks the length before reading a list (`MAX_LIST_SIZE = 4096`) and pre-allocates with `min(size, 64)`, so a hostile length can no longer trigger a huge allocation. `RoadSignCodec` range-checks the `Alignment` ordinal in its packet reader instead of throwing `ArrayIndexOutOfBoundsException`. `Network.handleClient`/`handleServer` take `@Nullable` and return early on null.

**专用服务器崩溃修复。** 上面那套尺寸校验最初写成 `roadSign.width()`，而它会走到 `frontTexture()` → `MinecraftImages` → `net.minecraft.client.*`。这些类在服务端发行版里根本不存在，于是第一次编辑路牌就会抛 `NoClassDefFoundError`，把整个服务器带走（玩家看到的是「连接中断」）。单人游戏里集成服务端和客户端同在一个 JVM，所以永远复现不出来。现在服务端改用 `RoadSign.declaredSize()`——直接读 `Tiler` 里随包上传的 `outputWidth/outputHeight` 字段，纯数据、不碰任何客户端类；没有 `Tiler` 的静态图来源（5 个 EU 模板）尺寸由打包好的图片天然限定，交给客户端在应用前用真实贴图再校验一遍。另外，`RoadSignUpdatePacket` 与 `TextureSource` 的兜底 `catch` 从 `Exception` 放宽到 `Exception | LinkageError`：一个模组不应该有能力打挂专用服务器，万一还有别的路径漏到客户端类，也只会丢弃这一包并记日志。

**Dedicated server crash fixed.** The dimension check above originally called `roadSign.width()`, which goes through `frontTexture()` → `MinecraftImages` → `net.minecraft.client.*`. Those classes do not exist in a server distribution, so the very first sign edit raised `NoClassDefFoundError` and took the whole server down (players just see "connection reset"). A single-player integrated server shares one JVM with the client, so it never showed up there. The server now uses `RoadSign.declaredSize()`, which reads the `outputWidth`/`outputHeight` fields a `Tiler` carries in the packet — pure data, no client classes involved. Sources without a `tiler` (the 5 EU templates) take their size from a bundled image, which bounds it anyway, and clients still re-validate against real textures before applying. In addition the fallback `catch` in `RoadSignUpdatePacket` and `TextureSource` was widened from `Exception` to `Exception | LinkageError`: a mod must never be able to kill a dedicated server, so any remaining path that reaches a client-only class now drops that one packet and logs it.

#### 5. 编辑器行为修复 / Editor behaviour fixes

文本控件是按元素 ID 被 memo 缓存的，之前只在创建时拿到过一次元素，之后就再也不更新了。结果是调整 Font Size 时数字会变、但渲染出来的字完全不变（因为 `scale` 只存在于元素里）。现在 memo 内只负责创建和注册一次监听器，memo 外每次重建都重新同步元素（字号、样式、对齐），`textElement()` 也只在渲染相关属性变化时才清缓存并重排布局。

The text field is memoized by element id and used to receive the element exactly once, at creation. Raising the font size therefore changed the number while the rendered text stayed the same, because `scale` lives on the element and never reached the cached field. Now the memo only creates the field and registers its listener once, while the element (scale, style, alignment) is re-synced on every rebuild outside the memo; `textElement()` also only clears its cache and invalidates layout when a rendering-relevant property actually changed.

顺带修掉了上游的一个问题：`onValueChanged` 是累加的，上游在每次重建时都注册一次，会把同一段文本重复提交 N 次。

This also fixes an upstream issue: `onValueChanged` is additive, and upstream registered it on every rebuild, re-committing the same text N times.

#### 6. 上游资源缺陷修复 / Upstream asset fix

`highway_area.template.json` 与 `highway_area_cancel.template.json` 引用的 `symbols/pictograms/highway.png` 在包里并不存在（文件实际位于 `pictograms/services/highway.png`），所以模板预览会渲染成 Minecraft 的紫黑"缺失贴图"方块。上游 master 同样存在这个问题，已改为正确路径。

`highway_area.template.json` and `highway_area_cancel.template.json` referenced `symbols/pictograms/highway.png`, which is not in the pack (the file actually lives at `pictograms/services/highway.png`), so the template preview rendered Minecraft's purple/black *missing texture* square. Upstream master has the same bug; the reference is corrected here.

注意：一个路牌把解析后的贴图和元素**直接**存进方块实体，而不是存模板 ID，所以在此之前放置的牌子仍保留旧路径，需要重新 Apply Template 或重新放置才会生效。

Note that a road sign stores its resolved textures and elements **directly** in the block entity instead of a template id, so signs placed before this fix keep the old reference - re-apply the template or place a fresh sign to pick up the corrected pictogram.

#### 7. 清理 / Cleanups

`fabric.mod.json` 里 `"java": ">=17"` 改为 `">=25"`（实际产物是 Java 25 字节码），占位符 `"suggests": {"another-mod": "*"}` 改为 `{"iris": "*"}`。删除了死代码 `EditorActionHandler.spawnElementNearSelected` 与 `SignView.resetTextFieldCache`，删掉了 `settings.gradle.kts` 里的 `// TODO: 1.21.11?` 和 `stonecutter.gradle.kts` 里的空 `string { }`，并在 22 个 Java 文件中删掉了共 34 行未使用的导入。

In `fabric.mod.json`, `"java": ">=17"` became `">=25"` (the jar really is Java 25 bytecode) and the placeholder `"suggests": {"another-mod": "*"}` became `{"iris": "*"}`. Dead code was removed (`EditorActionHandler.spawnElementNearSelected`, `SignView.resetTextFieldCache`), along with the `// TODO: 1.21.11?` in `settings.gradle.kts` and the empty `string { }` in `stonecutter.gradle.kts`, plus 34 unused import lines across 22 Java files.

#### 8. 构建环境：仓库内不含本机专属配置 / Build environment: no machine-local settings in the repo

仓库刻意保留上游的 `org.gradle.configuration-cache=true` 与 Gradle toolchain 25，**不把任何只适用于某一台机器的设置写进版本库**。如果你的机器上配置缓存无法写入指纹文件（macOS 上会报 `Operation not permitted`），或者没装 JDK 25，请在本机的 `~/.gradle/gradle.properties` 里覆盖，**不要改仓库里的文件**：

The repository keeps upstream's `org.gradle.configuration-cache=true` and Gradle toolchain 25, and **commits nothing that is specific to one machine**. If the configuration cache cannot write its fingerprint on your machine (macOS reports `Operation not permitted`), or no JDK 25 is installed, override it in your own `~/.gradle/gradle.properties` - **not in the repo**:

```
org.gradle.configuration-cache=false
javaToolchain=26
```

`build.fabric.gradle.kts` 会读取 `javaToolchain` 属性（缺省 25），而 `sourceCompatibility` / `targetCompatibility` 恒为 25——所以无论用哪个 JDK 编译，产物字节码始终是 Java 25。这样分叉在任何机器上都能直接构建，本机差异完全留在版本库之外。

`build.fabric.gradle.kts` reads the `javaToolchain` property (defaulting to 25), while `sourceCompatibility`/`targetCompatibility` are fixed at 25 - so the emitted bytecode is Java 25 whatever JDK compiles it. The fork therefore builds anywhere, with machine differences kept entirely outside version control.

### 如何创建与编辑路牌 / How to: Create & Edit Road Signs

要合成一个 *Road Sign*（路牌），把 **8 个铁锭**围在**任意告示牌**周围：

To craft a *Road Sign*, put **8 iron ingots** around **any sign**:

![](https://cdn.modrinth.com/data/cached_images/bfafe170b70044d731ce1383d4a43bfeecfef7ac.jpeg)

然后把它放在墙上、铁栏杆上或任何你喜欢的地方，点击路牌即可打开编辑菜单：

Then, place it on a wall, on iron bars or anywhere you'd like, then click on the road sign to access the edit menu:

![](https://cdn.modrinth.com/data/cached_images/d8eda3c79c84934a543928b75ed33aa12866c1a7.jpeg)

- 像图片里那样填写文字。
- 用箭头在这个模板的不同变体（也就是配色）之间切换。
- 用对齐按钮调整路牌的对齐方式。
- **完成后务必点击 CONFIRM。** *否则你的修改不会被保存。*
- 点击 **Change Template** 可以浏览所有可用模板。
- Fill in your text like on the image.
- Use the arrows to change between different variants (aka. color schemes) of this template.
- Use the alignment buttons to change the alignment of the sign.
- **Make sure to click CONFIRM once you're done.** *Your changes won't be saved if you don't.*
- Click **Change Template** to browse through all the various templates.

#### 输入文字 / Typing text

- 文本框的行为和普通输入框一致：会有一个闪烁的光标标记插入点，方向键、`Home`/`End`、`Backspace`/`Delete`，以及用鼠标点击重新定位光标，都能正常工作。
- 打字时光标保持实心，只有停下来之后才开始闪烁，所以不会在按键过程中消失。
- **输入法可以正常用了。** 在 macOS、Windows、Linux 上都可以切换到中文 / 日文 / 韩文输入法，直接在编辑器里组词，候选窗会浮在输入框正上方，光标会跟随上屏的文字（含中英文混排）。
- 中文使用随模组附带的 GNU Unifont 像素字形渲染，不需要额外资源包或字体配置。
- The text field behaves like a normal text box: a blinking caret marks your insertion point, arrow keys, `Home`/`End`, `Backspace`/`Delete` and clicking to reposition all work as expected.
- The caret stays solid while you type and only starts blinking once you pause, so it never disappears mid-keystroke.
- **IME input works.** On macOS, Windows and Linux you can switch to your Chinese / Japanese / Korean input method and compose text directly in the sign editor - the candidate window pops up right above the field, and the caret follows the committed text (including mixed Chinese/Latin input).
- Chinese text renders in Minecraft's own pixel font (GNU Unifont), shipped with the mod. No extra resource pack or font configuration is needed.

### 如何更换路牌模板 / How to: Changing The Road Sign Template

- 点击 *Change Template* 之后，你会看到这样的界面：
- After you click *Change Template*, you will be welcomed by this screen:

![](https://cdn.modrinth.com/data/cached_images/4c5b226a9012829ec5d9cd8fc5a57e2ba69f7bf0.jpeg)

- **左侧**是所有**你已安装**的模板列表。
- 模板以 *"宽 x 高 : 资源包 : 名称 : (详情)"* 的格式显示。
- **右侧**是所选模板的全部细节。
- 这里同样可以用箭头查看不同变体。
- **使用筛选：** 点击你想要的方向，或者输入要搜索的宽/高。例如同时选中 **Left** 和 **Forward**，就只会显示同时具备这两个方向的模板。
- *On the left side*, you have a list of all the templates **you have installed.**
- The templates are displayed in the format: *"Width x Height : Pack : Name : (Details)"*
- *On the right side*, you have all the details regarding the selected template.
- You can use the arrows to look at different variants here as well.
- **Using Filters:** To use the filters, either click on the direction you want, or input the width/height you're searching. If you for example select both **Left** and **Forward**, it will only show you the sign templates that have both directions.

#### Highway Area 模板 / Highway Area templates

内置的 **Highway Area** 和 **Highway Area Cancel** 模板原先引用了一个不存在的记形符号：`symbols/pictograms/highway.png`，而包里实际的位置是 `symbols/pictograms/services/highway.png`。因此模板预览渲染出的是 Minecraft 的紫黑"缺失贴图"方块（Cancle 变体上还会叠一个红色叉）。

The bundled **Highway Area** and **Highway Area Cancel** templates had a broken pictogram reference: both pointed at `symbols/pictograms/highway.png`, but the shipped asset actually lives at `symbols/pictograms/services/highway.png`. The template preview therefore rendered Minecraft's purple/black *missing texture* square, with the red cancel cross drawn on top for the cancel variant.

引用已修正，两个模板现在会在绿色高速路底板上，用该 tileset 的 `foreground` 色绘制白色的 **EU Highway** 记形符号（两条车行道加横向交叉路）。

The reference is corrected, so both templates now draw the white **EU Highway** pictogram (an interchange of two carriageways) in the tileset's `foreground` color on the green highway plate.

**在此修复之前放置的牌子仍保留旧引用。** 路牌直接存储解析后的贴图与元素，而不是模板 ID，所以已经放好的 *Highway Area* 牌子仍会显示缺失贴图。重新打开牌子点一次 **Apply Template**（或者重新放置一块）即可生效。

**Signs placed before this fix keep the old reference.** A road sign stores its resolved textures and elements directly instead of a template id, so already placed *Highway Area* signs still show the missing texture. Re-open the sign and hit **Apply Template** again (or place a fresh sign) to pick up the corrected pictogram.

验证方式：打开 *Change Template*，展开 **Highway Signs**，选中 *Highway Area* / *Highway Area Cancel*——预览应该是绿底白字的高速路符号，而不是洋红棋盘格。

To verify: open *Change Template*, expand **Highway Signs** and select *Highway Area* / *Highway Area Cancel* - the preview should show a green sign with the white highway symbol instead of the magenta checkerboard.

### 自定义路牌 / How to: Custom Road Signs

- 找不到你想要的某个路牌模板？那就自己做！
- 模板系统是**资源包**驱动的、完全模块化、且**纯客户端**的。
- 也就是说，你可以自己编写或安装资源包来使用新的路牌模板。这些模板不需要装到服务器上，但其他玩家也需要装同一份资源包才能看到。
- You can't find a certain road sign template you want? Make it yourself!
- The template system is fully modular and client-side through **resourcepacks!**.
- That means, you can create or install custom resourcepacks to have access to new road sign templates. These don't have to be installed on the server, but other players would need to have the same resourcepack installed in order to see them.

### 与光影包一起使用 / How to: Using ClickSigns with Shader Packs

路牌使用自己的渲染管线（基于原版实体管线改造的自定义半透明管线），以便底板、符号和文字能够分层叠加而互不穿透。

Road signs use their own render pipelines (a custom translucent pipeline derived from the vanilla entity one) so that plates, symbols and text can be layered on top of each other without punching holes in each other.

会替换 Minecraft 渲染层的光影包（最典型的是 **Iris**）会重编译所有它不认识的着色器程序。一旦发生，路牌管线就会丢失自己的 shader define，路牌会变成**完全透明**。

Shader packs that replace Minecraft's rendering layer - most notably **Iris** - recompile every shader program they don't know about. When that happens the road sign pipelines lose their shader defines and the signs come out **fully transparent**.

为避免这一点，ClickSigns 启动时会检测 Iris（以及 VanillaPipelines）：

To prevent this, ClickSigns detects Iris (and VanillaPipelines) while it starts up:

- 装了 Iris 时，路牌自动回落到 Minecraft 原版的 `entity_translucent_cull` 管线。牌子完全可见，但分层深度处理被简化，重叠的底板与符号排序可能与不开光影时略有差异。
- 如果你想要原本的分层效果，把光影关掉（游戏内按 `V`，然后选 *Shaders: OFF*），ClickSigns 下次启动会重新使用自己的管线。
- With Iris installed, road signs automatically fall back to Minecraft's vanilla `entity_translucent_cull` pipeline. Signs are fully visible, but the per-layer depth handling is simplified, so overlapping plates and symbols may sort slightly differently than without a shader pack.
- If you want the original layered look, turn shader packs off (`V` in game, then *Shaders: OFF*). ClickSigns will use its own pipelines again the next time it starts.

回退生效时会往日志里写一行，方便你确认当前走的是哪条路径：

A line is written to the log when the fallback is activated, so you can confirm which path is in use:

```
[main/WARN]: net.irisshaders.iris.Iris is installed, so ClickSigns falls back to vanilla render types for road signs...
```

### 多人游戏与环境要求 / Multiplayer & Requirements

- **路牌只能近距离编辑。** 只有当你站在牌子 8 格以内时，服务端才会接受这次修改。之前被改过的客户端可以修改世界上任意位置的牌子，现在服务端会拒绝。
- 服务端还会校验每一个收到的路牌：尺寸必须落在路牌尺寸限制内，且元素数不得超过 256。越界的修改会被记录并丢弃，而不是应用后再广播出去。
- **需要 Java 25 或更高版本。** 模组编译到 Java 25 字节码，更旧的 Java 会以 `UnsupportedClassVersionError` 拒绝加载。
- 编辑路牌不需要创造模式——任何能够到它的玩家都可以修改。
- **Road signs can only be edited up close.** A sign edit is only accepted by the server if you are standing within 8 blocks of it. Previously a modified client could edit signs anywhere in the world, which the server now rejects.
- The server also validates every incoming sign: dimensions have to stay within the sign size limits and a sign may not contain more than 256 elements. Out-of-bounds edits are logged and discarded instead of being applied and broadcast.
- **Java 25 or newer is required.** The mod is compiled to Java 25 bytecode; older Java versions will refuse to load it with an `UnsupportedClassVersionError`.
- ClickSigns does not require creative mode to edit a sign - any player who can reach it can change it.

### 第三方组件 / Third-party components

- **GNU Unifont**（Roman Czyborra、Paul Hardy 及贡献者）：提供 Minecraft 26.x 缺失的汉字、日文假名与韩文谚文字形，版本 15.0.01，以 `assets/minecraft/font/clicksigns_unifont.zip` 随包分发。双许可 `(GPL-2.0-or-later WITH Font-exception-2.0) OR OFL-1.1`；本项目按 OFL-1.1 再分发，完整许可文本与署名见 [`UNIFONT-LICENSE.txt`](src/main/resources/assets/minecraft/font/UNIFONT-LICENSE.txt)。
- **GNU Unifont** (Roman Czyborra, Paul Hardy and contributors): supplies the Chinese, Japanese kana and Korean hangul glyphs that Minecraft 26.x does not ship. Version 15.0.01, bundled as `assets/minecraft/font/clicksigns_unifont.zip`. Dual-licensed `(GPL-2.0-or-later WITH Font-exception-2.0) OR OFL-1.1`; redistributed here under OFL-1.1, with the full licence text and attribution in [`UNIFONT-LICENSE.txt`](src/main/resources/assets/minecraft/font/UNIFONT-LICENSE.txt).

### 许可 / Licensing

- 本项目基于 GPLv3 许可发布，与上游 ClickSigns 保持一致；上游作者 Clickism 的版权声明与许可全文（LICENSE.md）均予保留。
- 详见 LICENSE.md。GNU Unifont 的许可与署名见上一节。
- This project is licensed under the GPLv3 license, same as upstream ClickSigns; Clickism's copyright notice and the full licence text (LICENSE.md) are preserved.
- Refer to LICENSE.md for more information. GNU Unifont's licence and attribution are covered in the section above.
