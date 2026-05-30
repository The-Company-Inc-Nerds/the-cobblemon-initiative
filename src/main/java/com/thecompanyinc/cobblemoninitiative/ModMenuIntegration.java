package com.thecompanyinc.cobblemoninitiative;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thecompanyinc.cobblemoninitiative.config.DeathModConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return DeathModConfigScreen::create;
  }
}
