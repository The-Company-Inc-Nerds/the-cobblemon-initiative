package com.thecompanyinc.cobblemoninitiative.fieldmark;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only: wheat-field location marking tool.
 *
 * <p>Stand at a field and run {@code /cobblemon-initiative field-mark add <id> <region>}
 * to capture its center, tune radius/setpiece, then {@code export} the JSON for the
 * Wheat War liberation system.
 *
 * <p>Remove this entrypoint from fabric.mod.json before final release.
 */
public class FieldMarkInit implements ModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-fieldmark");

  private static FieldMarkStorage storage;

  @Override
  public void onInitialize() {
    storage = new FieldMarkStorage();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      FieldMarkCommand.register(dispatcher, registryAccess, storage)
    );

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      storage.load(server);
      LOGGER.info("Field Mark loaded {} marked field(s).", storage.size());
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> storage.save());

    LOGGER.info("Field Mark dev tool initialized.");
  }

  public static FieldMarkStorage getStorage() {
    return storage;
  }
}
