package com.thecompanyinc.cobblemoninitiative.docprop;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * "Found it" document props for THE INCOMPLETE FILE (Rebuild the Ledger).
 *
 * <p>Two set-dressing containers in Sango — the chest on Lumo's cart and the barrel by the
 * farm fountain — hide the misfiled records the player rebuilds for Lucian. Instead of the
 * old walk-near auto-pickup (removed from {@code personnel_file/tick.mcfunction}), the
 * player now clicks the container: this handler swallows the vanilla open and "finds" the
 * document in place.
 *
 * <p>Registered as a {@code UseBlockCallback} <em>before</em> {@link
 * com.thecompanyinc.cobblemoninitiative.lootchest.LootChestManager} so it wins at the
 * portrait <em>chest</em> (which LootChest would otherwise stock as a loot cache) during the
 * pickup window. It only intercepts while the quest wants the doc ({@code file_opened} and
 * the doc not yet taken); outside that window it PASSes and the container behaves normally.
 *
 * <p>The give itself reuses the existing datapack functions so the item NBT and the on-screen
 * "Recovered:" message stay single-sourced. The functions run as the player (source entity =
 * player, so {@code @s} resolves) at permission level 4 (their {@code give}/{@code tag}/{@code
 * title} lines need op-2+).
 */
public class DocPropManager {

  /** A clickable quest-prop container: click it to "find" {@code giveFunction}'s document. */
  private record DocProp(BlockPos pos, String requireTag, String presentTag, String giveFunction) {}

  // Positions mirror the removed proximity checks in
  // data/cobblemon_initiative/function/sidequest/personnel_file/tick.mcfunction.
  private static final List<DocProp> PROPS = List.of(
    // chest on the cart by Lumo -> the sun-faded portrait backing
    new DocProp(
      new BlockPos(2591, 111, 2815), "file_opened", "doc_portrait",
      "cobblemon_initiative:sidequest/personnel_file/give_doc_portrait"),
    // barrel by the farm fountain -> the re-signed ledger page
    new DocProp(
      new BlockPos(2584, 107, 2925), "file_opened", "doc_ledger",
      "cobblemon_initiative:sidequest/personnel_file/give_doc_ledger")
  );

  /** {@code UseBlockCallback} handler — see class doc. */
  public InteractionResult onUse(
    Player player,
    Level level,
    InteractionHand hand,
    BlockHitResult hit
  ) {
    if (level.isClientSide()) return InteractionResult.PASS;
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
    // Sneaking = placing a block against the container, not "using" it.
    if (serverPlayer.isShiftKeyDown()) return InteractionResult.PASS;

    BlockPos pos = hit.getBlockPos();
    for (DocProp prop : PROPS) {
      if (!prop.pos().equals(pos)) continue;

      // Only "find" the document while the quest is open and it has not been taken yet.
      // Otherwise leave the container to vanilla / LootChest (PASS).
      boolean wanted = serverPlayer.getTags().contains(prop.requireTag())
          && !serverPlayer.getTags().contains(prop.presentTag());
      if (!wanted) return InteractionResult.PASS;

      MinecraftServer server = serverPlayer.getServer();
      if (server != null) {
        server.getCommands().performPrefixedCommand(
          serverPlayer.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
          "function " + prop.giveFunction());
      }
      // Swallow the container open — the doc was "found" in place.
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }
}
