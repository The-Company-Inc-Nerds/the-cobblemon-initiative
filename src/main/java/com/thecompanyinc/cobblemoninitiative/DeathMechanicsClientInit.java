package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.screen.SacrificeSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathMechanicsClientInit implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    DeathMechanicsInit.MOD_ID
  );

  public static boolean isPokemonWhiteout = false;

  @Override
  public void onInitializeClient() {
    LOGGER.info("Cobblemon Death Mod client initialized!");

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player != null && client.screen == null) {
        if (DeathMechanicsInit.consumePendingSacrifice()) {
          client.setScreen(new SacrificeSelectionScreen());
        }
      }
    });
  }

  public static void triggerWhiteoutDeath() {
    isPokemonWhiteout = true;
  }

  public static void resetWhiteoutFlag() {
    isPokemonWhiteout = false;
  }

  public static void triggerSacrificeSelection() {
    Minecraft.getInstance().execute(() -> {
      Minecraft.getInstance().setScreen(new SacrificeSelectionScreen());
    });
  }
}
