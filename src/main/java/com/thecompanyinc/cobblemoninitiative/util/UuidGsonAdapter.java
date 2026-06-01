package com.thecompanyinc.cobblemoninitiative.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.UUID;

/** Shared GSON type adapter for serialising/deserialising {@link UUID} as a string. */
public class UuidGsonAdapter
  implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

  public static final UuidGsonAdapter INSTANCE = new UuidGsonAdapter();

  private UuidGsonAdapter() {}

  @Override
  public JsonElement serialize(UUID src, Type type, JsonSerializationContext ctx) {
    return new JsonPrimitive(src.toString());
  }

  @Override
  public UUID deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
    throws JsonParseException {
    try {
      return UUID.fromString(json.getAsString());
    } catch (IllegalArgumentException e) {
      throw new JsonParseException("Invalid UUID: " + json.getAsString(), e);
    }
  }
}
