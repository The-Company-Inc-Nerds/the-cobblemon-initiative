package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The Cobblemon Initiative is single-player only, so a disconnect/kick from the integrated
 * server should return the player to the main menu — not the multiplayer server list.
 *
 * Vanilla {@code createDisconnectScreen} builds the {@link net.minecraft.client.gui.screens.DisconnectedScreen}
 * with a {@link JoinMultiplayerScreen} parent (a local world has no {@code ServerData}, so the
 * non-realm branch is taken), which makes the disconnect screen's button read "Back to Server
 * List" and land on the multiplayer menu. We swap that parent for a fresh {@link TitleScreen}
 * so it lands on the main menu instead. This covers the {@code /cobblemon-initiative install run}
 * kick as well as any other server disconnect. Realm disconnects use a different screen and are
 * untouched; a non-default parent (a custom {@code postDisconnectScreen}) is left as-is.
 */
@Mixin(ClientCommonPacketListenerImpl.class)
public class DisconnectToTitleMixin {

  @ModifyArg(
    method = "createDisconnectScreen",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screens/DisconnectedScreen;<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/DisconnectionDetails;)V"
    ),
    index = 0
  )
  private Screen cobbleminit$backToTitle(Screen parent) {
    return parent instanceof JoinMultiplayerScreen ? new TitleScreen() : parent;
  }
}
