package com.thecompanyinc.cobblemoninitiative.phone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

/**
 * PokePhone call scripts — the data behind the client call screen (0.7.0-alpha.20, replacing
 * the invisible-caller Easy NPC dialog delivery). Loaded from {@code data/<any namespace>/
 * phone_calls/<id>.json} through the server's resource manager at SERVER_STARTED, so a
 * datapack can add calls without touching the mod jar (the shipped calls live under
 * {@code data/cobblemon_initiative/phone_calls/} — Agent-B/content territory).
 *
 * <p>Pages are SEQUENTIAL — this is our own screen, so unlike an Easy NPC {@code say[]}
 * entry (a one-page-per-open rotation, ENGINE_FINDINGS §2) a monologue may be authored
 * across pages and every viewer reads all of them in order.
 */
public final class PhoneCallScripts {

  /** Corporate gold — the default accent when a script names none. */
  public static final int DEFAULT_ACCENT = 0xE8B84B;

  public static final String AVATAR_INITIALS = "initials";
  public static final String AVATAR_UNKNOWN = "unknown";

  private static final Set<String> KNOWN_KEYS = Set.of(
    "caller", "subtitle", "avatar", "accent", "pages", "choices", "on_complete", "done_tag");
  private static final Set<String> KNOWN_CHOICE_KEYS = Set.of("label", "commands");

  /** One loaded call script. {@code choices} / {@code onComplete} are empty (never null)
   *  when absent; {@code doneTag} is null when absent. */
  public record PhoneCallScript(
    String id,
    String caller,
    String subtitle,
    String avatar,
    int accent,
    List<String> pages,
    List<Choice> choices,
    List<String> onComplete,
    String doneTag
  ) {
    public record Choice(String label, List<String> commands) {}
  }

  private PhoneCallScripts() {}

  /** Scan every namespace's {@code phone_calls/} dir; invalid files are skipped with a warn
   *  (never aborts the scan — one bad datapack call must not silence the story phone). */
  public static Map<String, PhoneCallScript> load(MinecraftServer server) {
    Map<String, PhoneCallScript> scripts = new LinkedHashMap<>();
    Map<ResourceLocation, Resource> found = server.getResourceManager()
      .listResources("phone_calls", loc -> loc.getPath().endsWith(".json"));
    for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
      ResourceLocation loc = entry.getKey();
      String path = loc.getPath(); // "phone_calls/<id>.json"
      String id = path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length());
      try (InputStream in = entry.getValue().open()) {
        PhoneCallScript script = parse(
          id, JsonParser.parseReader(
            new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
        PhoneCallScript previous = scripts.put(id, script);
        if (previous != null) {
          InitiativeInit.LOGGER.warn(
            "[Phone] Duplicate call script id '{}' — {} replaced an earlier copy.", id, loc);
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.warn("[Phone] Skipping unreadable call script {}: {}",
          loc, e.getMessage());
      }
    }
    InitiativeInit.LOGGER.info("[Phone] Loaded {} call script(s).", scripts.size());
    return scripts;
  }

  private static PhoneCallScript parse(String id, JsonObject json) {
    List<String> unknown = new ArrayList<>();
    for (String key : json.keySet()) {
      if (!KNOWN_KEYS.contains(key)) unknown.add(key);
    }
    if (!unknown.isEmpty()) {
      InitiativeInit.LOGGER.warn("[Phone] Call '{}' has unknown key(s) {} — ignored.", id, unknown);
    }

    String caller = optString(json, "caller", "UNKNOWN");
    String subtitle = optString(json, "subtitle", "");
    String avatar = optString(json, "avatar", AVATAR_INITIALS);
    if (!AVATAR_INITIALS.equals(avatar) && !AVATAR_UNKNOWN.equals(avatar)) {
      InitiativeInit.LOGGER.warn(
        "[Phone] Call '{}': avatar '{}' is not initials|unknown — using initials.", id, avatar);
      avatar = AVATAR_INITIALS;
    }

    int accent = DEFAULT_ACCENT;
    if (json.has("accent")) {
      String raw = json.get("accent").getAsString();
      try {
        accent = Integer.parseInt(raw.startsWith("#") ? raw.substring(1) : raw, 16) & 0xFFFFFF;
      } catch (NumberFormatException e) {
        InitiativeInit.LOGGER.warn(
          "[Phone] Call '{}': accent '{}' is not #RRGGBB hex — using corporate gold.", id, raw);
      }
    }

    List<String> pages = stringList(json, "pages");
    if (pages.isEmpty() || pages.stream().anyMatch(String::isBlank)) {
      throw new IllegalArgumentException("pages must be a non-empty array of non-blank strings");
    }

    List<PhoneCallScript.Choice> choices = new ArrayList<>();
    if (json.has("choices")) {
      for (JsonElement el : json.getAsJsonArray("choices")) {
        JsonObject c = el.getAsJsonObject();
        for (String key : c.keySet()) {
          if (!KNOWN_CHOICE_KEYS.contains(key)) {
            InitiativeInit.LOGGER.warn(
              "[Phone] Call '{}': unknown choice key '{}' — ignored.", id, key);
          }
        }
        String label = optString(c, "label", "");
        if (label.isBlank()) {
          throw new IllegalArgumentException("every choice needs a non-blank label");
        }
        choices.add(new PhoneCallScript.Choice(label, stringList(c, "commands")));
      }
    }

    String doneTag = json.has("done_tag") ? json.get("done_tag").getAsString() : null;
    return new PhoneCallScript(
      id, caller, subtitle, avatar, accent, List.copyOf(pages),
      List.copyOf(choices), stringList(json, "on_complete"), doneTag);
  }

  private static String optString(JsonObject json, String key, String fallback) {
    return json.has(key) ? json.get(key).getAsString() : fallback;
  }

  private static List<String> stringList(JsonObject json, String key) {
    List<String> out = new ArrayList<>();
    if (json.has(key)) {
      JsonArray arr = json.getAsJsonArray(key);
      for (JsonElement el : arr) out.add(el.getAsString());
    }
    return List.copyOf(out);
  }
}
