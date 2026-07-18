package com.thecompanyinc.cobblemoninitiative.screen;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Party picker — the voluntary sibling of {@link SacrificeSelectionScreen} (same layout),
 * with multi-select capped at the facility's free slots, ESC allowed, and a hard guard
 * that at least one Pokémon stays in the party. Fully network-driven since 0.6.0-alpha.6:
 * the server's PickerOpenPayload carries the real free-slot count, and confirm sends a
 * PickerDepositPayload the server re-validates — no singleplayer-server bridge, so the
 * picker works on dedicated servers (and the e2e harness) too.
 *
 * Parameterised so it serves BOTH the Sango daycare (XP boarding, 2 slots) and Mom's
 * friendship care (1 slot) — the facility key routes the deposit server-side.
 */
public class DaycareSelectionScreen extends Screen {

  private final List<PokemonSlot> pokemonSlots = new ArrayList<>();
  private final Set<Integer> selectedSlots = new HashSet<>();
  private int capacity = 0;
  private int partySize = 0;

  private final String facilityTitle;
  private final int titleColor;
  private final int freeSlots;
  private final String confirmLabel;
  private final String facilityKey;

  public DaycareSelectionScreen(
    String facilityTitle,
    int titleColor,
    int freeSlots,
    String confirmLabel,
    String facilityKey
  ) {
    super(Component.literal(facilityTitle));
    this.facilityTitle = facilityTitle;
    this.titleColor = titleColor;
    this.freeSlots = freeSlots;
    this.confirmLabel = confirmLabel;
    this.facilityKey = facilityKey;
  }

  @Override
  protected void init() {
    pokemonSlots.clear();
    selectedSlots.clear();

    if (this.minecraft == null || this.minecraft.player == null) return;

    // The server computed the free-slot count when it sent the open payload.
    capacity = Math.max(0, freeSlots);

    ClientParty party = CobblemonClient.INSTANCE.getStorage().getParty();
    List<Pokemon> partyPokemon = new ArrayList<>();
    for (Pokemon pokemon : party) {
      if (pokemon != null) {
        partyPokemon.add(pokemon);
      }
    }
    partySize = partyPokemon.size();

    int slotSize = 80;
    int spacing = 10;
    int totalWidth = 3 * slotSize + 2 * spacing;
    int startX = (this.width - totalWidth) / 2;
    int startY = this.height / 2 - slotSize - spacing / 2;

    for (int index = 0; index < partyPokemon.size(); index++) {
      Pokemon pokemon = partyPokemon.get(index);
      int row = index / 3;
      int col = index % 3;
      int x = startX + col * (slotSize + spacing);
      int y = startY + row * (slotSize + spacing);

      pokemonSlots.add(new PokemonSlot(index, x, y, slotSize, pokemon));
    }

    this.addRenderableWidget(
      Button.builder(Component.literal(confirmLabel), button ->
        confirmDeposit()
      )
        .bounds(this.width / 2 - 155, this.height - 50, 150, 20)
        .build()
    );
    this.addRenderableWidget(
      Button.builder(Component.literal("Never mind"), button ->
        this.onClose()
      )
        .bounds(this.width / 2 + 5, this.height - 50, 150, 20)
        .build()
    );
  }

  @Override
  public void renderBackground(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {}

  @Override
  public void render(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {
    String title = facilityTitle;
    int titleWidth = this.font.width(title);
    graphics.drawString(
      this.font,
      title,
      (this.width - titleWidth) / 2,
      30,
      titleColor,
      true
    );

    String subtitle = capacity > 0
      ? "Select up to " + capacity + " Pokémon to board (" + selectedSlots.size() + " selected):"
      : "No room right now — collect a boarder first.";
    int subtitleWidth = this.font.width(subtitle);
    graphics.drawString(
      this.font,
      subtitle,
      (this.width - subtitleWidth) / 2,
      50,
      0xFFFFFF,
      true
    );

    if (capacity > 0 && partySize - selectedSlots.size() <= 1 && partySize > 0) {
      String hint = "At least one Pokémon must stay with you.";
      graphics.drawString(
        this.font,
        hint,
        (this.width - this.font.width(hint)) / 2,
        62,
        0xAAAAAA,
        true
      );
    }

    for (PokemonSlot slot : pokemonSlots) {
      slot.render(
        graphics,
        this.font,
        mouseX,
        mouseY,
        selectedSlots.contains(slot.index)
      );
    }

    super.render(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0 && capacity > 0) {
      for (PokemonSlot slot : pokemonSlots) {
        if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
          if (selectedSlots.contains(slot.index)) {
            selectedSlots.remove(slot.index);
          } else if (
            selectedSlots.size() < capacity &&
            partySize - selectedSlots.size() > 1 // never board the last party Pokémon
          ) {
            selectedSlots.add(slot.index);
          }
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void confirmDeposit() {
    if (
      selectedSlots.isEmpty() ||
      this.minecraft == null ||
      this.minecraft.player == null
    ) {
      return;
    }

    List<UUID> monUuids = new ArrayList<>();
    for (PokemonSlot slot : pokemonSlots) {
      if (selectedSlots.contains(slot.index)) {
        monUuids.add(slot.pokemon.getUuid());
      }
    }

    // The server-side receiver routes to the facility manager, which re-validates
    // slots, ownership, and the last-mon guard before mutating anything.
    ClientPlayNetworking.send(
      new InitiativePayloads.PickerDepositPayload(facilityKey, monUuids));

    this.minecraft.setScreen(null);
  }

  /** Boarding is voluntary — unlike the sacrifice screen, ESC just walks away. */
  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  private static class PokemonSlot {

    final int index;
    final int x, y, size;
    final Pokemon pokemon;
    final String displayName;
    final String displayLevel;
    final String displayHp;

    PokemonSlot(int index, int x, int y, int size, Pokemon pokemon) {
      this.index = index;
      this.x = x;
      this.y = y;
      this.size = size;
      this.pokemon = pokemon;

      String name = pokemon.getSpecies().getName();
      this.displayName = name.length() > 10 ? name.substring(0, 9) + "…" : name;
      this.displayLevel = "Lv. " + pokemon.getLevel();
      this.displayHp = pokemon.getCurrentHealth() + "/" + pokemon.getMaxHealth();
    }

    void render(
      GuiGraphics graphics,
      net.minecraft.client.gui.Font font,
      int mouseX,
      int mouseY,
      boolean selected
    ) {
      int bgColor = selected
        ? 0xFF227744
        : (isMouseOver(mouseX, mouseY) ? 0xFF666666 : 0xFF333333);
      graphics.fill(x, y, x + size, y + size, bgColor);

      int borderColor = selected ? 0xFF55FF55 : 0xFFAAAAAA;
      graphics.renderOutline(x, y, size, size, borderColor);

      int nameWidth = font.width(displayName);
      graphics.drawString(
        font,
        displayName,
        x + (size - nameWidth) / 2,
        y + size / 2 - 10,
        0xFFFFFF,
        false
      );

      int levelWidth = font.width(displayLevel);
      graphics.drawString(
        font,
        displayLevel,
        x + (size - levelWidth) / 2,
        y + size / 2 + 5,
        0xAAAAAA,
        false
      );

      int hpWidth = font.width(displayHp);
      int hpColor = pokemon.isFainted() ? 0xFF5555 : 0x55FF55;
      graphics.drawString(
        font,
        displayHp,
        x + (size - hpWidth) / 2,
        y + size / 2 + 20,
        hpColor,
        false
      );
    }

    boolean isMouseOver(int mouseX, int mouseY) {
      return (
        mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size
      );
    }
  }
}
