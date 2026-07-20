package com.thecompanyinc.cobblemoninitiative.screen;

import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import com.thecompanyinc.cobblemoninitiative.nickname.NicknameManager;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The nickname ritual — offered once for every new acquisition (capture / gift /
 * trade / starter). Voluntary like {@link DaycareSelectionScreen}: ESC or "Keep"
 * walks away and the species name stands; nothing server-side is waiting.
 *
 * <p>Class name deliberately avoids "Dialog" — the e2e harness's wait_screen matches
 * screen class names by substring, and "Dialog" would collide with every Easy NPC
 * dialog wait in the suite.
 */
public class NicknamePromptScreen extends Screen {

  private final UUID monUuid;
  private final String speciesName;
  private EditBox nameBox;
  /** Survives init() rebuilds — a window resize mid-typing must not eat the name. */
  private String typedSoFar = "";

  public NicknamePromptScreen(UUID monUuid, String speciesName) {
    super(Component.literal("Nickname"));
    this.monUuid = monUuid;
    this.speciesName = speciesName;
  }

  @Override
  protected void init() {
    if (this.minecraft == null || this.minecraft.player == null) return;

    int centerX = this.width / 2;
    int boxY = this.height / 2 - 10;

    nameBox = new EditBox(this.font, centerX - 100, boxY, 200, 20,
      Component.literal("Nickname"));
    nameBox.setMaxLength(NicknameManager.MAX_NAME_LENGTH);
    nameBox.setHint(Component.literal("§8" + speciesName));
    nameBox.setValue(typedSoFar);
    nameBox.setResponder(value -> typedSoFar = value);
    this.addRenderableWidget(nameBox);
    this.setInitialFocus(nameBox);

    this.addRenderableWidget(
      Button.builder(Component.literal("Name it"), button -> confirm())
        .bounds(centerX - 155, boxY + 32, 150, 20)
        .build()
    );
    this.addRenderableWidget(
      Button.builder(Component.literal("Keep " + speciesName), button -> this.onClose())
        .bounds(centerX + 5, boxY + 32, 150, 20)
        .build()
    );
  }

  private void confirm() {
    String name = nameBox == null ? "" : nameBox.getValue().trim();
    if (!name.isEmpty()) {
      ClientPlayNetworking.send(new InitiativePayloads.NicknameSetPayload(monUuid, name));
    }
    if (this.minecraft != null) this.minecraft.setScreen(null);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    // Enter / numpad-enter confirms — but only while the text box owns focus,
    // so Tab→"Keep"→Enter still activates the button instead of naming.
    if ((keyCode == 257 || keyCode == 335) && nameBox != null && nameBox.isFocused()) {
      confirm();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  // Transparent over the world — the catch moment stays visible (house style:
  // both sibling screens skip the blur/dirt background).
  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {}

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    int centerX = this.width / 2;
    int y = this.height / 2 - 58;

    drawCentered(graphics, "§lNew teammate: " + speciesName, centerX, y, 0xFFD700);
    drawCentered(graphics, "The Company files everything under a designation.",
      centerX, y + 16, 0xAAAAAA);
    drawCentered(graphics, "This one is yours to name.", centerX, y + 28, 0xAAAAAA);

    super.render(graphics, mouseX, mouseY, delta);
  }

  private void drawCentered(GuiGraphics graphics, String text, int centerX, int y, int color) {
    graphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, true);
  }

  /** Naming is voluntary — ESC keeps the species name (unlike the sacrifice screen). */
  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
