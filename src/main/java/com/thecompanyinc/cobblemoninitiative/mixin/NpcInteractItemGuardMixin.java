package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Talking to an Easy NPC must not ALSO fire the held item. Vanilla only skips held-item
 * use when the entity interaction returns a CONSUMING result; Easy NPC's client-side
 * {@code mobInteract} returns PASS (its dialog opens server-side), so the client falls
 * through to item use — eating food, opening the Pokédex, etc. — and re-fires on every
 * click while the dialog is open (showrunner report 2026-07-17).
 *
 * <p>This client mixin coerces {@code MultiPlayerGameMode.interact}'s return to a
 * consuming result for {@code easy_npc}-namespace targets. The interact packet is already
 * sent inside {@code interact()} BEFORE this RETURN (verified in bytecode), so the server
 * still opens the dialog normally — we only stop the CLIENT from proceeding to item use,
 * which means no use-item packet is ever sent and nothing is consumed server-side either.
 * CONSUME (not SUCCESS) so there is no arm-swing on the held item.
 */
@Mixin(MultiPlayerGameMode.class)
public class NpcInteractItemGuardMixin {

  @Inject(method = "interact", at = @At("RETURN"), cancellable = true)
  private void ci$suppressItemUseOnNpc(
      Player player, Entity target, InteractionHand hand,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (hand != InteractionHand.MAIN_HAND) return;
    if (cir.getReturnValue().consumesAction()) return; // NPC already handled it
    if (!"easy_npc".equals(
        BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getNamespace())) {
      return;
    }
    // Easy NPC's own tools (wand / preset / move items) act THROUGH the held item — leave them.
    ItemStack held = player.getItemInHand(hand);
    if ("easy_npc".equals(BuiltInRegistries.ITEM.getKey(held.getItem()).getNamespace())) {
      return;
    }
    cir.setReturnValue(InteractionResult.CONSUME);
  }
}
