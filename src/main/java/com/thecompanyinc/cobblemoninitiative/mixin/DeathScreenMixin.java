package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.screen.PokeballDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class DeathScreenMixin {

  @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
  private void onSetScreen(Screen screen, CallbackInfo ci) {
    // PokeballDeathScreen is the ONLY carrier of the Die-with-Honor / Dishonorable-Respawn
    // choice. It was previously shown only on a Nuzlocke whiteout — so a hardcore death from
    // natural causes (fall, lava, mob, drowning) fell through to the vanilla hardcore screen
    // (Spectate / Title only) and the player never got the respawn option. Show it on ANY
    // hardcore death as well as the whiteout. PokeballDeathScreen extends Screen (not
    // DeathScreen), so replacing the DeathScreen here cannot re-enter this hook.
    if (!(screen instanceof DeathScreen)) return;
    Minecraft client = (Minecraft) (Object) this;
    boolean whiteout = NuzlockeInit.consumePendingWhiteoutDeath(); // always consume the flag
    boolean hardcore = client.level != null && client.level.getLevelData().isHardcore();
    if (whiteout || hardcore) {
      client.setScreen(new PokeballDeathScreen());
      ci.cancel();
    }
  }
}
