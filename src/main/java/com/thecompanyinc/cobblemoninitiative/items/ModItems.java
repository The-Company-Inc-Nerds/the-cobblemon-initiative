package com.thecompanyinc.cobblemoninitiative.items;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

  // Shrine crystal guardians (showrunner-ruled 2026-07-19): distinct from the noble
  // roster — the old fire→Moltres / ground→Groudon / ice→Articuno / dragon→Rayquaza
  // picks collided with the noble bird/trio set-pieces. The crystal is the
  // place-anywhere WILD capture path; nobles keep their own site-based fights.

  public static final ShrineCrystalItem FIRE_SHRINE_CRYSTAL =
    new ShrineCrystalItem("fire", "hooh", 70);

  public static final ShrineCrystalItem GROUND_SHRINE_CRYSTAL =
    new ShrineCrystalItem("ground", "landorus", 70);

  public static final ShrineCrystalItem ICE_SHRINE_CRYSTAL =
    new ShrineCrystalItem("ice", "glastrier", 70);

  public static final ShrineCrystalItem DRAGON_SHRINE_CRYSTAL =
    new ShrineCrystalItem("dragon", "kyurem", 70);

  public static final ShrineCrystalItem FAIRY_SHRINE_CRYSTAL =
    new ShrineCrystalItem("fairy", "xerneas", 70);

  public static final Item BADGE_ICON = new Item(new Item.Properties());

  public static void register() {
    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "fire_shrine_crystal"
      ),
      FIRE_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "ground_shrine_crystal"
      ),
      GROUND_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "ice_shrine_crystal"
      ),
      ICE_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "dragon_shrine_crystal"
      ),
      DRAGON_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "fairy_shrine_crystal"
      ),
      FAIRY_SHRINE_CRYSTAL
    );

    Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(
        InitiativeInit.MOD_ID,
        "badge_icon"
      ),
      BADGE_ICON
    );

    InitiativeInit.LOGGER.info("Registered Shrine Crystal items");
    InitiativeInit.LOGGER.info("Registered Badge Icon item");
  }
}
