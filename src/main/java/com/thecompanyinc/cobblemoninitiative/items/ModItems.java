package com.thecompanyinc.cobblemoninitiative.items;

import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

  public static final ShrineCrystalItem FIRE_SHRINE_CRYSTAL =
    new ShrineCrystalItem("fire", "moltres", 70);

  public static final ShrineCrystalItem GROUND_SHRINE_CRYSTAL =
    new ShrineCrystalItem("ground", "groudon", 70);

  public static final ShrineCrystalItem ICE_SHRINE_CRYSTAL =
    new ShrineCrystalItem("ice", "articuno", 70);

  public static final ShrineCrystalItem DRAGON_SHRINE_CRYSTAL =
    new ShrineCrystalItem("dragon", "rayquaza", 70);

  public static final ShrineCrystalItem FAIRY_SHRINE_CRYSTAL =
    new ShrineCrystalItem("fairy", "xerneas", 70);

  public static final Item BADGE_ICON = new Item(new Item.Properties());

  public static void register() {
    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "fire_shrine_crystal"
      ),
      FIRE_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "ground_shrine_crystal"
      ),
      GROUND_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "ice_shrine_crystal"
      ),
      ICE_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "dragon_shrine_crystal"
      ),
      DRAGON_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "fairy_shrine_crystal"
      ),
      FAIRY_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        AchievementsInit.MOD_ID,
        "badge_icon"
      ),
      BADGE_ICON
    );

    AchievementsInit.LOGGER.info("Registered Shrine Crystal items");
    AchievementsInit.LOGGER.info("Registered Badge Icon item");
  }
}
