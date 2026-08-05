package com.thecompanyinc.cobblemoninitiative.mixin;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Gates optional-mod mixins on their target mod actually being loaded, so a bare-mod
 * dev runtime (no CobbleDollars jar) never has Mixin try to transform a class that
 * does not exist. Easy NPC needs no entry here: it is a hard runtime presence for the
 * pack AND the dev runtime (modRuntimeOnly), whereas cobbledollars-less launches are
 * a supported bare-mod dev case.
 */
public class InitiativeMixinPlugin implements IMixinConfigPlugin {

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    if (mixinClassName.endsWith(".CobbleDollarsHudMixin")) {
      return FabricLoader.getInstance().isModLoaded("cobbledollars");
    }
    return true;
  }

  @Override
  public void onLoad(String mixinPackage) {}

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(
    String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo
  ) {}

  @Override
  public void postApply(
    String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo
  ) {}
}
