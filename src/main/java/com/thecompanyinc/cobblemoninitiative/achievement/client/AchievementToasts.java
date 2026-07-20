package com.thecompanyinc.cobblemoninitiative.achievement.client;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-only: pops a live-earn achievement toast.
 *
 * <p>Primary path builds the vanilla {@link AdvancementToast} straight from the synced
 * advancement display, so the toast frame/icon/title match the JSON exactly — even though
 * that advancement ships {@code show_toast:false} (the flag only governs the AUTOMATIC
 * toast on completion; manually constructing the toast bypasses it). This is precisely why
 * a silent mid-run backfill, which never sends the toast packet, stays toast-free.
 *
 * <p>Fallback ({@link SystemToast}) covers the rare case where the advancement is not in
 * the client tree (e.g. a bare-mod client that never received the datapack advancement).
 */
public final class AchievementToasts {

  private AchievementToasts() {}

  public static void show(String advancementId, String title) {
    Minecraft mc = Minecraft.getInstance();
    mc.execute(() -> {
      AdvancementHolder holder = resolve(mc, advancementId);
      if (holder != null) {
        mc.getToasts().addToast(new AdvancementToast(holder));
      } else {
        SystemToast.add(
          mc.getToasts(),
          SystemToast.SystemToastId.NARRATOR_TOGGLE,
          Component.literal("Achievement Unlocked!").withStyle(ChatFormatting.GOLD),
          Component.literal(title)
        );
      }
    });
  }

  private static AdvancementHolder resolve(Minecraft mc, String advancementId) {
    if (mc.getConnection() == null) return null;
    ClientAdvancements advancements = mc.getConnection().getAdvancements();
    if (advancements == null) return null;
    try {
      ResourceLocation id = ResourceLocation.parse(advancementId);
      AdvancementNode node = advancements.getTree().get(id);
      return node != null ? node.holder() : null;
    } catch (Exception e) {
      return null;
    }
  }
}
