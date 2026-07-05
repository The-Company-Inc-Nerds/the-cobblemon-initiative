# Easy NPC 6.25.0 — NPC Config Reference

**Scope:** every NPC config value + format Easy NPC 6.25.0 (Fabric, MC 1.21.1) accepts in a
hand-authored `.npc.snbt` preset. Enumerated by decompiling the PINNED jar
(`easy_npc-fabric-1.21.1-6.25.0.jar`) — the exact bytecode this modpack runs — NOT the
possibly-newer GitHub source. **Version is current:** 6.25.0 is the newest Easy NPC for
1.21.1 Fabric on Modrinth (published 2026-06-29) and the highest project-wide.

**How to read:** enum sections list every UPPERCASE constant (the exact SNBT string). Data
tables give `Tag key | type | default | meaning`. Most sub-tags are written ONLY when they
differ from default (`hasChanged()` gating), so a minimal preset omits everything at default.
Companion: `docs/ENGINE_FINDINGS.md` §2 (the load-bearing quirks — renderer species-only,
empty-ObjectiveDataSet brick, PlayerTagCondition ignores Operation, deferred CLOSE_DIALOG).
Repo authoring goes through `scripts/content_compile` (dialog-src → presets); this doc is the
underlying grammar that compiler targets.

> Generated 2026-07-05 from a 6-agent bytecode sweep of the jar's 35 `data/` packages.

---

## Table of contents

1. Top-Level Skeleton, Preset Metadata, Entity Types, Sound, Trading, Navigation
2. Visuals — Render, Model, Skin, Scale, Rotation
3. Dialog — DialogData, Entries, Buttons
4. Actions & Conditions — Events, Action Types, Condition Types
5. Objectives — Types, Goals, Fields
6. Attributes, Display, Profession, Progression

---

## Top-Level Skeleton, Preset Metadata, Entity Types, Sound, Trading, Navigation

All facts below are bytecode-verified against `easy_npc-fabric-1.21.1-6.25.0.jar`. SNBT value-type notation: `1b` = byte, `12` = int, `0L` = long, `0.75f` = float, `1.0d` = double, `"…"` = string, `[…]` = list, `{…}` = compound, `[I;a,b,c,d]` = int-array (UUID). Class citations use the simple class name.

### 1. Preset file skeleton

An authored `data/easy_npc/preset/<type>/<name>.npc.snbt` has a root compound with **exactly two keys**: `PresetMetadata` and `data`. The entity itself (type, variant, all Easy-NPC subsystems, and vanilla mob NBT) lives entirely inside `data`. (Verified by parsing all 190 project presets + the jar's bundled `data/easy_npc/default_preset/**`.)

| Root key | Type | Meaning |
|----------|------|---------|
| `PresetMetadata` | compound | Descriptive header for the preset browser (author, name, category…). Optional; every field defaults. Cite `PresetMetadata`. |
| `data` | compound | The full entity NBT (vanilla mob tags + all Easy NPC data compounds). This is what spawns the NPC. |

**Keys inside `data`** — Easy NPC subsystem tags (each written by a `*DataCapable` handler interface):

| Tag key | Type | Handler class | One-line meaning |
|---------|------|---------------|------------------|
| `id` | String | (vanilla entity id) | Entity-type resource location, e.g. `"easy_npc:humanoid"`; selects which NPC class instantiates. **Required.** |
| `EasyNPCVersion` | int | `ConfigDataCapable` | Easy NPC data-format version; always written as **3** in 6.25.0. Missing → treated as legacy (0). |
| `PresetUUID` | int[] (UUID) | `PresetData` | Stable preset identity, `[I;0,0,0,0]` when unset. |
| `VariantType` | String | `VariantDataCapable` | Skin/variant enum-constant name for this entity type (see §3). |
| `SkinData` | compound | `SkinDataCapable` | Skin source (type/url/uuid). *(skin subsystem)* |
| `RenderData` | compound | `RenderDataCapable` | Renderer override (`Type` = `RenderType`, `EntityModel`). *(render subsystem)* |
| `ModelData` | compound | `ModelDataCapable` | Pose/model config (`Pose` = `ModelPose`, `DefaultPose`). *(model subsystem)* |
| `DisplayAttribute` | list | `DisplayAttributeDataCapable` | Visibility/light/name-visibility flags. *(attribute subsystem)* |
| `EntityAttribute` | compound | (attribute subsystem) | Behavior flags (CanFloat, IsInvulnerable, NoGravity…). *(attribute subsystem)* |
| `attributes` | list | (vanilla) | Vanilla base-attribute modifier list `[{base:<d>,id:"…"}]`. |
| `DialogData` | compound | `DialogDataCapable` | Dialog set (`DialogDataSet`). *(dialog subsystem)* |
| `ActionData` | compound | `ActionEventDataCapable` | Action-event set (`ActionEventSet`, `ActionPermissionLevel`). *(action subsystem)* |
| `ObjectiveData` | compound | `ObjectiveDataCapable` | Objectives/goals (`ObjectiveDataSet`, `HasObjectives`…). *(objective subsystem)* |
| `SoundData` | compound | `SoundDataCapable` | Per-event sound overrides — see §4. |
| `TradingData` | compound | `TradingDataCapable` | Easy-NPC trading config (`TradingDataSet`) — see §5. |
| `Offers` | compound | `TradingDataCapable` / `TradingUtils` | Vanilla merchant offers (legacy nested wrapper) — see §5. |
| `Navigation` | compound | `NavigationDataCapable` | Home position — see §6. |
| `Profession` | String | `ProfessionDataCapable` | Villager profession enum name; default `"NONE"` (see §6). |
| `Progression` | compound | `ProgressionDataCapable` | Level/XP scaling: `AttributeScalingEnabled` (byte), `EntityExperience` (int), `EntityExperienceLevel` (int). |
| `Owner` | int[] (UUID) | `OwnerDataCapable` | Owning player UUID (`putUUID` int-array); omitted when unowned. |
| `Status` | compound | `StatusDataCapable` | Internal finalize/timestamp flags — see §6. |

**Vanilla `Mob`/`LivingEntity` passthrough** kept in `data` so the entity round-trips (present in every project preset): `CustomName` (String, usually a JSON text component), `CustomNameVisible` (byte), `Brain` (compound), `ArmorItems`/`ArmorDropChances`/`HandItems`/`HandDropChances` (lists), `CanPickUpLoot`, `LeftHanded`, `Invulnerable`, `PersistenceRequired`, `OnGround` (all byte).

**Position/runtime tags** (`Pos` [3×double], `Rotation` [2×float], `UUID` [int-array], `Motion`, `Air`, `Fire`, `FallDistance`, `HurtTime`, `HurtByTimestamp`, `DeathTime`, `AbsorptionAmount`) are normally **absent** from a preset — `PresetDataUtils.cleanupEntityData` strips them on export. `PresetDataUtils` constants: `POSITION_TAGS = {Pos, Rotation}`, `ENTITY_UUID_TAG = UUID`, `RUNTIME_STATE_TAGS = {AbsorptionAmount, Air, DeathTime, FallDistance, Fire, HurtByTimestamp, HurtTime, Motion, OnGround}`. `PresetDataUtils$CleanupMode` = **`RUNTIME_ONLY`**, **`FULL`** (FULL also drops position + UUID). Position is re-injected on spawn.

Minimal skeleton:

```snbt
{
  PresetMetadata: { author:"…", category:"NPC", created:0L, description:"…",
    entityTypeId:"easy_npc:humanoid", modified:0L, name:"…",
    variantType:"STEVE", version:"1.0.0" },
  data: {
    id: "easy_npc:humanoid",
    EasyNPCVersion: 3,
    PresetUUID: [I; 0, 0, 0, 0],
    VariantType: "STEVE",
    CustomName: '{"text":"…"}',
    CustomNameVisible: 1b,
    Navigation: { Home: { X:1526, Y:84, Z:2029 } },
    Profession: "NONE",
    Progression: { AttributeScalingEnabled:0b, EntityExperience:1, EntityExperienceLevel:1 },
    Status: { finalized:0b, npc_data_last_saved:0L, npc_data_last_update:0L },
    SkinData: {…}, RenderData: {…}, ModelData: {…},
    DisplayAttribute: [{…}], EntityAttribute: {…}, attributes: [{…}],
    DialogData: {…}, ActionData: {…}, ObjectiveData: {…},
    SoundData: { SoundDataSet: [] }
  }
}
```

### 2. PresetMetadata (`PresetMetadata`)

Every field is a `Codec.optionalFieldOf` with the default shown, so any/all may be omitted. Purely descriptive: `entityTypeId`/`variantType` here are informational; the authoritative type is `data.id` and `data.VariantType`.

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `name` | String | `"Unnamed Preset"` | Display name in the preset browser. |
| `category` | String | `"Custom"` | Grouping/folder label. |
| `version` | String | `"1.0.0"` | Free-form preset version. |
| `author` | String | `"Unknown"` | Author credit. |
| `created` | long | `0L` | Creation epoch-ms. |
| `modified` | long | `0L` | Last-modified epoch-ms. |
| `description` | String | `""` | Long description. |
| `entityTypeId` | String | `""` | Informational entity-type id (mirror of `data.id`). |
| `variantType` | String | `""` | Informational variant name (mirror of `data.VariantType`). |

Constants: `TAG_* ` = the exact keys above; `DEFAULT_NAME/CATEGORY/VERSION/AUTHOR/DESCRIPTION` as shown. Related enums: `PresetType` = **`CUSTOM`, `DATA`, `DEFAULT`, `LOCAL`, `WORLD`**; `PresetExportFormat` = **`NBT`, `SNBT`, `JSON`, `UNKNOWN`**.

### 3. Entity types & VariantType

`data.id` must be one of the registered Easy NPC entity types (namespace `easy_npc`). The `NPCType` interface (`RawNPCType`, `DefaultNPCType`) computes `getRegistryId()` = `name().toLowerCase(ROOT)`; `RawNPCType` additionally appends `"_raw"`.

**Standard entity types** — `easy_npc:<id>` where `<id>` is a `DefaultNPCType` registry id (registered by `ModNPCEntityType`; `GENERIC` is the internal abstract base, not placeable). All 38 `DefaultNPCType` constants:

`generic`, `allay`, `bogged`, `cat`, `cave_spider`, `chicken`, `creeper`, `drowned`, `enderman`, `evoker`, `fox`, `ghast`, `horse`, `humanoid`, `humanoid_slim`, `husk`, `illusioner`, `iron_golem`, `pig`, `piglin`, `piglin_brute`, `pillager`, `skeleton`, `skeleton_horse`, `slime`, `spider`, `stray`, `vex`, `villager`, `vindicator`, `witch`, `wither_skeleton`, `wolf`, `zombie`, `zombie_horse`, `zombie_villager`, `zombified_piglin`.

**Raw entity types** — `easy_npc:<id>_raw` (registered by `ModRawEntityType`). All 35 `RawNPCType` constants (id = lowercase name + `_raw`): `generic`, `allay`, `bogged`, `cat`, `creeper`, `chicken`, `drowned`, `enderman`, `evoker`, `fox`, `ghast`, `horse`, `iron_golem`, `illusioner`, `pathfinder_mob`, `humanoid`, `humanoid_slim`, `pillager`, `pig`, `piglin`, `piglin_brute`, `zombified_piglin`, `skeleton`, `stray`, `wither_skeleton`, `spider`, `slime`, `vex`, `villager`, `vindicator`, `wolf`, `witch`, `zombie`, `husk`, `zombie_villager` — each suffixed `_raw` (e.g. `easy_npc:humanoid_raw`).

**Custom entity types** (`ModCustomEntityType`): `easy_npc:doppler`, `easy_npc:fairy`, `easy_npc:orc`, `easy_npc:orc_warrior`.

**Cobblemon entity type** (`CobblemonEntityType`): `easy_npc:cobblemon_npc`.

**VariantType** is a per-entity-type enum whose constant *name* is stored in the `VariantType` string tag (and mirrored to `RenderData.EntityModel` for cobblemon). It equals the entity's Skin-Variant enum:

| Entity type | VariantType source | Constants |
|-------------|--------------------|-----------|
| `cobblemon_npc` | `CobblemonNPC$VariantType` | `COBBLEMON_NPC` |
| `doppler` | `Doppler$VariantType` | `DOPPLER` |
| `humanoid` / `humanoid_slim` | `HumanoidSkinVariant` | `ALEX`, `ARI`, `EFE`, `KAI`, `MAKENA`, `NOOR`, `STEVE`, `SUNNY`, `ZURI`, `JAYJASONBO`, `PROFESSOR_01`, `SECURITY_01`, `KNIGHT_01`, `KNIGHT_02` (project default `STEVE`) |
| other mobs | `<Mob>SkinVariant` | one constant per vanilla skin/type *(skin subsystem)* |

`RenderData.Type` uses `RenderType` = **`CUSTOM`, `DEFAULT`, `CUSTOM_ENTITY`, `COBBLEMON_ENTITY`**; cobblemon NPCs use `COBBLEMON_ENTITY` with `EntityModel:"cobblemon:<species>"`.

### 4. Sound (`SoundData` → `SoundDataSet` → `SoundDataEntry`)

Top-level `SoundData` is a compound holding one list, `SoundDataSet` (tag `"SoundDataSet"`, list of compounds; default `[]`). Cite `SoundDataSet`, `SoundDataEntry`.

**`SoundType` enum (19 constants)** — the sound event slot; stored in each entry's `Type`:

`AMBIENT`, `AMBIENT_STRAY`, `AMBIENT_TAMED`, `CAST_SPELL`, `CELEBRATE`, `DAMAGE`, `DEATH`, `DEFAULT`, `DRINKING`, `EAT`, `FALL_DAMAGE_BIG`, `FALL_DAMAGE_SMALL`, `HURT`, `PET`, `STEP`, `SWIM`, `TRADE`, `TRADE_YES`, `TRADE_NO`.

**`SoundDataEntry` fields:**

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `Type` | String | (required) | `SoundType` enum name; the event slot. |
| `Name` | String | (required) | Sound-event resource id, e.g. `"minecraft:entity.villager.trade"`. |
| `Volume` | float | `0.75f` | Playback volume. |
| `Pitch` | float | `1.0f` | Playback pitch. |
| `Enabled` | byte | `1b` (true) | Whether this override plays. Omitted entries default enabled. |

```snbt
SoundData: { SoundDataSet: [
  { Type:"DEATH", Name:"minecraft:entity.player.death" },
  { Type:"TRADE", Name:"minecraft:entity.villager.trade", Volume:0.75f, Pitch:1.0f, Enabled:1b }
] }
```

### 5. Trading (`TradingData` + `Offers`)

Two sibling tags under `data`. `TradingData` holds Easy NPC's config; `Offers` holds the actual vanilla merchant recipes.

**`TradingType` enum:** `ADVANCED`, `BASIC`, `CUSTOM`, `NONE`.
**`TradingValueType` enum:** `DEMAND`, `LAST_TRADING_RESET`, `MAX_USES`, `PRICE_MULTIPLIER`, `RESET_TRADING_EVERY_MIN`, `REWARD_EXP`.

**`TradingData` → `TradingDataSet` compound** (`TradingDataSet`):

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `Type` | String | `"NONE"` | `TradingType` name; `NONE` disables trading. |
| `MaxUses` | int | `64` | Uses applied per offer (BASIC/ADVANCED). |
| `RewardedXP` | int | `0` | XP granted per trade. |
| `ResetsEveryMin` | int | `0` | Stock-reset interval in minutes (0 = never). |
| `LastReset` | long | `0L` | Epoch-ms of last stock reset. |
| `OfferActions` | list | (absent) | Per-offer action bindings: `[{Index:<int>, ActionDataSet:{…}}]`. |

Constants: `DATA_TRADING_DATA_SET_TAG="TradingDataSet"`, `DATA_TYPE_TAG="Type"`, `DATA_TRADING_MAX_USES_TAG="MaxUses"`, `DATA_TRADING_REWARDED_XP_TAG="RewardedXP"`, `DATA_TRADING_RESETS_EVERY_MIN_TAG="ResetsEveryMin"`, `DATA_TRADING_LAST_RESET_TAG="LastReset"`, `DATA_OFFER_ACTIONS_TAG="OfferActions"`, `DATA_OFFER_ACTION_INDEX_TAG="Index"`. (`TradingSettings.BASIC_TRADING_OFFERS` / `ADVANCED_TRADING_OFFERS` are grid-slot counts, not NBT.)

**`Offers`** is written by Easy NPC via the vanilla `MerchantOffers` codec, but the loader (`TradingUtils.parseMerchantOffers` → `migrateLegacyOffersTag`) **accepts three shapes**, so authored presets use the legacy double-nested wrapper:
- double-nested (project style): `Offers:{ Recipes:{ Recipes:[ …offers… ] } }` (an `Inventory:{}` sibling is ignored),
- single-nested: `Offers:{ Recipes:[ … ] }`,
- modern flat list.

Each offer is a **vanilla `MerchantOffer`** compound:

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `buy` | compound (ItemCost) | (required) | First cost item: `{id:"…", count:<int>}`. |
| `buyB` | compound (ItemCost) | (optional) | Optional second cost item. |
| `sell` | compound (ItemStack) | (required) | Result item: `{id:"…", count:<int>}`. |
| `uses` | int | `0` | Times already used. |
| `maxUses` | int | `4` | Max uses before out of stock. |
| `rewardExp` | byte | `1b` (true) | Whether the trade grants XP (presets set `0b`). |
| `specialPrice` | int | `0` | Flat price adjustment. |
| `demand` | int | `0` | Demand accumulator (price scaling). |
| `priceMultiplier` | float | `0.0f` | Demand-based price multiplier. |
| `xp` | int | `1` | Merchant XP granted. |

```snbt
Offers: { Inventory: {}, Recipes: { Recipes: [
  {buy:{id:"minecraft:wheat",count:18}, sell:{id:"cobblemon:rare_candy",count:1},
   uses:0, maxUses:4, rewardExp:0b, specialPrice:0, demand:0, priceMultiplier:0.0f, xp:0}
] } },
TradingData: { TradingDataSet: { Type:"BASIC", MaxUses:4, ResetsEveryMin:525600, LastReset:0L } }
```

### 6. Navigation, Position, Status, Owner, Profession, Spawner

**`Navigation`** (`NavigationDataCapable`) — compound with one sub-tag `Home` (constants `DATA_NAVIGATION_TAG="Navigation"`, `DATA_HOME_TAG="Home"`). `Home` is a `BlockPos` written by `CompoundTagUtils.writeBlockPos` as three **int** keys:

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `X` | int | — | Home block X. |
| `Y` | int | — | Home block Y. |
| `Z` | int | — | Home block Z. |

```snbt
Navigation: { Home: { X:1526, Y:84, Z:2029 } }
```

**`CustomPosition`** (`data/position`) — record `{x,y,z}` **floats**, `DEFAULT = (0f,0f,0f)`; `save()` emits a `ListTag` of three floats `[x,y,z]`. Used for model root/part offsets, not a top-level preset tag.

**`Status`** (`StatusDataCapable`, `DATA_STATUS_DATA_TAG="Status"`) — internal bookkeeping. Keys are `StatusDataType.getTagName()` = the enum name lowercased. `StatusDataType` = `FINALIZED`, `NPC_DATA_LAST_UPDATE`, `NPC_DATA_LAST_SAVED`, each carrying a `StatusDataType$ValueType` of `BOOLEAN` or `TIMESTAMP`:

| Tag key | Type | Default | Meaning |
|---------|------|---------|---------|
| `finalized` | byte | `0b` | Whether initial setup completed (BOOLEAN). |
| `npc_data_last_update` | long | `0L` | Last data-change epoch-ms (TIMESTAMP). |
| `npc_data_last_saved` | long | `0L` | Last persistence epoch-ms (TIMESTAMP). |

The generic `data/type/ValueType` enum (used by dialog/objective typed values) = **`BOOLEAN`, `DOUBLE`, `INTEGER`, `STRING`**.

**`Owner`** (`OwnerDataCapable`, tag `"Owner"`) — player UUID via `putUUID`, i.e. an int-array `[I;a,b,c,d]`; omitted when the NPC has no owner.

**`Profession`** (`ProfessionDataCapable`, tag `"Profession"`) — string enum name, default `"NONE"`. `Profession` enum (15): `NONE`, `ARMORER`, `BUTCHER`, `CARTOGRAPHER`, `CLERIC`, `FARMER`, `FISHERMAN`, `FLETCHER`, `LEATHERWORKER`, `LIBRARIAN`, `MASON`, `NITWIT`, `SHEPHERD`, `TOOLSMITH`, `WEAPONSMITH`.

**Spawner (`data/spawner`)** — these belong to the Easy NPC **spawner block entity**, not the `.npc.snbt` preset. `SpawnerType` = `BOSS_SPAWNER`, `DEFAULT_SPAWNER`, `GROUP_SPAWNER`, `SINGLE_SPAWNER`. `SpawnerData` is a record of seven ints (`spawnDelay`, `minSpawnDelay`, `maxSpawnDelay`, `spawnCount`, `maxNearbyEntities`, `requiredPlayerRange`, `spawnRange`); `fromSpawnerType` seeds them from `SpawnerTypeConfig`:

| SpawnerType | delay | min | max | count | maxNearby | playerRange | spawnRange |
|-------------|-------|-----|-----|-------|-----------|-------------|------------|
| `DEFAULT_SPAWNER` | 600 | 400 | 1200 | 2 | 4 | 16 | 8 |
| `SINGLE_SPAWNER` | 600 | 3000 | 9000 | 1 | 1 | 8 | 4 |
| `GROUP_SPAWNER` | 600 | 3000 | 12000 | 3 | 6 | 12 | 6 |
| `BOSS_SPAWNER` | 600 | 6000 | 18000 | 1 | 1 | 32 | 16 |

*(Aside: `data/npc/NPCEntityMetadata`, `SavedNPCEntityEntry`, `UserDefinedConfiguration` are server-side registry/persistence records — tags `TAG_DIMENSION`, `TAG_OWNER`, `TAG_ENTITY_TYPE`, `TAG_PRESET_UUID` — and are not part of the preset SNBT.)*

---

## Visuals — Render, Model, Skin, Scale, Rotation

All visual data lives as peer compound/primitive keys inside an Easy NPC's serialized entity-data compound (the body of a `.npc.snbt` preset). The relevant top-level keys and their owning handler interfaces are:

| Top-level tag | Type | Handler class (`entity/easynpc/data/…`) | Section |
|---|---|---|---|
| `RenderData` | compound | `RenderDataCapable` | Render |
| `ModelData` | compound | `ModelDataCapable` (+ `ModelPosition/Root/Rotation/Scale/Visibility DataCapable`) | Model |
| `AnimationData` | compound | `ModelAnimationDataCapable` (written at NPC-data level, **not** inside `ModelData`) | Model |
| `SkinData` | compound | `SkinDataCapable` | Skin |
| `VariantType` | string | `VariantDataCapable` | Skin |

Serialization invariants that apply throughout: floats are written as NBT `FloatTag` (SNBT `f` suffix); vectors are stored as **lists of `FloatTag`** (not `int-array`/`double`); booleans use `putBoolean` → `ByteTag` (`0b`/`1b`); UUIDs use `putUUID` → `int-array` of 4 (`[I;…]`); most sub-tags are written **only when they differ from their default** (`hasChanged()` gating), so a minimal preset omits everything at default.

---

### Render — `RenderDataEntry`

#### `RenderType` enum (`data/render/RenderType`)

All constants: `CUSTOM`, `DEFAULT`, `CUSTOM_ENTITY`, `COBBLEMON_ENTITY`.
`RenderType.get(String)` returns `DEFAULT` for null / empty / unrecognised names (no exception).

#### `RenderData` compound fields (`data/render/RenderDataEntry`)

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Type` | string | `DEFAULT` | `RenderType.name()`. **Written only when ≠ `DEFAULT`.** Read via `RenderType.get()` → falls back to `DEFAULT`. |
| `EntityType` | string | *(absent → null)* | Registry id of the entity to render as (e.g. `minecraft:villager`). Written only when the type is non-null **and** vanilla-serializable (`EntityType.canSerialize()`). Read via `EntityType.byString().orElse(null)`. |
| `EntityModel` | string | *(absent → null)* | Free-form model/species identifier (for `COBBLEMON_ENTITY`, a Cobblemon species id, e.g. `pikachu`). Written only when non-null and non-empty. |

How the render types differ (semantics enforced by `withRenderEntityType`/`withRenderEntityModel`):

- `DEFAULT` — normal Easy NPC rendering from `ModelData` + `SkinData` + `VariantType`. `Type` tag is omitted.
- `CUSTOM_ENTITY` — render the NPC **as another registered entity**; `EntityType` supplies the id. Setting a non-null entity type forces `RenderType.CUSTOM_ENTITY` (null → `DEFAULT`).
- `COBBLEMON_ENTITY` — render the NPC **as a Cobblemon**; `EntityModel` supplies the species. Setting a non-empty model forces `RenderType.COBBLEMON_ENTITY`. (Per project findings, the Cobblemon renderer keys on species only.)
- `CUSTOM` — reserved for a fully custom renderer; carries neither `EntityType` nor `EntityModel` by itself.

```snbt
// render NPC as a Cobblemon species
RenderData: { Type: "COBBLEMON_ENTITY", EntityModel: "pikachu" }
// render NPC as a vanilla villager
RenderData: { Type: "CUSTOM_ENTITY", EntityType: "minecraft:villager" }
```

---

### Model — `ModelData` compound (`entity/easynpc/data/ModelDataCapable`)

Top-level fields of the `ModelData` compound:

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Pose` | string | `VANILLA` | `ModelPose.name()`. On write, if pose is a custom pose it is stored; if pose is `VANILLA` the literal `"VANILLA"` is written alongside `DefaultPose`. Read via `ModelPose.get()` → `VANILLA` if absent/blank. |
| `DefaultPose` | string | `STANDING` | Vanilla `net.minecraft.world.entity.Pose` (`class_4050`) name used when `Pose == VANILLA`. Read via `Pose.valueOf()`. |
| `PoseName` | string | *(absent)* | Optional named-pose label; written only when non-empty. |
| `Root` | compound | *(absent)* | Whole-entity root transform (`RootModelData`); written only when changed. See below. |
| `Position` | compound | *(absent)* | Per-part position overrides. See below. |
| `Rotation` | compound | *(absent)* | Per-part rotation overrides. See below. |
| `Scale` | compound | *(absent)* | Per-part scale overrides. See below. |
| `Visible` | compound | *(absent)* | Per-part visibility (only `false` entries stored). See below. |

Pose read/write logic (from `addAdditionalModelData` / `readAdditionalModelData`):
- Per-part `Position/Rotation/Scale/Visible/Root` are only serialized when `Pose != VANILLA` **and** the model actually changed (`hasChangedModel()`), i.e. only for `DEFAULT`/`CUSTOM` poses with real overrides.
- After reading, if `Pose == DEFAULT` and nothing changed, it is snapped back to `VANILLA`.

#### `ModelPose` enum (`data/model/ModelPose`)

All constants: `VANILLA`, `DEFAULT`, `CUSTOM`. `ModelPose.get()` → `VANILLA` for unknown/blank.
- `VANILLA` — entity uses its normal vanilla pose (given by `DefaultPose`) and AI-driven animation.
- `DEFAULT` — Easy NPC baseline model pose (arms driven by `ModelArmPose`).
- `CUSTOM` — apply the per-part `Position`/`Rotation`/`Scale`/`Visible` overrides.

#### Vanilla `Pose` enum for `DefaultPose` (`net.minecraft.class_4050`, 1.21.1)

All 18 constants (declaration order; `STANDING` is the default `field_18076`):
`STANDING`, `FALL_FLYING`, `SLEEPING`, `SWIMMING`, `SPIN_ATTACK`, `CROUCHING`, `LONG_JUMPING`, `DYING`, `CROAKING`, `USING_TONGUE`, `SITTING`, `ROARING`, `SNIFFING`, `EMERGING`, `DIGGING`, `SLIDING`, `SHOOTING`, `INHALING`.

#### `ModelPartType` enum and tag names (`data/model/ModelPartType`)

The per-part sub-compounds (`Position`/`Rotation`/`Scale`/`Visible`) are keyed by each part's CamelCase `getTagName()`:

| Enum constant | Tag-name key | Enum constant | Tag-name key |
|---|---|---|---|
| `ROOT` | `Root` | `LEFT_WING` | `LeftWing` |
| `HEAD` | `Head` | `RIGHT_LEG` | `RightLeg` |
| `HAT` | `Hat` | `LEFT_LEG` | `LeftLeg` |
| `HELMET` | `Helmet` | `LEGGINGS` | `Leggings` |
| `BODY` | `Body` | `BOOTS` | `Boots` |
| `CHESTPLATE` | `Chestplate` | `RIGHT_PANTS` | `RightPants` |
| `BODY_JACKET` | `BodyJacket` | `LEFT_PANTS` | `LeftPants` |
| `RIGHT_ARM` | `RightArm` | `RIGHT_FRONT_LEG` | `RightFrontLeg` |
| `LEFT_ARM` | `LeftArm` | `LEFT_FRONT_LEG` | `LeftFrontLeg` |
| `ARMS` | `Arms` | `RIGHT_HIND_LEG` | `RightHindLeg` |
| `RIGHT_SLEEVE` | `RightSleeve` | `LEFT_HIND_LEG` | `LeftHindLeg` |
| `LEFT_SLEEVE` | `LeftSleeve` | `TAIL` | `Tail` |
| `RIGHT_WING` | `RightWing` | `TAIL1` | `Tail1` |
| | | `TAIL2` | `Tail2` |
| | | `UNKNOWN` | `Unknown` |

#### Per-part sub-compounds

Each maps `ModelPartType.getTagName()` → a value; only non-default parts are written (`CompoundTagUtils.putIfNotEmpty` drops empty sub-compounds).

| Sub-compound | Value class | Value encoding | Per-part default | Written when |
|---|---|---|---|---|
| `Position` | `CustomPosition` (`data/position`) | FloatList `[x, y, z]` | `(0,0,0)` | position `hasChanged()` |
| `Rotation` | `CustomRotation` (`data/rotation`) | FloatList `[x, y, z, locked]` (`locked` = `1.0f`/`0.0f`, index 3) | `(0,0,0, false)` | rotation `hasChanged()` |
| `Scale` | `CustomScale` (`data/scale`) | FloatList `[x, y, z]` | `(1,1,1)` | scale `hasChanged()` |
| `Visible` | boolean | `ByteTag` (`0b`) — **only invisible parts are written**; absence ⇒ visible | `true` | part hidden |

`CustomRotation` / `CustomScale` / `CustomPosition` are lenient on read: missing list entries fall back to the axis default (0 for rotation/position, 1 for scale); shorter lists don't error.

#### `Root` sub-compound — `RootModelData` (`data/model/RootModelData`)

Whole-entity root transform. `RootModelData.DEFAULT` = rotation `(0,0,0,false)` + scale `(1,1,1)`.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Rotation` | FloatList `[x,y,z,locked]` | `[0,0,0,0]` | `CustomRotation` for the whole model; **always written** when the `Root` compound is emitted. |
| `Scale` | FloatList `[x,y,z]` | `[1,1,1]` | `CustomScale` for the whole model; written only when scale changed. |

```snbt
ModelData: {
  Pose: "CUSTOM",
  DefaultPose: "STANDING",
  Root:     { Rotation: [0.0f,0.0f,0.0f,0.0f], Scale: [1.0f,1.0f,1.0f] },
  Rotation: { Head: [0.2f,0.0f,0.0f,0.0f] },     // tilt head, unlocked
  Scale:    { RightArm: [1.0f,1.5f,1.0f] },
  Visible:  { Hat: 0b }                            // hide hat layer
}
```

#### `ModelType` enum (`data/model/ModelType`) — not serialized

Derived per entity (default `HUMANOID` via `getModelType()`); not a preset NBT field. All constants:
`ALLAY`, `AVIAN`, `CANINE`, `CREEPER`, `EQUINE`, `FELINE`, `GOLEM`, `ENDERMAN`, `HUMANOID`, `ILLAGER`, `PIXIE`, `QUADRUPED`, `SPIDER`, `SLIME`, `GHAST`, `VILLAGER`, `ZOMBIE`.

#### `ModelArmPose` enum (`data/model/ModelArmPose`) — not serialized in presets

Render-time arm posture (no `ModelArmPose` reference exists in any `entity/easynpc/data` serializer). All constants:
`ATTACKING`, `ATTACKING_WITH_MELEE_WEAPON`, `BOW_AND_ARROW`, `CELEBRATING`, `CROSSBOW_CHARGE`, `CROSSBOW_HOLD`, `CROSSED`, `CUSTOM`, `DANCING`, `DEFAULT`, `GUN_HOLD`, `NEUTRAL`, `SPELLCASTING`, `SPYGLASS`.

#### `ModelScaleAxis` enum (`data/model/ModelScaleAxis`) — GUI helper, not serialized

Constants: `X`, `Y`, `Z`.

#### `ItemAttachmentPoint` (`data/model/ItemAttachmentPoint`) — render helper, not preset NBT

A `Record(attachPart: ModelPartType, offsetX/Y/Z, rotX/Y/Z, scale: float)` with factory helpers `mouth/wing/arm/body` and sentinel `NONE`. Used at render time to place held items; not written to preset SNBT.

#### `AnimationData` compound — `ModelAnimationData` (`data/model/ModelAnimationData`)

Written at the NPC-data level (a sibling of `ModelData`), only when changed. `ModelAnimationData.DEFAULT` behavior = `SMART`.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Behavior` | string | `SMART` | `ModelAnimationBehavior.name()`; read via `ModelAnimationBehavior.get()` → `SMART` if absent. |

`ModelAnimationBehavior` enum (`data/model/ModelAnimationBehavior`): `SMART`, `DEFAULT`, `NONE`.

> The `data/animation/AnimationData` family (`AnimationData`, `AnimationData$Animation`, `AnimationData$Bone`, `AnimationDataReader`) models Bedrock/geometry-style keyframe animations loaded from JSON, not from the NBT preset — the only animation value carried in a preset is the `AnimationData.Behavior` string above.

```snbt
AnimationData: { Behavior: "SMART" }
```

---

### Skin — `SkinData` + `VariantType`

#### `SkinType` enum (`data/skin/SkinType`)

All constants: `CUSTOM`, `DEFAULT`, `INSECURE_REMOTE_URL`, `NONE`, `PLAYER_SKIN`, `SERVER_SKIN`, `SECURE_REMOTE_URL`.
`SkinType.get()` → `DEFAULT` for unknown/blank.
- `DEFAULT` — use the built-in model skin selected by `VariantType`.
- `NONE` — no skin.
- `PLAYER_SKIN` — Mojang player skin resolved from `Name` + player `UUID`.
- `SECURE_REMOTE_URL` / `INSECURE_REMOTE_URL` — skin fetched from `URL` (secure = validated https via `UrlValidator`; picked automatically by `createRemoteSkin`).
- `CUSTOM` — locally-uploaded texture referenced by `UUID` (+ optional `Content`).
- `SERVER_SKIN` — server-hosted skin.

#### `SkinData` compound fields (`data/skin/SkinDataEntry`)

All fields are written **unconditionally** by `write()` (no `hasChanged` gating). Empty-constructor defaults shown.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Name` | string | `""` | Player name (`PLAYER_SKIN`) or skin label. Read → `""` if absent. |
| `Type` | string | `DEFAULT` | `SkinType.name()`; read via `SkinType.get()`. |
| `URL` | string | `""` | Remote skin URL (`*_REMOTE_URL`). |
| `UUID` | int-array (`putUUID`) | `0-0-0-0` (`Constants.BLANK_UUID`) | Skin identity: player UUID or custom-texture id. Read via `getUUID` → BLANK_UUID if absent. |
| `DisableLayers` | boolean (`ByteTag`) | `false` (`0b`) | Suppress the outer (hat/jacket) skin layers. |
| `Content` | string | `""` | Base64 texture payload for `CUSTOM` skins. |
| `Timestamp` | long | `System.currentTimeMillis()` | Skin cache timestamp. |

```snbt
// player skin
SkinData: { Type: "PLAYER_SKIN", Name: "jeb_", URL: "", UUID: [I;853137608,2109412289,-1607837786,-1607837786], DisableLayers: 0b, Content: "", Timestamp: 1700000000000L }
// default built-in skin (variant chosen by VariantType)
SkinData: { Type: "DEFAULT", Name: "", URL: "", UUID: [I;0,0,0,0], DisableLayers: 0b, Content: "", Timestamp: 0L }
```

#### `VariantType` (`entity/easynpc/data/VariantDataCapable`)

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `VariantType` | string | model-specific (`getDefaultSkinVariantType()`) | Name of the active built-in skin-variant enum constant (e.g. `STEVE`, `PLAINS_FARMER`). Selects the texture when `SkinType` is `DEFAULT`/`NONE`. Unknown names log `Unknown variant {} for {}`. |

#### `SkinModel` enum (`data/skin/SkinModel`) — categorizer, not a preset field

Referenced by `SkinDataCapable` to pick the applicable variant set; not written as its own NBT tag. All constants:
`ALLAY`, `BOGGED`, `CAT`, `CHICKEN`, `CREEPER`, `FAIRY`, `FOX`, `DROWNED`, `EVOKER`, `ENDER_MAN`, `GHAST`, `HORSE`, `HUMANOID_SLIM`, `HUMANOID`, `HUSK`, `ILLAGER`, `ILLUSIONER`, `IRON_GOLEM`, `ORC`, `PIG`, `PIGLIN`, `PIGLIN_BRUTE`, `PILLAGER`, `PLAYER`, `SKELETON`, `SLIME`, `SPIDER`, `VEX`, `VILLAGER`, `VINDICATOR`, `WITCH`, `WOLF`, `ZOMBIE_VILLAGER`, `ZOMBIE`, `ZOMBIFIED_PIGLIN`, `STRAY`, `WITHER_SKELETON`.

#### The 26 skin-variant enums (`data/skin/variant/*SkinVariant`)

Each implements `api.skin.VariantTexture` and enumerates the built-in textures for one model family; a constant's `name()` is what goes in `VariantType`. Full constant lists:

| Variant enum | Constants |
|---|---|
| `AllaySkinVariant` | `ALLAY`, `LAVA`, `GRASSLAND`, `WATER` |
| `CatSkinVariant` | `ALL_BLACK`, `BLACK`, `BRITISH_SHORTHAIR`, `CALICO`, `JELLIE`, `OCELOT`, `PERSIAN`, `RAGDOLL`, `RED`, `SIAMESE`, `TABBY`, `WHITE` |
| `ChickenSkinVariant` | `WHITE` |
| `CreeperSkinVariant` | `CREEPER`, `CHARGED` |
| `DopplerSkinVariant` | `DOPPLER` |
| `EnderManSkinVariant` | `ENDERMAN` |
| `FairySkinVariant` | `GREEN`, `RED`, `BLUE` |
| `FoxSkinVariant` | `RED`, `SNOW` |
| `GhastSkinVariant` | `GHAST`, `GHAST_SHOOTING` |
| `HorseSkinVariant` | `WHITE`, `WHITE_SADDLED`, `CREAMY`, `CREAMY_SADDLED`, `CHESTNUT`, `CHESTNUT_SADDLED`, `BROWN`, `BROWN_SADDLED`, `BLACK`, `BLACK_SADDLED`, `GRAY`, `GRAY_SADDLED`, `DARKBROWN`, `DARKBROWN_SADDLED`, `SKELETON`, `SKELETON_SADDLED`, `ZOMBIE`, `ZOMBIE_SADDLED`, `DONKEY`, `DONKEY_SADDLED`, `MULE`, `MULE_SADDLED` |
| `HumanoidSkinVariant` | `ALEX`, `ARI`, `EFE`, `KAI`, `MAKENA`, `NOOR`, `STEVE`, `SUNNY`, `ZURI`, `JAYJASONBO`, `PROFESSOR_01`, `SECURITY_01`, `KNIGHT_01`, `KNIGHT_02` |
| `HumanoidSlimSkinVariant` | `ALEX`, `ARI`, `EFE`, `KAI`, `MAKENA`, `NOOR`, `STEVE`, `SUNNY`, `ZURI`, `KAWORRU` |
| `IllagerSkinVariant` | `EVOKER`, `EVOKER_CROSSED_ARMS`, `ILLUSIONER`, `ILLUSIONER_CROSSED_ARMS`, `PILLAGER`, `VINDICATOR`, `VINDICATOR_CROSSED_ARMS` |
| `IronGolemSkinVariant` | `IRON_GOLEM`, `IRON_GOLEM_CRACKINESS_HIGH`, `IRON_GOLEM_CRACKINESS_MEDIUM`, `IRON_GOLEM_CRACKINESS_LOW` |
| `OrcSkinVariant` | `ORC`, `ORC_WARRIOR` |
| `PigSkinVariant` | `PIG`, `SPOTTED` |
| `PiglinSkinVariant` | `PIGLIN`, `PIGLIN_BRUTE`, `ZOMBIFIED_PIGLIN` |
| `SkeletonSkinVariant` | `SKELETON`, `STRAY`, `WITHER_SKELETON`, `BOGGED` |
| `SlimeSkinVariant` | `SLIME` |
| `SpiderSkinVariant` | `CAVE_SPIDER`, `SPIDER` |
| `VexSkinVariant` | `VEX`, `CHARGED` |
| `VillagerSkinVariant` | biome × profession grid: `{DESERT,JUNGLE,PLAINS,SAVANNA,SNOW,SWAMP,TAIGA}_{ARMORER,BUTCHER,CARTOGRAPHER,CLERIC,FARMER,FISHERMAN,FLETCHER,LEATHERWORKER,LIBRARIAN,MASON,NITWIT,NONE,SHEPHERD,TOOLSMITH,WEAPONSMITH}` + `DEFAULT` (106 total) |
| `WitchSkinVariant` | `WITCH` |
| `WolfSkinVariant` | `WOLF`, `TAMED`, `ANGRY` |
| `ZombieSkinVariant` | `ZOMBIE`, `HUSK`, `DROWNED` |
| `ZombieVillagerSkinVariant` | same biome × profession grid as `VillagerSkinVariant` + `DEFAULT` (106 total) |

> Note: `HorseSkinVariant`'s `_SADDLED` and `IllagerSkinVariant`'s `_CROSSED_ARMS` suffixes are appended/handled by `VariantDataCapable` (`hasVariantTypeSaddled` / `hasVariantTypeCrossedArms`).

---

### Scale, Rotation & Position value records

These three records back both the per-part model sub-compounds and (for scale/rotation) the `Root` transform. All serialize as **FloatTag lists**.

#### `CustomScale` (`data/scale/CustomScale`)

Record `(x, y, z: float)`. `DEFAULT = (1.0, 1.0, 1.0)`. `save()` → `[x, y, z]`. Missing entries default to `1.0`.

#### `CustomRotation` (`data/rotation/CustomRotation`)

Record `(x, y, z: float, locked: boolean)`. `DEFAULT = (0.0, 0.0, 0.0, false)`. `save()` → `[x, y, z, locked?1.0:0.0]` (4 floats; index 3 is the lock flag). Missing entries default to `0.0`; index 3 == `1.0` ⇒ `locked`.

#### `CustomPosition` (`data/position/CustomPosition`)

Record `(x, y, z: float)`. `DEFAULT = (0.0, 0.0, 0.0)`. `save()` → `[x, y, z]`. Missing entries default to `0.0`.

| Record | SNBT list format | Default list |
|---|---|---|
| `CustomPosition` | `[x f, y f, z f]` | `[0.0f,0.0f,0.0f]` |
| `CustomRotation` | `[x f, y f, z f, locked f]` | `[0.0f,0.0f,0.0f,0.0f]` |
| `CustomScale` | `[x f, y f, z f]` | `[1.0f,1.0f,1.0f]` |

---

### Texture — `TextureFailureType` (`data/texture`)

Runtime-only diagnostics for remote/custom skin loading; **not serialized into presets** (`TextureFailureType` + `TextureFailureInfo` are used by the skin download/cache pipeline). Enum constants:
`INVALID_IMAGE_SIZE`, `DECODING_ERROR`, `INVALID_FORMAT`, `FILE_TOO_LARGE`, `NETWORK_ERROR`, `HTTP_CLIENT_ERROR`, `URL_INVALID`, `TIMEOUT`, `MAX_RETRIES_EXCEEDED`.

---

## Dialog — DialogData, Entries, Buttons

*All facts below are bytecode-verified against `easy_npc-fabric-1.21.1-6.25.0.jar`, package `de.markusbordihn.easynpc.data.dialog` (14 classes). Tag-key strings are the exact `ldc` String constants passed to `CompoundTag` get/put calls; enum spellings are the exact constant names. All UUIDs and the `Id` of a dialog/button/text are **derived, never stored** — do not write them.*

### Wrapper: the `DialogData` compound

The whole subsystem serializes into one compound whose canonical key in an Easy NPC preset is `DialogData`. `DialogDataSet.load(tag)` reads two keys off that compound and **returns early (no-op) if the `DialogDataSet` key is absent** — so the list key must exist for the NPC to have any dialog.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `DialogDataSet` | list&lt;compound&gt; (NBT id 10) | *(required)* | Ordered list of `DialogDataEntry` compounds. |
| `Type` | string | `STANDARD` | `DialogType` name; auto-normalized on save (see below). |

```snbt
DialogData: {
    Type: "STANDARD",
    DialogDataSet: [
        { Name: "Greeting", Label: "default", Texts: [ {Text: "Hello, @initiator."} ] }
    ]
}
```
*Class: `DialogDataSet` (`DATA_DIALOG_DATA_SET_TAG="DialogDataSet"`, `DATA_TYPE_TAG="Type"`).*

### `DialogType` enum

All constants (ordinal order), class `DialogType`:

| Constant | Meaning |
|---|---|
| `STANDARD` | Multi-dialog set; any number of entries. Default for `new DialogDataSet()`. |
| `BASIC` | Single-text NPC. Valid only when the set has ≤ 1 dialog. |
| `YES_NO` | Question + branch set. Valid only when the set has ≤ 3 dialogs. |
| `CUSTOM` | Reserved for non-STANDARD/BASIC/YES_NO shapes. |
| `NONE` | No dialog. Assigned automatically when the set is empty. |

`DialogType.get(String)` returns **`NONE`** for null/empty/unknown. `DialogDataSet` is deserialized with `DialogType.valueOf` (exact spelling required; missing `Type` ⇒ stays `STANDARD`).

**Save-time normalization (`DialogDataSet.save`) — the written `Type` is corrected, so an authored `Type` is only a hint:**
- `BASIC` with &gt; 1 dialog ⇒ rewritten `STANDARD`.
- `YES_NO` with &gt; 3 dialogs ⇒ rewritten `STANDARD`.
- empty set ⇒ `NONE`.
- otherwise if `Type` is not one of `BASIC`/`YES_NO`/`STANDARD` ⇒ `CUSTOM`.

### `DialogDataSet` structure & dialog selection

Internally holds `dialogByLabelMap` (`Label → entry`) and `dialogByIdMap` (`UUID → entry`); the serialized form is only the `DialogDataSet` list. On `addDialog` an entry is **rejected** (logged, dropped) if its id, label, or text is null/empty; a duplicate id overwrites.

There is **no stored "default-dialog id" pointer**. The entry point is chosen at runtime by `getNextAvailableDialog(player[, npc])`:
1. keep entries with `Priority >= 0` (so `Priority: -1` = `MANUAL_ONLY` dialogs are **never auto-opened** — reachable only via an `OPEN_NAMED_DIALOG`/`OPEN_DIALOG` button action);
2. keep entries whose `Conditions` all pass (no conditions ⇒ always allowed; no player context ⇒ allowed by default);
3. sort by `Priority` **descending**, ties broken by `Label` ascending; take the first.

By convention the fallback dialog uses `Label: "default"`. Its derived id is fixed: **`c21f969b-5f03-333d-83e0-4f8f136e7682`** (`UUID.nameUUIDFromBytes("default")`), usable as the target of an OPEN_DIALOG-by-UUID action.

### `DialogDataEntry` fields

Class `DialogDataEntry`. `id` = `UUID.nameUUIDFromBytes(label.getBytes())` (MD5/name-based, **not stored**). `MAX_DIALOG_LABEL_LENGTH = 32`.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Name` | string | `""` (read via getString) | Display/author name. Used to derive `Label` when `Label` is absent. |
| `Label` | string | derived from `Name` | Stable key (see normalization). On **save it is omitted** when it already equals `generateDialogLabel(Name)`; write it explicitly when you need a label that differs from the name. |
| `Text` | string | — | Single-line dialog text. **Alternative** to `Texts`; on load, `Texts` is checked first, else `Text`. Re-serialized as `Texts`. |
| `Texts` | list&lt;compound&gt; (id 10) | — | One or more `DialogTextData` compounds (`{Text:"..."}`); when &gt; 1 a line is chosen at **random** each open. |
| `Buttons` | list&lt;compound&gt; (id 10) | *(none)* | `DialogButtonEntry` compounds. |
| `Conditions` | list&lt;compound&gt; (id 10) | *(none)* | `ConditionDataEntry` compounds; invalid ones are dropped on load and save. All must pass to open the dialog. |
| `Priority` | int | `DialogPriority.calculateDefaultPriority(Label)` | Selection weight; clamped to `>= -1` via setter. **Omitted on save** when equal to the label-derived default. |
| `Options` | compound | *(none → `DialogOptionsData.DEFAULT`)* | `DialogOptionsData`; **omitted on save** when equal to default. |

Label normalization (`DialogUtils.generateDialogLabel`/`generateButtonLabel`): `trim → lowercase(ROOT) → spaces to "_" → strip everything not matching [a-z0-9_] → truncate to 32`. Empty input yields a random `dialog…`/`button…` label. This is why authored labels must already be lowercase snake_case to round-trip.

### `DialogTextData` (each `Texts` element / the `Text` value)

Class `DialogTextData` (record). Only one serialized key:

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Text` | string | `""` | The line. Written trimmed. |

`id` = `UUIDUtils.textToUUID(text)` (SHA-256, not stored). `isTranslationKey` is **auto-detected** from the text (not a stored tag) — see macros/translation section. Multiple `DialogTextData` in one entry = random line pool.

### `DialogButtonEntry` fields

Class `DialogButtonEntry` (record). `id` = `UUIDUtils.textToUUID(label)` (SHA-256, **not stored**). `MAX_BUTTON_LABEL_LENGTH = 32`. Note: the button's **display text is `Name`** — there is no separate "ButtonName" tag.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `Name` | string | `""` | Button caption (or a translation key — auto-detected). Written trimmed. |
| `Label` | string | `generateButtonLabel(Name)` | Stable key used by actions/`getDialogButton`. **Omitted on save** when equal to `generateButtonLabel(Name)`. |
| `Type` | string (`DialogButtonType`) | `DEFAULT` | See enum. **Omitted on save** when `DEFAULT`. Loaded via `DialogButtonType.get`. |
| `Actions` | compound | empty `ActionDataSet` | The button's `ActionDataSet`, written via `ActionDataSet.save(tag,"Actions")`. This — not `Type` — drives what the button does. (Action tree is documented in the Action subsystem.) |
| `Conditions` | list&lt;compound&gt; (id 10) | *(none)* | `ConditionDataEntry` list; invalid entries dropped. Only written when non-empty. Gate button visibility/enabled state per `ButtonConditionMode`. |

```snbt
Buttons: [
    { Name: "Yes", Actions: { ActionData: [ {Type:"OPEN_NAMED_DIALOG", Command:"yes_answer"} ] } },
    { Name: "Leave", Type: "CLOSE" }
]
```

### `DialogButtonType` enum

All constants, class `DialogButtonType`: `ACTION`, `CLOSE`, `CUSTOM`, `DEFAULT`. `get(String)` returns **`DEFAULT`** for null/empty/unknown. In 6.25.0 no class **outside the dialog package** (except a gametest helper) branches on this value — button behaviour comes from the `Actions` set, so `Type` is effectively a UI/legacy category. Keep it `DEFAULT` (omit it) unless you have a specific reason.

### `DialogOptionsData` (the `Options` compound)

Class `DialogOptionsData` (record). Every key is **written only when it differs from the runtime default**; a fully-default `Options` compound is omitted entirely. Defaults come from `DialogOptionsConfig` (verified: all three booleans `true`, mode `LOCK`).

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `AllowEscClose` | byte(bool) | `true` | Allow ESC to close the dialog. |
| `ShowCloseButton` | byte(bool) | `true` | Render the built-in close button. |
| `DisplayAvatar` | byte(bool) | `true` | Show the NPC avatar in the dialog. |
| `AvatarTop` | int | *(unset/null)* | Avatar Y offset; absent ⇒ engine default. |
| `AvatarLeft` | int | *(unset/null)* | Avatar X offset; absent ⇒ engine default. |
| `AvatarScale` | int | *(unset/null)* | Avatar scale; absent ⇒ engine default. |
| `ButtonConditionMode` | string (`DialogButtonConditionMode`) | `LOCK` | How condition-gated buttons behave when their conditions fail. |

`DialogOptionsData.DEFAULT` = `(true, true, true, null, null, null, LOCK)`.

### `DialogButtonConditionMode` enum

All constants, class `DialogButtonConditionMode`: `LOCK`, `HIDE`. `get(String)` returns **`LOCK`** for null/blank/unknown. `LOCK` = button stays visible but locked/disabled when its `Conditions` fail; `HIDE` = button is hidden when its `Conditions` fail.

### `DialogPriority` constants

Class `DialogPriority` (int constants, **not an enum** — write the integer, not the name):

| Constant | Value |
|---|---|
| `MANUAL_ONLY` | `-1` |
| `FALLBACK` | `0` |
| `LOW` | `1` |
| `NORMAL` | `5` |
| `HIGH` | `10` |
| `CRITICAL` | `100` |

`Priority: -1` excludes a dialog from auto-selection (openable only by explicit action). Higher = preferred by `getNextAvailableDialog`. `calculateDefaultPriority(Label)` supplies the value when `Priority` is absent, and equal-to-default values are omitted on save.

### Text macros, `@score`, and translation keys (`DialogUtils`)

Dialog `Text` and button `Name` are stored verbatim; substitution happens at **display time** in `DialogUtils.parseDialogText`, only when the string contains a macro (`hasDialogMacros`). Order applied:

| Token | Replaced with |
|---|---|
| `@npc` | the NPC's display name (`livingEntity.getName()`). |
| `@initiator` | the interacting player's name (`player.getName()`). |
| `@score(<objective>)` | the scoreboard score for `<objective>` (regex `@score\(([a-zA-Z0-9_.-]+)\)`), requires `ScoreboardData` context. |

After macro replacement, `TextFormattingCodes.parseTextLineBreaks` then `…parseTextFormattingCodes` run (so `\n` line breaks and `&`/`§` color codes are honored).

**Translation-key auto-detection** (no `Translate`/`isTranslationKey` tag exists): a `Text`/button `Name` is rendered as a `Component.translatable` iff it matches `TextUtils.TRANSLATION_KEY_PATTERN = ^[\w-]+(?:\.[\w-]+)*\.[\w-]+$` — i.e. a dotted key such as `dialog.mynpc.greeting` (word chars, `-`, at least one `.`, no spaces). Any string with spaces is treated as literal text. This flag is recomputed on every load; you never author it.

### Not serialized (runtime only)

- **`DialogMetaData`** (record: `livingEntity`, `player`, `scoreboardData`) — the per-interaction context passed to `parseDialogText`; never written to NBT.
- **`DialogScreenLayout`** (client enum, computed by `DialogUtils.getDialogScreenLayout` from text length + button count; not stored). Constants: `UNKNOWN`, `COMPACT_TEXT_ONLY`, `COMPACT_TEXT_WITH_ONE_BUTTON`, `COMPACT_TEXT_WITH_TWO_BUTTONS`, `COMPACT_TEXT_WITH_TWO_LARGE_BUTTONS`, `COMPACT_TEXT_WITH_THREE_BUTTONS`, `COMPACT_TEXT_WITH_FOUR_BUTTONS`, `COMPACT_TEXT_WITH_FIVE_BUTTONS`, `COMPACT_TEXT_WITH_SIX_BUTTONS`, `TEXT_ONLY`, `TEXT_WITH_ONE_BUTTON`, `TEXT_WITH_TWO_BUTTONS`, `TEXT_WITH_THREE_BUTTONS`, `TEXT_WITH_FOUR_BUTTONS`, `TEXT_WITH_FIVE_BUTTONS`, `TEXT_WITH_SIX_BUTTONS`.
- **`DialogDataManager`** — server registry/dispatch; carries no preset tags.

### Reference SNBT (STANDARD, condition-gated, branching)

```snbt
DialogData: {
    Type: "STANDARD",
    DialogDataSet: [
        {
            Name: "Fragment 10",
            Label: "frag_10",
            Priority: 100,
            Conditions: [ {Type: "PLAYER_TAG", Operation: "EQUALS", Name: "memory_fragment_10"} ],
            Texts: [ {Text: "There is one signature you have not let yourself read, @initiator."} ]
        },
        {
            Name: "Default",
            Label: "default",
            Texts: [ {Text: "The @npc has nothing more to say."} ],
            Buttons: [ {Name: "Leave", Type: "CLOSE"} ],
            Options: { DisplayAvatar: 1b, AllowEscClose: 1b }
        }
    ]
}
```

---

## Actions & Conditions — Events, Action Types, Condition Types

All facts below are bytecode-verified from `easy_npc-fabric-1.21.1-6.25.0.jar`, packages `de.markusbordihn.easynpc.data.{action,condition,execution}`. Tag-key strings are the exact `ldc` constants passed to `CompoundTag` get/put calls, i.e. the literal SNBT keys.

### Storage layout (how these tags nest)

An NPC's action bindings live in one root compound `ActionEventSet` (class `ActionEventSet`). Inside it, **each key is an `ActionEventType` name** (e.g. `ON_INTERACTION`) whose value is a **List of Compound** — the ordered `ActionDataEntry` list (class `ActionDataSet`). Empty event lists are omitted on save; only events with at least one *valid, non-empty* entry are written.

```snbt
ActionEventSet: {
  ON_INTERACTION: [
    { Id: [I; ...], Type: "OPEN_NAMED_DIALOG", Cmd: "intro" }
  ],
  ON_DEATH: [
    { Id: [I; ...], Type: "COMMAND", Cmd: "say goodbye", ExecAsUser: 0b, PermLevel: 4 }
  ]
}
```

Enum-parse rule (applies to `ActionEventType.get`, `ActionDataType.get`, `ConditionType.get`, `ConditionOperationType.get`): the string is fed to `Enum.valueOf`, so it must be the **exact UPPERCASE** constant spelling. `null`, empty, or any unrecognised value falls back to that enum's `NONE` constant (no exception). SubType parsing (`ConditionType.getSubType`) also matches on exact `Enum.name()`; no match → `null`.

---

### `ActionEventType` — event triggers (class `ActionEventType`)

All 13 constants (ordinal order). These are the valid keys inside the `ActionEventSet` compound.

| Constant | Meaning / when the entry list fires |
|----------|-------------------------------------|
| `NONE` | Placeholder / unset. Never a real trigger. |
| `ON_BUTTON_CLICK` | A dialog button bound to actions was clicked. |
| `ON_CLOSE_DIALOG` | A dialog screen was closed. |
| `ON_DEATH` | The NPC died. |
| `ON_DISTANCE_CLOSE` | A player entered the **8.0**-block radius (see distance bands). |
| `ON_DISTANCE_NEAR` | A player entered the **16.0**-block radius. |
| `ON_DISTANCE_TOUCH` | A player entered the **1.25**-block radius. |
| `ON_DISTANCE_VERY_CLOSE` | A player entered the **4.0**-block radius. |
| `ON_HURT` | The NPC took damage. |
| `ON_INTERACTION` | A player right-clicked / interacted with the NPC. |
| `ON_KILL` | The NPC killed an entity. |
| `ON_OPEN_DIALOG` | A dialog screen was opened. |
| `ON_TRADE` | A trade completed at the trading screen. |

**Distance bands** are hard-coded radii (verified in `entity.easynpc.handlers.ActionHandler`, `getPlayersInRange(Double)`); they are **not** per-action fields — you pick the band by which `ON_DISTANCE_*` slot holds the entry:

| Event | Radius (blocks) |
|-------|-----------------|
| `ON_DISTANCE_NEAR` | 16.0 |
| `ON_DISTANCE_CLOSE` | 8.0 |
| `ON_DISTANCE_VERY_CLOSE` | 4.0 |
| `ON_DISTANCE_TOUCH` | 1.25 |

`ActionGroup` (class `ActionGroup`) — runtime-only per-player de-duplication set for distance events (`NONE`, `DISTANCE_NEAR`, `DISTANCE_CLOSE`, `DISTANCE_VERY_CLOSE`, `DISTANCE_TOUCH`). Not persisted to SNBT.

---

### `ActionDataType` — action kinds (class `ActionDataType`)

All 9 constants (ordinal order). Written/read as the `Type` string on each `ActionDataEntry`. `requiresArgument` = whether the `Cmd` field carries a mandatory payload (verified: 2-arg ctor defaults it `true`; 3-arg ctor sets it explicitly).

| Constant | `requiresArgument` | `Cmd` payload / effect |
|----------|--------------------|------------------------|
| `NONE` | true* | Placeholder / unset. |
| `COMMAND` | true | `Cmd` = the command line to run (see `ExecAsUser`/`PermLevel`). |
| `CLOSE_DIALOG` | false | Closes the open dialog. No payload. |
| `INTERACT_BLOCK` | true | Interact with a block; uses `BlockPos` (+ `Cmd`). |
| `OPEN_TRADING_SCREEN` | false | Opens the NPC trade UI. No payload. |
| `OPEN_DEFAULT_DIALOG` | false | Opens the NPC's default dialog. No payload. |
| `OPEN_NAMED_DIALOG` | true | `Cmd` = target dialog label/name. |
| `OPEN_NAMED_DIALOG_CONDITIONAL` | true | `Cmd` = dialog name; opened only if entry conditions pass. |
| `SCOREBOARD` | true | `Cmd` = scoreboard operation payload. |

\* `NONE` uses the 2-arg ctor so its flag is `true`, but it is a placeholder and never executes. `getId()` on this enum is `name().toLowerCase(ROOT)` + suffix (UI id), unrelated to SNBT.

---

### `ActionDataEntry` — one action (class `ActionDataEntry`)

Each entry is a Compound in an event's list. **Write path is sparse**: optional fields are only emitted when they differ from their default (noted below). On read, missing fields fall back to the listed default.

| Tag key | Type | Default | Meaning / write condition |
|---------|------|---------|---------------------------|
| `Id` | UUID (int-array) | random UUID if absent | Entry id. **Always written**. Absent on read → `UUID.randomUUID()`, so hand-written presets may omit it. |
| `Type` | string | `NONE` | `ActionDataType.name()`. **Always written**. |
| `Cmd` | string | `""` | The action argument (command text / dialog name). Written only if non-empty after `trim()`. (Field constant is `DATA_COMMAND_TAG` but the literal key is `Cmd`.) |
| `TargetUUID` | UUID (int-array) | `null` | Optional action target. Written only if non-null. |
| `BlockPos` | compound `{X:int, Y:int, Z:int}` | ORIGIN `(0,0,0)` | For `INTERACT_BLOCK`. Written only if `!= ORIGIN`. |
| `ExecAsUser` | byte (bool) | `false` (`0b`) | Run the command as the interacting player vs. as the NPC/server. Written only if `true`. |
| `Debug` | byte (bool) | `false` (`0b`) | Log debug output on execution. Written only if `true`. |
| `PermLevel` | int | `2` (GAMEMASTERS) | Minecraft permission level the command runs at. Clamped to **[0, 4]** on read (`ALL`=0 … `OWNERS`=4; below 0 → 0 with warn, above 4 → 4 with warn). Written only if `!= 2`. |
| `ConditionDataSet` | compound | empty | Gate conditions (see below). Written only if non-empty. |

Permission-level source enum `CommandPermissionLevel`: `ALL`=0, `MODERATORS`=1, `GAMEMASTERS`=2 (default), `ADMINS`=3, `OWNERS`=4 (max).

`ActionDataSet` (class `ActionDataSet`) is a `LinkedHashSet<ActionDataEntry>` (insertion order preserved); serialized as a `ListTag` of Compounds. Entries failing `isValidAndNotEmpty()` are skipped on save. Its standalone container key constant is `ActionDataSet` (used when saved outside an event map).

```snbt
{ Id: [I; -1, -1, -1, -1],
  Type: "COMMAND",
  Cmd: "give @s minecraft:diamond 1",
  ExecAsUser: 0b,
  PermLevel: 4,
  ConditionDataSet: { ConditionDataSet: [ { Type: "PLAYER_TAG", Name: "story.metCEO" } ] } }
```

---

### `ConditionType` — condition kinds (class `ConditionType`)

All 16 constants (ordinal order). Each carries a `ConditionTypeRequirements` (which of `Name`/`Value`/`Operation` are meaningful) and optionally a subtype enum. Fields not "required" by a type are **ignored** even if present (notably: types without operation ignore `Operation` entirely).

| Constant | Requirements (Name / Value / Operation) | SubType enum | Notes |
|----------|-----------------------------------------|--------------|-------|
| `NONE` | – / – / – | — | Placeholder / unset. |
| `SCOREBOARD` | ✔ / ✔ / ✔ | — | `Name` = objective/target, `Value` compared via `Operation`. |
| `EXECUTION_LIMIT` | – / ✔ / – | `DurationType` | Rate-limit: `Value` executions per `SubType` window. **Ignores Operation.** |
| `HAS_ITEM_IN_INVENTORY` | ✔ / – / – | — | `Name` = item id. Ignores Value/Operation. |
| `HAS_ITEM_IN_HAND` | ✔ / – / – | `HandItemType` | `Name` = item id, `SubType` = which hand. Ignores Value/Operation. |
| `ADVANCEMENT` | ✔ / – / – | — | `Name` = advancement id. Ignores Value/Operation. |
| `EXPERIENCE_LEVEL` | – / ✔ / ✔ | — | Player XP level vs. `Value`. |
| `PLAYER_HEALTH` | – / ✔ / ✔ | — | Player health vs. `Value`. |
| `NPC_HEALTH` | – / ✔ / ✔ | — | NPC health vs. `Value`. |
| `ENTITY_HEALTH` | ✔ / ✔ / ✔ | — | `Name` = target entity, health vs. `Value`. |
| `PLAYER_TAG` | ✔ / – / – | — | `Name` = scoreboard tag. **Ignores Operation** (presence-only). |
| `TEAM` | ✔ / – / – | — | `Name` = team name. Ignores Value/Operation. |
| `GAMEMODE` | ✔ / – / – | — | `Name` = gamemode. Ignores Value/Operation. |
| `TIME_OF_DAY` | – / ✔ / ✔ | — | World time vs. `Value`. |
| `WEATHER` | – / – / – | `WeatherType` | `SubType` = weather. Ignores Name/Value/Operation. |
| `FALLBACK` | – / – / – | — | Catch-all / else branch. Ignores all. |

`ConditionTypeRequirements` (class `ConditionTypeRequirements`), ctor `(name, value, operation)` booleans:

| Constant | requiresName | requiresValue | requiresOperation |
|----------|:---:|:---:|:---:|
| `NONE` | false | false | false |
| `NAME_ONLY` | true | false | false |
| `VALUE_ONLY` | false | true | false |
| `VALUE_AND_OPERATION` | false | true | true |
| `NAME_VALUE_OPERATION` | true | true | true |

**Conditions that ignore `Operation`** (requirements without the operation flag): `EXECUTION_LIMIT`, `HAS_ITEM_IN_INVENTORY`, `HAS_ITEM_IN_HAND`, `ADVANCEMENT`, `PLAYER_TAG`, `TEAM`, `GAMEMODE`, `WEATHER`, `FALLBACK`, `NONE`. (Even if an `Operation` tag is present it is not written on save when the entry's `operationType` is `NONE`, and comparison-less types never consult it.)

---

### `ConditionOperationType` — comparison operators (class `ConditionOperationType`)

All 7 constants (ordinal order). `evaluate(int actual, int expected)` semantics verified from bytecode; `getSymbol()` shown for reference.

| Constant | Symbol | `evaluate(actual, expected)` true when |
|----------|--------|----------------------------------------|
| `NONE` | `""` | always **false** (default/unset) |
| `EQUALS` | `==` | `actual == expected` |
| `NOT_EQUALS` | `!=` | `actual != expected` |
| `GREATER_THAN` | `>` | `actual > expected` |
| `GREATER_THAN_OR_EQUALS` | `>=` | `actual >= expected` |
| `LESS_THAN` | `<` | `actual < expected` |
| `LESS_THAN_OR_EQUALS` | `<=` | `actual <= expected` |

---

### `ConditionSubTypeEntry` implementations

`ConditionSubTypeEntry` is a marker interface; the concrete subtype enums are stored as the `SubType` string (exact UPPERCASE `Enum.name()`).

| Enum (class) | Used by | Constants |
|--------------|---------|-----------|
| `DurationType` | `EXECUTION_LIMIT` | `PER_MINUTE`, `PER_HOUR`, `PER_DAY`, `PER_WEEK`, `PER_MONTH`, `LIFETIME` |
| `HandItemType` | `HAS_ITEM_IN_HAND` | `BOTH`, `MAIN_HAND`, `OFF_HAND` |
| `WeatherType` | `WEATHER` | `CLEAR`, `RAIN`, `THUNDER` |

---

### `ConditionDataEntry` — one condition (class `ConditionDataEntry`)

Compound entries inside a `ConditionDataSet` list. Conditions are **not** given a persisted `Id` (`getId()` is derived at runtime via `UUID.nameUUIDFromBytes(hashCode)`). Sparse write path:

| Tag key | Type | Default | Meaning / write condition |
|---------|------|---------|---------------------------|
| `Type` | string | `NONE` | `ConditionType.name()`. **Always written**. |
| `SubType` | string | `null` | Subtype `Enum.name()` (`DurationType`/`HandItemType`/`WeatherType`). Written only if a subType is set. |
| `Operation` | string | `NONE` | `ConditionOperationType.name()`. Written only if `!= NONE`. |
| `Name` | string | `""` | Item id / tag / objective / entity — meaning depends on `Type`. Written only if `hasName()` (trimmed). |
| `Value` | int | `0` | Comparison value / limit count. Written only if `!= 0`. |
| `CustomDataComponent` | string | `""` | Optional item data-component matcher. Written only if set (trimmed). |
| `CustomData` | string | `""` | Optional extra custom NBT/data string. Written only if set (trimmed). |
| `Text` | string | — | **Legacy read-only** (`DATA_LEGACY_TEXT_TAG`). Old presets stored the subtype name here; on read, `SubType` is preferred, then `Text`. Never written by current versions. |

`ConditionDataSet` (class `ConditionDataSet`) is a `LinkedHashSet<ConditionDataEntry>` serialized as a `ListTag` under the key `ConditionDataSet` (constant `CONDITION_DATA_SET_TAG`). This nests inside each `ActionDataEntry`'s `ConditionDataSet` compound — note the doubled key: `ConditionDataSet: { ConditionDataSet: [ … ] }`.

```snbt
ConditionDataSet: { ConditionDataSet: [
  { Type: "PLAYER_HEALTH", Operation: "GREATER_THAN", Value: 10 },
  { Type: "HAS_ITEM_IN_HAND", SubType: "MAIN_HAND", Name: "minecraft:emerald" },
  { Type: "EXECUTION_LIMIT", SubType: "PER_DAY", Value: 3 },
  { Type: "WEATHER", SubType: "RAIN" }
] }
```

---

### Execution package — `EXECUTION_LIMIT` runtime state (package `data.execution`)

These structures back the `EXECUTION_LIMIT` condition. `ExecutionTrackerData`/`ExecutionData` are **runtime/persistence state** (per-player, per-target counters) — normally *not* hand-authored in presets — but the tag keys are documented for completeness.

`ExecutionInterval` (class `ExecutionInterval`) — the window enum mirroring `DurationType`, with verified window lengths in milliseconds:

| Constant | Milliseconds |
|----------|-------------|
| `PER_MINUTE` | 60000 |
| `PER_HOUR` | 3600000 |
| `PER_DAY` | 86400000 |
| `PER_WEEK` | 604800000 |
| `PER_MONTH` | 2592000000 |
| `LIFETIME` | 9223372036854775807 (`Long.MAX_VALUE`) |

`ExecutionData` (record class `ExecutionData`) — one player↔target counter:

| Tag key | Type | Default (read) | Meaning |
|---------|------|----------------|---------|
| `Count` | int | `0` | Executions in the current window. |
| `WindowStart` | long | `0` | Epoch-ms when the window opened. |
| `LastExec` | long | `0` | Epoch-ms of the last execution. |

All three are **always written** by `save`.

`ExecutionTrackerData` (record class `ExecutionTrackerData`) — nested map `player UUID → (target UUID → ExecutionData)`:

| Tag key | Type | Meaning |
|---------|------|---------|
| `Players` | list of compound | One entry per player. |
| `PlayerUUID` | UUID (int-array) | Player id (inside each `Players` element). |
| `Targets` | list of compound | Per-target counters for that player. |
| `TargetUUID` | UUID (int-array) | Target/NPC id (inside each `Targets` element). |

Each `Targets` element also inlines the `Count`/`WindowStart`/`LastExec` fields of its `ExecutionData`.

---

## Objectives — Types, Goals, Fields

All facts below are bytecode-verified against `easy_npc-fabric-1.21.1-6.25.0.jar`. Authoring path in a preset:

```
ObjectiveData:{
  ObjectiveDataSet:[ {Type:"FLOAT",Prio:0}, {Type:"LOOK_AT_PLAYER",Prio:9} ],
  HasObjectives:1b
}
```

The wrapper compound key is **`ObjectiveData`** (`ObjectiveDataCapable`), which contains the list **`ObjectiveDataSet`** (`ObjectiveDataSet`), whose elements are `ObjectiveDataEntry` compounds. Each entry's `Type` string is resolved by `ObjectiveType.get()` → `Enum.valueOf()`, so **it must be the EXACT UPPERCASE constant name**; null/empty/unknown falls back to `NONE` (silently ignored).

### ObjectiveType — all 46 constants

Class: `de.markusbordihn.easynpc.data.objective.ObjectiveType`. Columns: `friendlyName` (the enum's second field, used only for GUI/translation keys — **never** an NBT value), `Prio` (enum default priority — used by the GUI/`getOrCreateObjective`, *not* auto-applied to hand-authored SNBT), `Travel` (`hasTravelObjective` flag), `Goal produced` (from `ObjectiveUtils.createObjectiveGoal`/`createObjectiveTarget`, dispatched via ordinal switch in `ObjectiveUtils$1`), and fields consumed.

`class_####` = obfuscated vanilla goal; readable name in parentheses. Goals marked **[PF]** call `requiresPathfinderMob` first and return **null** (objective silently skipped) if the Easy NPC entity is not a `PathfinderMob` (`class_1314`).

| Constant | friendlyName | Prio | Travel | Goal produced | Consumes |
|----------|--------------|:----:|:------:|---------------|----------|
| `NONE` | `none` | 5 | no | *(none — default switch → null; entries of this type are dropped by `addObjective`/`save`)* | — |
| **Look** | | | | | |
| `LOOK_AT_PLAYER` | `player` | 9 | no | `CustomLookAtPlayerGoal` (target `class_1657` Player) | LookDistance, Probability |
| `LOOK_AT_MOB` | `mob` | 10 | no | `CustomLookAtPlayerGoal` (target `class_1308` Mob) | LookDistance, Probability |
| `LOOK_AT_ANIMAL` | `animal` | 10 | no | `CustomLookAtPlayerGoal` (target `class_1429` Animal) | LookDistance, Probability |
| `LOOK_AT_OWNER` | `owner` | 9 | no | `LookAtEntityByUUIDGoal` (resolves owner via `getTargetOwner`) | LookDistance, TargetOwnerUUID |
| `LOOK_AT_ENTITY_BY_UUID` | `entity` | 9 | no | `LookAtEntityByUUIDGoal` | LookDistance, TargetEntityUUID |
| `LOOK_RANDOM_AROUND` | `random` | 10 | no | `class_1376` (RandomLookAroundGoal) | — |
| `LOOK_AT_RESET` | `reset` | 9 | no | `ResetLookAtPlayerGoal` | — |
| `LOOK_AT_ITEM` | `item` | 9 | no | **NOT IMPLEMENTED** — no switch case; falls to default → null | — |
| **Swim / Float** | | | | | |
| `FLOAT` | `float` | 0 | yes | `class_1347` (FloatGoal) | — |
| `RANDOM_SWIMMING` | `swimming` | 4 | yes | **[PF]** `class_1378` (RandomSwimmingGoal) | SpeedModifier, Interval |
| **Stroll / Home / Village** | | | | | |
| `RANDOM_STROLL` | `stroll` | 5 | yes | `RandomStrollAroundGoal` | SpeedModifier |
| `WATER_AVOIDING_RANDOM_STROLL` | `avoid_water` | 5 | yes | `class_1394` (WaterAvoidingRandomStrollGoal) if PathfinderMob, else `RandomStrollAroundGoal` | SpeedModifier |
| `RANDOM_STROLL_AROUND_HOME` | `around_home` | 2 | yes | `RandomStrollAroundHomeGoal` | SpeedModifier |
| `RANDOM_STROLL_IN_VILLAGE` | `in_village` | 2 | yes | **[PF]** `class_5274` (GolemRandomStrollInVillageGoal) | SpeedModifier |
| `MOVE_BACK_TO_HOME` | `back_home` | 3 | yes | `MoveBackToHomeGoal` | SpeedModifier, StopDistance |
| `MOVE_BACK_TO_VILLAGE` | `back_village` | 3 | yes | **[PF]** `class_4291` (MoveBackToVillageGoal, canMoveThroughLeaves=false) | SpeedModifier |
| `MOVE_THROUGH_VILLAGE` | `through_village` | 5 | yes | **[PF]** `class_1368` (MoveThroughVillageGoal) | SpeedModifier, OnlyAtNight, DistanceToPoi, CanDealWithDoors |
| **Follow** | | | | | |
| `FOLLOW_PLAYER` | `player` | 7 | yes | `FollowLivingEntityGoal` (player by name) | SpeedModifier, StopDistance, StartDistance, TargetPlayerName |
| `FOLLOW_OWNER` | `owner` | 6 | yes | `FollowLivingEntityGoal` (owner) | SpeedModifier, StopDistance, StartDistance, TargetOwnerUUID |
| `FOLLOW_ENTITY_BY_UUID` | `entity` | 7 | yes | `FollowLivingEntityGoal` (entity by UUID) | SpeedModifier, StopDistance, StartDistance, TargetEntityUUID |
| `FOLLOW_ITEM` | `item` | 7 | yes | **[PF]** `class_1391` (TemptGoal; item from `TargetItemTag`, `speedMod`, canScare=false) | SpeedModifier, TargetItemTag |
| **Panic** | | | | | |
| `PANIC` | `panic` | 1 | yes | `CustomPanicGoal` | SpeedModifier |
| **Sun** | | | | | |
| `AVOID_SUN` | `avoid_sun` | 2 | yes | **[PF]** `class_1384` (RestrictSunGoal) | — |
| `FLEE_SUN` | `flee_sun` | 3 | yes | **[PF]** `class_1344` (FleeSunGoal) | SpeedModifier |
| **Flee** (all `class_1338` AvoidEntityGoal; farSpeed = `speedMod * 1.2`) | | | | | |
| `FLEE_CREEPER` | `creeper` | 3 | yes | **[PF]** `class_1338` avoid `class_1548` Creeper | LookDistance, SpeedModifier |
| `FLEE_MOB` | `mob` | 3 | yes | **[PF]** `class_1338` avoid `class_1308` Mob | LookDistance, SpeedModifier |
| `FLEE_MONSTER` | `monster` | 3 | yes | **[PF]** `class_1338` avoid `class_1588` Monster | LookDistance, SpeedModifier |
| `FLEE_PLAYER` | `player` | 3 | yes | **[PF]** `class_1338` avoid `class_1657` Player | LookDistance, SpeedModifier |
| `FLEE_VILLAGER` | `villager` | 3 | yes | **[PF]** `class_1338` avoid `class_3988` AbstractVillager | LookDistance, SpeedModifier |
| **Doors** | | | | | |
| `OPEN_DOOR` | `open_door` | 8 | yes | `CustomDoorInteractGoal(mob, false)` | — |
| `CLOSE_DOOR` | `close_door` | 8 | yes | `CloseDoorGoal(mob)` | — |
| **Melee / ranged (goal side)** | | | | | |
| `MELEE_ATTACK` | `melee` | 2 | yes | **[PF]** `CustomMeleeAttackGoal` | SpeedModifier, MustSeeTarget |
| `ZOMBIE_ATTACK` | `zombie` | 2 | yes | **[PF]** `ZombieAttackGoal` | SpeedModifier, MustSeeTarget |
| `BOW_ATTACK` | `bow` | 4 | yes | **[PF]** `BowAttackGoal` | SpeedModifier, AttackInterval, AttackRadius |
| `CROSSBOW_ATTACK` | `crossbow` | 4 | yes | **[PF]** `CrossbowAttackGoal` | SpeedModifier, AttackRadius |
| `GUN_ATTACK` | `gun` | 4 | yes | **[PF]** `GunAttackGoal` | SpeedModifier, AttackInterval, AttackRadius |
| **Attack targeting** (`createObjectiveTarget`; all `class_1400` NearestAttackableTargetGoal unless noted) | | | | | |
| `ATTACK_ANIMAL` | `animal` | 2 | yes | `class_1400` target `class_1429` Animal | MustSeeTarget |
| `ATTACK_PLAYER` | `player` | 2 | yes | `class_1400` target `class_1657` Player | MustSeeTarget |
| `ATTACK_PLAYER_WITHOUT_OWNER` | `player_without_owner` | 2 | yes | `class_1400` target Player, predicate excludes NPC owner | Interval, MustSeeTarget, MustReachTarget, TargetOwnerUUID |
| `ATTACK_MONSTER` | `monster` | 2 | yes | `class_1400` target `class_1588` Monster | MustSeeTarget |
| `ATTACK_MOB` | `mob` | 2 | yes | `class_1400` target `class_1308` Mob, predicate = instanceof `class_1569` (Enemy) | Interval, MustSeeTarget, MustReachTarget |
| `ATTACK_MOB_WITHOUT_CREEPER` | `mob_without_creeper` | 2 | yes | `class_1400` target Mob, predicate = Enemy && !`class_1548` Creeper | Interval, MustSeeTarget, MustReachTarget |
| `ATTACK_VILLAGER` | `villager` | 2 | yes | `class_1400` target `class_3988` AbstractVillager | MustSeeTarget |
| `OWNER_HURT_BY_TARGET` | `owner_hurt_by_target` | 2 | yes | `CustomOwnerHurtByTargetGoal` | (owner) |
| `HURT_BY_TARGET` | `hurt_by_target` | 2 | yes | **[PF]** `class_1399` (HurtByTargetGoal), `setAlertOthers()` | — |

Notes:
- Every constant except the 8 `LOOK_*` types and `NONE` has `Travel = true` (`hasTravelObjective`). This flag drives the outer `HasTravelTarget` write-hint; it does not gate goal creation.
- `LOOK_AT_ITEM` is a defined constant but has **no goal implementation** (no case in either switch) — authoring it produces a silent no-op.

### ObjectiveDataEntry — serialized fields

Class: `de.markusbordihn.easynpc.data.objective.ObjectiveDataEntry`. Round-trip is `save(CompoundTag)` / `load(CompoundTag)`. On **read**, each field (except `Type`/`Prio`) is guarded by `contains(key)`, so an omitted key keeps the constructor default. On **write**, most numeric fields are written **only when they differ from their default** (marked "non-default only"); booleans have inverse rules (below). Tag key strings are the exact literals.

| Tag key | NBT type | Default | Written when | Meaning / consumers |
|---------|----------|---------|--------------|---------------------|
| `Type` | String | `"NONE"` | **always** | `ObjectiveType.name()` (UPPERCASE). Selects the goal. |
| `Prio` | int | `1` (ctor) / **`0` if key absent on load** | **always** | Goal priority (lower = evaluated first). Read *unconditionally* — omitting `Prio` yields **0**, not the enum default. |
| `Id` | String | `objectiveType.name()` (or random UUID via no-arg ctor) | only if non-null, non-empty, and not equal to `Type` (case-insensitive) | HashMap key in the set. Give a distinct `Id` to have **multiple entries of the same Type** (same-Id entries overwrite each other). |
| `SpeedModifier` | double | `0.7` | `!= 0.7` | Movement speed multiplier. |
| `StartDistance` | float | `16.0` | `!= 16.0` | Follow: distance at which NPC starts following. |
| `StopDistance` | float | `2.0` | `!= 2.0` | Follow / MoveBackToHome: stop distance. |
| `LookDistance` | float | `15.0` | `!= 15.0` | Look & Flee: detection/look range. |
| `Probability` | float | `1.0` | `!= 1.0` | LOOK_AT_PLAYER/MOB/ANIMAL: per-tick look chance. |
| `Interval` | int | `10` | `!= 10` | RANDOM_SWIMMING interval; ATTACK target `randomInterval`. |
| `OnlyAtNight` | boolean | `false` | **only if `true`** | MOVE_THROUGH_VILLAGE night restriction. |
| `DistanceToPoi` | int | `16` | `!= 16` | MOVE_THROUGH_VILLAGE POI search distance. |
| `CanDealWithDoors` | boolean | `false` (`() -> false`) | **only if `true`** | MOVE_THROUGH_VILLAGE door handling (stored as `BooleanSupplier`). |
| `MustSeeTarget` | boolean | **`true`** | **only if `false`** (inverse) | Melee/Zombie & attack targets: require line-of-sight. Omit ⇒ true. |
| `MustReachTarget` | boolean | **`true`** | **only if `false`** (inverse) | ATTACK_MOB(_WITHOUT_CREEPER)/PLAYER_WITHOUT_OWNER: require reachable path. Omit ⇒ true. |
| `AttackInterval` | int | `20` | `!= 20` | BOW/GUN reload/attack cooldown (ticks). |
| `AttackRadius` | float | `8.0` | `!= 8.0` | CROSSBOW/BOW/GUN attack range. |
| `TargetPlayerName` | String | *(null)* | if non-null & non-empty | FOLLOW_PLAYER target (resolved by name). |
| `TargetOwnerUUID` | int-array (UUID) | *(null)* | if non-null | FOLLOW_OWNER / LOOK_AT_OWNER / ATTACK_PLAYER_WITHOUT_OWNER. Format `[I;a,b,c,d]`. |
| `TargetEntityUUID` | int-array (UUID) | *(null)* | if non-null | FOLLOW_ENTITY_BY_UUID / LOOK_AT_ENTITY_BY_UUID. Format `[I;a,b,c,d]`. |
| `TargetItemTag` | String | *(null)* | if non-null & non-empty | FOLLOW_ITEM: item registry id (e.g. `"minecraft:wheat"`). |

Field field-name → tag-key is not 1:1 obvious: the boolean getters are `getOnlyAtNight`, `isMustSeeTarget`, `isMustReachTarget`. Non-serialized runtime fields: `isRegistered`, `goal`, `target` (never written).

Minimal entry examples:
```
{Type:"LOOK_AT_PLAYER",Prio:9}                                  // uses all defaults (LookDistance 15, Prob 1.0)
{Type:"FOLLOW_PLAYER",Prio:7,TargetPlayerName:"Steve",StopDistance:2.0f,StartDistance:10.0f}
{Type:"MELEE_ATTACK",Prio:2,MustSeeTarget:0b}                   // MustSeeTarget only appears when false
{Type:"FOLLOW_ITEM",Prio:7,TargetItemTag:"minecraft:wheat",SpeedModifier:1.0d}
```

### ObjectiveDataSet + outer ObjectiveData compound

Set class: `de.markusbordihn.easynpc.data.objective.ObjectiveDataSet` (backing `HashMap<Id, ObjectiveDataEntry>`). Wrapper: `de.markusbordihn.easynpc.entity.easynpc.data.ObjectiveDataCapable`.

`ObjectiveDataSet.save()` always writes the list key **`ObjectiveDataSet`** (a `TAG_List` of compounds), even when empty (`[]`). `NONE`-typed entries are skipped. `ObjectiveDataSet.load()` returns early (no-op) if the `ObjectiveDataSet` key is absent; if present it **`clear()`s the map first**, then loads every entry.

`ObjectiveDataCapable.addAdditionalObjectiveData()` writes the wrapper compound **`ObjectiveData`** containing:

| Tag key | NBT type | Written when | Meaning |
|---------|----------|--------------|---------|
| `ObjectiveDataSet` | List<Compound> | always (server-side) | the entry list (see above) |
| `HasObjectives` | boolean | **always** | `!objectives.isEmpty()` |
| `HasTravelTarget` | boolean | only if `true` | any entry has `hasTravelObjective` |
| `HasPlayerTarget` | boolean | only if `true` | any entry has a player target |
| `HasEntityTarget` | boolean | only if `true` | any entry has an entity-UUID target |

There is **no** `HasOwnerTarget` in NBT (the field exists but is never serialized). **The four `Has*` flags are write-only hints and are IGNORED on read** — `readAdditionalObjectiveData` reads only `ObjectiveDataSet`, and the flags are recomputed internally by `ObjectiveDataSet.updateTargetFlags()` on every `addObjective`/`removeObjective`. So in a hand-authored preset the `Has*` flags are optional/cosmetic; only `ObjectiveDataSet` matters.

`readAdditionalObjectiveData` flow:
1. No `ObjectiveData` key ⇒ return (keep existing).
2. Get `ObjectiveData` compound. If it contains `ObjectiveDataSet` ⇒ build a fresh `ObjectiveDataSet` (which `clear()`s and loads), `setObjectiveDataSet(...)`, then `registerCustomObjectives()`.
3. If `getNPCDataVersion() == -1` (never-saved NPC) ⇒ additionally `registerStandardObjectives()`, which adds `LOOK_AT_RESET`(prio 10), `LOOK_AT_PLAYER`(prio 9), `LOOK_AT_MOB`(prio 10).

**Empty-set "brick" rule.** Writing `ObjectiveData:{ObjectiveDataSet:[]}` is **destructive**: on load it clears the map and installs an empty set, so the NPC ends up with **zero AI goals** (no float, no look, no idle stroll). Because the standard-objectives fallback only runs when `getNPCDataVersion() == -1`, any NPC that already carries a data version and is fed an empty `ObjectiveDataSet` is left permanently inert. Corollaries for authoring:
- To leave the NPC's default/standard behaviour untouched, **omit the `ObjectiveDataSet` key** (or omit `ObjectiveData` entirely) — that path is a pure no-op.
- To fully control AI, provide a **non-empty** `ObjectiveDataSet` listing every objective you want (there is no merge with defaults; the set is replaced wholesale).

---

## Attributes, Display, Profession, Progression

All facts below are bytecode-verified against `easy_npc-fabric-1.21.1-6.25.0.jar`. Boolean fields are stored as NBT `Byte` (`0b`/`1b` in SNBT); enum-valued fields are stored as the **UPPERCASE constant name** unless noted. In 6.25.0 the attribute system was re-split into *categories* — the old flat `EntityAttribute` field names are now grouped, but they still all serialize into one flat compound.

### Top-level NBT storage keys (where each structure lives in the NPC root tag)

| Root tag key | NBT type | Structure / class | Persisted? |
|---|---|---|---|
| `EntityAttribute` | Compound (flat) | `EntityAttributes` (Combat + Environmental + Interaction + Movement) | yes (`SynchedDataIndex.ENTITY_ATTRIBUTES`) |
| `BaseAttributes` | Compound | `BaseAttributes` (editable vanilla-backed stats) | yes |
| `DisplayAttribute` | List of Compound | `DisplayAttributeDataSet` | yes (`DISPLAY_ATTRIBUTE_SET`) |
| `Profession` | String | `ProfessionDataCapable` (`Profession` enum) | yes (`PROFESSION`) |
| `Progression` | Compound | `ProgressionData` | yes (`PROGRESSION`) |
| `Scores` | Compound | `ScoreboardData` (runtime dialog helper, not a preset field) | n/a |

`SynchedDataIndex` (`data/synched`) marks every persistent field; **only** `ATTACK_IS_CHARGING_CROSSBOW` and `MODEL_ANIMATION` are non-persistent (transient) — everything in this subsystem is written to disk.

---

### 1. Entity attributes — `EntityAttribute` compound

Cited: `EntityAttributes`, `CombatAttributes`, `EnvironmentalAttributes`, `InteractionAttributes`, `MovementAttributes` and their `*AttributeType` enums (`data/attribute`).

**Category enum** `EntityAttributeType`: `COMBAT`, `CUSTOM`, `INTERACTION`, `MOVEMENT`, `ENVIRONMENTAL`. Only COMBAT/ENVIRONMENTAL/INTERACTION/MOVEMENT are wired into `EntityAttributes` — **`CUSTOM` is declared but never serialized** into `EntityAttribute`.

**Key derivation:** every tag key is `TextUtils.convertToCamelCase(ENUM_NAME)` (split on `_`/`-`/space, first letter lower, each following segment capitalised). All four categories write their keys **flat into the single `EntityAttribute` compound** (no per-category sub-compound). On `load()`, if `EntityAttribute` is absent **or empty**, the whole structure is skipped and constructor defaults are kept.

#### 1a. Combat (`CombatAttributeType` / `CombatAttributes`)

| Tag key | Type | Default | Enum constant | Meaning |
|---|---|---|---|---|
| `isAttackableByPlayers` | bool | `false` | `IS_ATTACKABLE_BY_PLAYERS` | players may damage the NPC |
| `isAttackableByMonsters` | bool | `false` | `IS_ATTACKABLE_BY_MONSTERS` | hostile mobs may target/damage it |
| `isInvulnerable` | bool | `true`¹ | `IS_INVULNERABLE` | immune to all damage |
| `isKnockbackResistant` | bool | `false` | `IS_KNOCKBACK_RESISTANT` | ignores knockback |
| `isExplosionResistant` | bool | `false` | `IS_EXPLOSION_RESISTANT` | immune to explosion damage |
| `healthRegeneration` | double | `0.0` | `HEALTH_REGENERATION` | HP regen rate |

¹ **Default nuance:** a fresh `CombatAttributes()` has `isInvulnerable = true`, all other flags `false`, `healthRegeneration = 0.0`. But when an `EntityAttribute` compound **is present**, `decode()` reads each key with `getBoolean`/`getDouble`, which fall back to `false`/`0.0` for any **omitted** key. So omitting `isInvulnerable` *inside a present compound* yields `false`, not `true`. `encode()` always writes all six keys, so a round-tripped preset contains them explicitly.

#### 1b. Environmental (`EnvironmentalAttributeType` / `EnvironmentalAttributes`)

| Tag key | Type | Default | Enum constant | Meaning |
|---|---|---|---|---|
| `canBreatheUnderwater` | bool | `false` | `CAN_BREATHE_UNDERWATER` | no drowning |
| `canFloat` | bool | `false` | `CAN_FLOAT` | swims up / floats in water |
| `freefall` | bool | `false` | `FREEFALL` | disables fall handling |
| `noGravity` | bool | `false` | `NO_GRAVITY` | ignores gravity |

#### 1c. Interaction (`InteractionAttributeType` / `InteractionAttributes`)

| Tag key | Type | Default | Enum constant | Meaning |
|---|---|---|---|---|
| `isPushable` | bool | `false` | `IS_PUSHABLE` | pushed by other entities/pistons |
| `canBeHitByProjectile` | bool | `false` | `CAN_BE_HIT_BY_PROJECTILE` | arrows/etc. collide |
| `canBeLeashed` | bool | `false` | `CAN_BE_LEASHED` | lead attachable |
| `pushEntities` | bool | `false` | `PUSH_ENTITIES` | pushes other entities |

#### 1d. Movement (`MovementAttributeType` / `MovementAttributes`)

| Tag key | Type | Default | Enum constant | Meaning |
|---|---|---|---|---|
| `canOpenDoor` | bool | `false` | `CAN_OPEN_DOOR` | opens doors during pathfinding |
| `canCloseDoor` | bool | `false` | `CAN_CLOSE_DOOR` | closes doors behind it |
| `canPassDoor` | bool | `false` | `CAN_PASS_DOOR` | paths through door blocks |
| `canUseNetherPortal` | bool | `false` | `CAN_USE_NETHER_PORTAL` | may traverse nether portals |

**SNBT example** (flat compound, all 18 keys):

```
EntityAttribute:{
  isAttackableByPlayers:0b, isAttackableByMonsters:0b, isInvulnerable:1b,
  isKnockbackResistant:0b, isExplosionResistant:0b, healthRegeneration:0.0d,
  canBreatheUnderwater:0b, canFloat:1b, freefall:0b, noGravity:0b,
  isPushable:0b, canBeHitByProjectile:1b, canBeLeashed:0b, pushEntities:0b,
  canOpenDoor:0b, canCloseDoor:0b, canPassDoor:0b, canUseNetherPortal:0b
}
```

---

### 2. Editable base stats — `BaseAttributes` compound

Cited: `BaseAttributes`, `BaseAttributeType` (`data/attribute`). These are Easy NPC's own editable copies of the four vanilla stats; keys are camelCase of the enum name. Stored as `Double` (values are float-rounded on save via `d2f/f2d`). `load()` reads each key only if present, else keeps the constructor default; missing `BaseAttributes` tag → all defaults.

| Tag key | Type | Default | Enum constant | Vanilla attribute name (`getAttributeName()`) |
|---|---|---|---|---|
| `attackDamage` | double | `2.0` | `ATTACK_DAMAGE` | `attack_damage` |
| `attackKnockback` | double | `0.0` | `ATTACK_KNOCKBACK` | `attack_knockback` |
| `followRange` | double | `32.0` | `FOLLOW_RANGE` | `follow_range` |
| `knockbackResistance` | double | `0.0` | `KNOCKBACK_RESISTANCE` | `knockback_resistance` |

```
BaseAttributes:{ attackDamage:4.0d, attackKnockback:0.0d, followRange:32.0d, knockbackResistance:0.0d }
```

`CustomAttributeType` (`data/attribute`) has a single constant `CUSTOM`; its `getTagName()` uses `convertToPascalCase` → `"Custom"`, but it is not serialized anywhere in this subsystem.

---

### 3. Vanilla `attributes` list (inherited — NOT Easy NPC code)

> Not present in this jar; cannot be bytecode-verified here. Easy NPC NPCs extend a vanilla `PathfinderMob`, so Minecraft 1.21.1 serializes the standard living-entity `attributes` list independently of the `BaseAttributes`/`EntityAttribute` structures above. For authoring, prefer the Easy NPC `BaseAttributes` compound (§2); the vanilla list is documented only for completeness.

Format (vanilla MC 1.21.1): NBT list key `attributes`, each element a compound:

| Field | Type | Meaning |
|---|---|---|
| `id` | String (ResourceLocation) | attribute id, e.g. `minecraft:generic.max_health`, `minecraft:generic.movement_speed`, `minecraft:generic.attack_damage` (the `generic.` prefix is dropped in 1.21.2+) |
| `base` | double | base value before modifiers |
| `modifiers` | list of compound | optional modifier list |

Each `modifiers` entry: `id` (String ResourceLocation — replaced the old UUID+name in 1.21), `amount` (double), `operation` (String). **`operation` enum values** (`AttributeModifier.Operation`, serialized lowercase):

| String | Effect |
|---|---|
| `add_value` | flat add to base |
| `add_multiplied_base` | add `amount × base` |
| `add_multiplied_total` | multiply running total by `(1 + amount)` |

```
attributes:[
  {id:"minecraft:generic.max_health", base:40.0d},
  {id:"minecraft:generic.movement_speed", base:0.25d,
   modifiers:[{id:"upm:speed_boost", amount:0.2d, operation:"add_multiplied_base"}]}
]
```

---

### 4. Display attributes — `DisplayAttribute` list

Cited: `DisplayAttributeType`, `DisplayAttributeEntry`, `DisplayAttributeDataSet`, `NameVisibilityType` (`data/display`), `ValueType` (`data/type`), `DisplayAttributeDataCapable`.

Stored as a **List of compounds** under root key `DisplayAttribute`. Each list element is one attribute:

| Entry tag key | Type | Written when | Meaning (`DisplayAttributeEntry`) |
|---|---|---|---|
| `Type` | String | always | `DisplayAttributeType.name()` — **UPPERCASE** (parsed back via `valueOf`) |
| `Bool` | bool | only if value `== true` | boolean payload |
| `Int` | int | only if value `!= 0` | integer payload |
| `Text` | String | only if non-null & non-empty | string payload |

A given attribute uses exactly one payload key according to its `ValueType`. Because non-default payloads are omitted, a boolean attribute set to `false` serializes as just `{Type:"..."}` (no `Bool`).

#### `DisplayAttributeType` enum — all constants (name, ValueType, lowercase `getAttributeName()`)

| Enum constant (`Type` value) | ValueType | `getAttributeName()` | Default (see set below) |
|---|---|---|---|
| `NONE` | STRING | `none` | sentinel; skipped on save/encode |
| `VISIBLE` | BOOLEAN | `visible` | `true` |
| `VISIBLE_AT_DAY` | BOOLEAN | `visible_at_day` | `true` |
| `VISIBLE_AT_NIGHT` | BOOLEAN | `visible_at_night` | `true` |
| `VISIBLE_IN_CREATIVE` | BOOLEAN | `visible_in_creative` | `true` |
| `VISIBLE_IN_SPECTATOR` | BOOLEAN | `visible_in_spectator` | `true` |
| `VISIBLE_IN_STANDARD` | BOOLEAN | `visible_in_standard` | `true` |
| `VISIBLE_TO_OWNER` | BOOLEAN | `visible_to_owner` | `true` |
| `VISIBLE_TO_TEAM` | BOOLEAN | `visible_to_team` | `true` |
| `INTERACTION_WHEN_INVISIBLE` | BOOLEAN | `interaction_when_invisible` | `true` |
| `LIGHT_LEVEL` | INTEGER | `light_level` | `7` |
| `NAME_VISIBILITY` | STRING | `name_visibility` | `ALWAYS` |

`ValueType` enum (`data/type`): `BOOLEAN`, `DOUBLE`, `INTEGER`, `STRING`.

#### Default set (`DisplayAttributeDataSet.createDefaultAttributes()`)

Used when **no** `DisplayAttribute` tag exists: all `VISIBLE*` + `INTERACTION_WHEN_INVISIBLE` = `true`, `LIGHT_LEVEL` = `7` (int), `NAME_VISIBILITY` = `"ALWAYS"`.

> **Authoring nuance (verified in `DisplayAttributeDataCapable`):** reading a present `DisplayAttribute` list **replaces** the default map with only the listed entries. Query fallbacks for an attribute **missing from the map** are `false` (bool), `0` (int), `""` (string) — **not** the create-default values. So a partial list silently turns off any visibility flag you did not include. To keep an NPC fully visible while changing one flag, list every entry you want on.

#### `NAME_VISIBILITY` values — `NameVisibilityType` enum

No custom `toString()`, so the `Text` value is the UPPERCASE enum name. Constants (with `getId()` = ordinal): `NEVER`(0), `ALWAYS`(1), `NEAR`(2), `MID`(3), `MOUSE_OVER`(4). Default `ALWAYS`.

**SNBT example:**

```
DisplayAttribute:[
  {Type:"VISIBLE", Bool:1b},
  {Type:"VISIBLE_AT_NIGHT"},            // Bool omitted → false (hidden at night)
  {Type:"LIGHT_LEVEL", Int:15},
  {Type:"NAME_VISIBILITY", Text:"NEAR"}
]
```

---

### 5. Profession — `Profession` string

Cited: `Profession` (`data/profession`), `ProfessionDataCapable`. Stored as a single String at root key `Profession` = `Profession.name()` (**UPPERCASE**). Default `NONE`. On read, an empty/absent value is skipped (keeps `NONE`); a **non-empty invalid** value calls `Enum.valueOf` with **no fallback** and will throw — spell it exactly.

`Profession` enum — all constants: `NONE`, `ARMORER`, `BUTCHER`, `CARTOGRAPHER`, `CLERIC`, `FARMER`, `FISHERMAN`, `FLETCHER`, `LEATHERWORKER`, `LIBRARIAN`, `MASON`, `NITWIT`, `SHEPHERD`, `TOOLSMITH`, `WEAPONSMITH`.

```
Profession:"LIBRARIAN"
```

---

### 6. Progression — `Progression` compound

Cited: `ProgressionData` (`data/progression`). Root key `Progression`.

| Tag key | Type | Default | Meaning |
|---|---|---|---|
| `EntityExperience` | int | `1`¹ | current XP |
| `EntityExperienceLevel` | int | `1`¹ | current level |
| `AttributeScalingEnabled` | bool | `false` | scale combat/base attributes with level |

¹ `ProgressionData()` no-arg defaults are `(experience=1, experienceLevel=1, attributeScalingEnabled=false)`, used when the `Progression` compound is **absent**. When the compound **is present**, `decode()` reads ints via `getInt` (fallback `0`) and the bool via `getBoolean` (fallback `false`) — so an omitted int key inside a present compound reads `0`, not `1`.

`ProgressionLevelMap` (`data/progression`) is a static XP↔level helper (`MIN_LEVEL`/`MAX_LEVEL`, `getLevelForExperience`, etc.); it holds no NBT keys.

```
Progression:{ EntityExperience:100, EntityExperienceLevel:5, AttributeScalingEnabled:1b }
```

---

### 7. Faction — enums only (not persisted in 6.25.0)

Cited: `FactionType`, `FactionRelationType` (`data/faction`). These enums exist but have **no `*DataCapable` class and no `SynchedDataIndex` entry**, so faction is **not serialized to NBT** in 6.25.0 — there is no preset tag key to author.

- `FactionType`: `ANIMAL`, `ILLAGER`, `UNDEAD`, `VIILLAGER` — note the constant is misspelled **`VIILLAGER`** (double `I`); that is the literal enum name.
- `FactionRelationType`: `NEUTRAL`, `FRIENDLY`, `HOSTILE`, `ALLY`, `ENEMY`.

---

### 8. Scoreboard — `Scores` compound (runtime dialog helper)

Cited: `ScoreboardData`, `ScoreboardOperation` (`data/scoreboard`). `ScoreboardData` is a runtime snapshot of a player's scoreboard values captured for dialog `@score(objective)` macro substitution — **not** a NPC preset field. Shape: root key `Scores` → a compound mapping each objective name (String, matching `[a-zA-Z0-9_.-]+`) to an `int` value. Macro form in dialog text: `@score(objectiveName)`.

`ScoreboardOperation` enum — all constants with their command tokens (`getCommandName()` / `getTranslationKey()`):

| Constant | Command name | Translation key |
|---|---|---|
| `INCREASE` | `increase` | `action.increase_value` |
| `DECREASE` | `decrease` | `action.decrease_value` |
| `SET` | `set` | `action.set_value` |

---

### `SynchedDataIndex` (persistence reference)

Cited: `SynchedDataIndex` (`data/synched`). All constants, all persistent except the two marked transient: `ATTACK_IS_CHARGING_CROSSBOW` *(transient)*, `DISPLAY_ATTRIBUTE_SET`, `ENTITY_ATTRIBUTES`, `MODEL_ANIMATION` *(transient)*, `MODEL_POSE`, `MODEL_POSE_NAME`, `MODEL_POSITION`, `MODEL_ROOT_DATA`, `MODEL_ROTATION`, `MODEL_SCALE`, `MODEL_VISIBILITY`, `NAVIGATION_HOME_POSITION`, `OWNER_UUID`, `PROFESSION`, `PROGRESSION`, `RENDER_DATA`, `SKIN_DATA`, `SOUND_DATA_SET`, `TRADING_DATA_SET`, `TRADING_INVENTORY`, `TRADING_MERCHANT_OFFERS`, `VARIANT_TYPE`. The `public final boolean persistent` field controls whether the value is written to disk NBT.
