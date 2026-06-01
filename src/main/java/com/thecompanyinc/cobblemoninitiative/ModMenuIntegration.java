package com.thecompanyinc.cobblemoninitiative;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thecompanyinc.cobblemoninitiative.config.InitiativeConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return InitiativeConfigScreen::create;
  }
}
