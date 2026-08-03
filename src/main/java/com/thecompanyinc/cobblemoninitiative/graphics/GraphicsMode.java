package com.thecompanyinc.cobblemoninitiative.graphics;

/**
 * The two graphics presets the main-menu toggle (and {@code /gfx}) switch between.
 *
 * <p>HIGH = 256x textures + BSL shaders + fancy video settings (the shipped default).
 * LOW  = 32x textures + shaders off + performance video settings, for weaker machines.
 *
 * <p>The active <em>resource pack</em> is treated as the source of truth for which mode
 * is live — see {@link GraphicsPresetManager#detectMode()} — so a menu-side FancyMenu
 * "Manage Resource Pack" swap and an in-world {@code /gfx} command converge on one state.
 */
public enum GraphicsMode {
  HIGH,
  LOW;

  public GraphicsMode other() {
    return this == HIGH ? LOW : HIGH;
  }

  public String id() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }

  /** Parse "high"/"low" (case-insensitive); null on anything else. */
  public static GraphicsMode fromId(String s) {
    if (s == null) return null;
    return switch (s.trim().toLowerCase(java.util.Locale.ROOT)) {
      case "high" -> HIGH;
      case "low" -> LOW;
      default -> null;
    };
  }
}
