package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager;
import com.thecompanyinc.cobblemoninitiative.momcare.MomCareManager;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackClient;
import com.thecompanyinc.cobblemoninitiative.screen.DaycareSelectionScreen;
import com.thecompanyinc.cobblemoninitiative.screen.SacrificeSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuzlockeClientInit implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiativeInit.MOD_ID);

  @Override
  public void onInitializeClient() {
    LOGGER.info("Nuzlocke client initialized!");

    // Quest tracking — ] / [ keybinds + the in-process waypoint poller.
    QuestTrackClient.init();

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player != null && client.screen == null) {
        if (NuzlockeInit.consumePendingSacrifice()) {
          client.setScreen(new SacrificeSelectionScreen());
        } else if (DaycareManager.consumePendingPicker()) {
          // Fires the tick AFTER Easy NPC's deferred CLOSE_DIALOG clears the screen,
          // so a dialog-button deposit flows straight into the picker.
          client.setScreen(new DaycareSelectionScreen());
        } else if (MomCareManager.consumePendingPicker()) {
          // Mom's friendship care reuses the same picker, 1-slot, routed to MomCareManager.
          client.setScreen(new DaycareSelectionScreen(
            "Mom's Care", 0xFF77DD, MomCareManager.MAX_SLOTS, "Leave with Mom",
            uuid -> InitiativeInit.getMomCareManager().boardedCount(uuid),
            (uuid, mons) -> InitiativeInit.getMomCareManager().deposit(uuid, mons)));
        }
      }
    });
  }

  public static void triggerSacrificeSelection() {
    Minecraft.getInstance().execute(() ->
      Minecraft.getInstance().setScreen(new SacrificeSelectionScreen())
    );
  }
}
