package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the live {@link LevelSettings} held by the server's world data so the install
 * command can flip the hardcore flag at runtime. Returning the field reference (not a copy)
 * is essential — {@link LevelSettingsAccessor#setHardcore} mutates this same instance.
 *
 * @see com.thecompanyinc.cobblemoninitiative.install.InstallCommand
 */
@Mixin(PrimaryLevelData.class)
public interface PrimaryLevelDataAccessor {

  @Accessor("settings")
  LevelSettings getSettings();
}
