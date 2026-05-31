package com.thecompanyinc.cobblemoninitiative.config;

import com.thecompanyinc.cobblemoninitiative.DeathMechanicsInit;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightConfig;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightInit;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DeathModConfigScreen {

  public static Screen create(Screen parent) {
    DeathModConfig config = DeathModConfig.load();
    DeathModConfig defaults = new DeathModConfig();

    ConfigBuilder builder = ConfigBuilder.create()
      .setParentScreen(parent)
      .setTitle(Component.literal("Cobblemon Initiative Config"));

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    ConfigCategory general = builder.getOrCreateCategory(
      Component.literal("General Settings")
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Scale Damage by Party Size"),
          config.isScaleDamageByPartySize()
        )
        .setDefaultValue(defaults.isScaleDamageByPartySize())
        .setTooltip(
          Component.literal("If enabled, damage = maxHealth / partySize")
        )
        .setSaveConsumer(config::setScaleDamageByPartySize)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Use Max Health"),
          config.isUseMaxHealth()
        )
        .setDefaultValue(defaults.isUseMaxHealth())
        .setTooltip(
          Component.literal(
            "Use max health instead of current health for damage calculation"
          )
        )
        .setSaveConsumer(config::setUseMaxHealth)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startFloatField(
          Component.literal("Minimum Damage Percent"),
          config.getMinimumDamagePercent()
        )
        .setDefaultValue(defaults.getMinimumDamagePercent())
        .setMin(0.0f)
        .setMax(1.0f)
        .setTooltip(
          Component.literal(
            "Minimum damage as a percentage of max health (0.0 - 1.0)"
          )
        )
        .setSaveConsumer(config::setMinimumDamagePercent)
        .build()
    );

    ConfigCategory nuzlocke = builder.getOrCreateCategory(
      Component.literal("Nuzlocke Rules")
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Remove Fainted Pokémon"),
          config.isRemoveFaintedPokemon()
        )
        .setDefaultValue(defaults.isRemoveFaintedPokemon())
        .setTooltip(
          Component.literal(
            "Permanently remove Pokémon from party when they faint"
          )
        )
        .setSaveConsumer(config::setRemoveFaintedPokemon)
        .build()
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Sacrifice on Flee"),
          config.isSacrificeOnFlee()
        )
        .setDefaultValue(defaults.isSacrificeOnFlee())
        .setTooltip(
          Component.literal("Must sacrifice a Pokémon when fleeing from battle")
        )
        .setSaveConsumer(config::setSacrificeOnFlee)
        .build()
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Mystery Sacrifice"),
          config.isMysterySacrifice()
        )
        .setDefaultValue(defaults.isMysterySacrifice())
        .setTooltip(
          Component.literal(
            "Obfuscate Pokémon names when choosing sacrifice (like enchantment text)"
          )
        )
        .setSaveConsumer(config::setMysterySacrifice)
        .build()
    );

    ConfigCategory capture = builder.getOrCreateCategory(
      Component.literal("Capture Rules")
    );

    capture.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Send Caught to PC"),
          config.isSendCaughtToPC()
        )
        .setDefaultValue(defaults.isSendCaughtToPC())
        .setTooltip(
          Component.literal(
            "Send caught Pokémon directly to PC instead of party"
          )
        )
        .setSaveConsumer(config::setSendCaughtToPC)
        .build()
    );

    capture.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Caught Pokémon Start Fainted"),
          config.isSetCaughtToZeroHP()
        )
        .setDefaultValue(defaults.isSetCaughtToZeroHP())
        .setTooltip(Component.literal("Set caught Pokémon to 0 HP (fainted)"))
        .setSaveConsumer(config::setSetCaughtToZeroHP)
        .build()
    );

    capture.addEntry(
      entryBuilder
        .startEnumSelector(
          Component.literal("Duplicate Handling"),
          DeathModConfig.DuplicateHandling.class,
          config.getDuplicateHandling()
        )
        .setDefaultValue(defaults.getDuplicateHandling())
        .setTooltip(
          Component.literal(
            "OFF: Keep all\nRELEASE_IF_OWNED: Release if species in party or PC\nRELEASE_IF_EVER_CAUGHT: Release if ever caught before"
          )
        )
        .setSaveConsumer(config::setDuplicateHandling)
        .build()
    );

    ConfigCategory battleTypes = builder.getOrCreateCategory(
      Component.literal("Battle Types")
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Wild Battles"),
          config.isApplyInWildBattles()
        )
        .setDefaultValue(defaults.isApplyInWildBattles())
        .setTooltip(
          Component.literal("Take damage when Pokémon faint in wild battles")
        )
        .setSaveConsumer(config::setApplyInWildBattles)
        .build()
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Trainer Battles"),
          config.isApplyInTrainerBattles()
        )
        .setDefaultValue(defaults.isApplyInTrainerBattles())
        .setTooltip(
          Component.literal(
            "Take damage when Pokémon faint in NPC trainer battles"
          )
        )
        .setSaveConsumer(config::setApplyInTrainerBattles)
        .build()
    );

    ConfigCategory messages = builder.getOrCreateCategory(
      Component.literal("Messages")
    );

    messages.addEntry(
      entryBuilder
        .startStrField(
          Component.literal("Damage Message"),
          config.getDamageMessage()
        )
        .setDefaultValue(defaults.getDamageMessage())
        .setTooltip(
          Component.literal(
            "Message shown when taking damage. Use %pokemon% for the Pokémon name."
          )
        )
        .setSaveConsumer(config::setDamageMessage)
        .build()
    );

    // -------------------------------------------------------------------------
    // NPC Sight settings
    // -------------------------------------------------------------------------
    NpcSightConfig sightConfig = NpcSightConfig.load();
    NpcSightConfig sightDefaults = new NpcSightConfig();

    ConfigCategory npcSight = builder.getOrCreateCategory(
      Component.literal("NPC Sight")
    );

    npcSight.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Default Sight Range"),
          sightConfig.getDefaultSightRange(),
          1,
          256
        )
        .setDefaultValue(sightDefaults.getDefaultSightRange())
        .setTooltip(
          Component.literal(
            "Default sight range (blocks) for NPCs that don't have a per-entity override."
          )
        )
        .setSaveConsumer(sightConfig::setDefaultSightRange)
        .build()
    );

    npcSight.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Debug Mode"),
          sightConfig.isDebugMode()
        )
        .setDefaultValue(sightDefaults.isDebugMode())
        .setTooltip(
          Component.literal(
            "Show colored dust particles along NPC raycasts (green = clear, red = blocked)."
          )
        )
        .setSaveConsumer(sightConfig::setDebugMode)
        .build()
    );

    npcSight.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Dialog Trigger Range"),
          (int) sightConfig.getDialogRange(),
          1,
          20
        )
        .setDefaultValue((int) sightDefaults.getDialogRange())
        .setTooltip(
          Component.literal(
            "Player must be within this many blocks for the NPC dialog to open (when the NPC can see them)."
          )
        )
        .setSaveConsumer(v -> sightConfig.setDialogRange(v.doubleValue()))
        .build()
    );

    builder.setSavingRunnable(() -> {
      config.save();
      sightConfig.save();
      DeathMechanicsInit.reloadConfig();
      NpcSightInit.reloadConfig();
    });

    return builder.build();
  }
}
