package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.world.level.LevelSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Makes the otherwise-final {@code hardcore} flag on {@link LevelSettings} writable, so the
 * install command can promote an existing world to hardcore. {@code @Mutable} strips the
 * {@code final} modifier at load time.
 *
 * <p>Hardcore is normally fixed at world creation; the client only learns it from the login
 * packet, so a flip applied here takes full effect (permadeath UI) on the next rejoin.
 */
@Mixin(LevelSettings.class)
public interface LevelSettingsAccessor {

  @Mutable
  @Accessor("hardcore")
  void setHardcore(boolean hardcore);
}
