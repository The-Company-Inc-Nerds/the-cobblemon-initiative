package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.devtools.DevWandTool;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Producer's Tool inputs that have no Fabric event (DEV-ONLY — strips with the devtools
 * package at 1.0.0; remove this mixin from cobblemon-initiative.mixins.json with it):
 * F (swap-hands) sets the primary position to the player's own feet, and Q (drop-item)
 * skips the current walk stop — the tool never actually swaps or drops.
 *
 * <p>Middle-click was the original spec for "my feet" but is unimplementable server-side:
 * bytecode-verified on 1.21.1, creative pick-block never sends ServerboundPickItemPacket
 * (it goes through SetCreativeModeSlot — and would swap the tool OUT of the hand), and
 * survival only sends it when the picked item sits in a non-hotbar slot. F fires
 * unconditionally as a PlayerAction, so it is the reliable gesture.
 *
 * <p>Q is predicted client-side (LocalPlayer.drop removes the stack BEFORE sending), so
 * after cancelling the server re-sends the full container to heal the ghost-empty hand.
 * Both handlers are invoked on the netty thread first ({@code ensureRunningOnSameThread}
 * re-dispatches), so the injection no-ops until it runs on the server thread.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class DevWandInputMixin {

  @Shadow public ServerPlayer player;

  @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
  private void cobblemonInitiative$wandActions(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
    MinecraftServer server = player.getServer();
    if (server == null || !server.isSameThread()) return;
    ServerboundPlayerActionPacket.Action action = packet.getAction();

    if (action == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
      if (DevWandTool.handleFeetKey(player)) {
        // No client prediction on the swap path, but resync defensively.
        player.containerMenu.sendAllDataToRemote();
        ci.cancel();
      }
      return;
    }

    if (action == ServerboundPlayerActionPacket.Action.DROP_ITEM
        || action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
      if (DevWandTool.handleDropKey(player)) {
        // The client already deleted the stack from its selected slot (LocalPlayer.drop
        // predicts before sending) — re-send the container or the tool ghost-vanishes.
        player.containerMenu.sendAllDataToRemote();
        ci.cancel();
      }
    }
  }
}
