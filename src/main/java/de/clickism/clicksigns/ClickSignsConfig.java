package de.clickism.clicksigns;

import de.clickism.configured.Config;
import de.clickism.configured.ConfigOption;
import de.clickism.configured.json.JsonFormat;

/**
 * The configuration file for ClickSigns.
 */
public interface ClickSignsConfig {
    // Use format directly, since service providers cause problems when running in dev for legacy forge.
    Config CONFIG = Config.of("config/ClickSigns/config.jsonc", JsonFormat.jsonc())
        .version(1)
        .appendDefaults()
        .header("""
            ---------------------------------------------------------
            ClickSigns Config
            NOTE: RESTART MINECRAFT FOR CHANGES TO TAKE EFFECT
            ---------------------------------------------------------
            """);

    // Defaulted to false in this fork: the checker looks the mod up on Modrinth by its mod id,
    // which is still "clicksigns" (see ClickSigns#MOD_ID). That resolves to the upstream project,
    // so an upstream release would be advertised to players of this fork - and installing it would
    // silently drop the CJK input and glyph support. Re-enable only after pointing the checker at
    // this fork's own project id.
    ConfigOption<Boolean> CHECK_UPDATES =
        CONFIG.option("check_updates", false)
            .description("""
                Whether to check for updates on game start.
                Disabled by default in ClickSigns-CJK: the mod id is still "clicksigns", so the
                check would find upstream releases instead of this fork's.
                """);
}
