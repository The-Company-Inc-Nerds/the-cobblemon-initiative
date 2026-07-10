# Noble Pokémon Encounters

Legends-Arceus-style real-time boss fights. A **noble** spawns into a ring arena, attacks
the player in real time (themed to its typing), and the player dodges while wearing it down
with **melee**. At **stagger** it becomes a real, **catchable** wild Cobblemon. The system
is a reusable, config-driven **engine** in the `noble/` package — each noble is a JSON file
+ an Easy NPC preset, no code per noble.

Subsystem entrypoint: `com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterInit`
(registered in `fabric.mod.json` `main`). It mirrors the shrine subsystem 1:1
(per-player state map + `END_SERVER_TICK` tick loop + Gson config).

---

## The two-phase fight

```
/noble start <id>  (OP 2)   OR   an Easy NPC dialog / story trigger
        │
INTRO ── spawn the Phase-1 Easy NPC body at the arena center; find it by its unique tag;
         open the stagger boss bar; play the intro title + roar
        │
REALTIME ── the Easy NPC natively chases + melees; the manager runs the noble's themed
            ranged/AoE attacks, draws the arena ring (teleport-back at the edge), ticks the
            ambient weather + per-player fake sky, plays the boss-music loop, and mirrors
            the body's HEALTH onto the boss bar (which talks: ⚠ danger / rage tint /
            weakened / grounded / melee hit-flash). Crossing the rage bands (default
            0.6/0.3, `phase1.rageThresholds`) triggers a roar + nova + shove, tightens the
            attack cadence, and unlocks `minRageTier`-gated attacks. The player dodges and
            melees the body down (every landed hit reads as crit-spark + bar flash).
        │  body health ≤ staggerAtHealthFraction · maxHealth
STAGGERED ── a scripted collapse cinematic (~2-5s, no attacks fire): the music stops, the
           body is frozen (setNoAi) and despawned inside a burst while a REAL
           cobblemon:<species> rises at its exact spot playing its actual cry animation,
           then the stagger title lands and the wild battle opens (BattleBuilder.pve).
           Scripts: default cocoon-spiral; "rebirth" (Moltres ember-collapse fake-out);
           "gotcha" (friendly chase freeze-frame — auto for chase type). The boss bar goes
           white-and-empty for the collapse and is removed before Cobblemon's battle UI.
        │
BATTLE ── the player defeats OR catches the noble. Completion is matched by battle-id
          (BATTLE_VICTORY) or captured-Pokémon uuid (POKEMON_CAPTURED).
        │
COMPLETE ── rewards + story-flag scoreboard + advancement + title; arena torn down.
FAILED ──── player death / logout / /noble-abort / battle flee-loss → clean teardown, no penalty.
```

### Why Phase 1 is an Easy NPC
`easy_npc:cobblemon_npc` (a `CobblemonNPC extends PathfinderMobRaw extends Mob`) renders as
the real species model, scales up, carries real `max_health`, takes real melee damage
(`IsAttackableByPlayers`), natively chases + hits the player (`ATTACK_PLAYER` +
`MELEE_ATTACK`), and can float. That gives us the body, model, health, native melee, and an
optional intro dialog for free — so the Java "director" only adds what Easy NPC can't: ranged
fireballs, telegraphed AoE, the arena ring, and the phase orchestration. An Easy NPC is
**not** a real Cobblemon (no ball throw), which is exactly why Phase 2 body-swaps to a real
one for the catch.

Because damage in Phase 1 is native, there is **no** `AttackEntityCallback` and **no**
`UseEntityCallback` — the real Cobblemon only appears at Phase 2 and its battle opens
immediately via `pve`.

---

## Encounter types (`type`)

Phase 2 (the catchable battle) is shared; **Phase 1 varies by `type`**:

- **`boss`** (default) — the combat wear-down above: the body attacks, you dodge + melee it
  down, the boss bar shows its **health**, and it staggers at `staggerAtHealthFraction`.
- **`chase`** — a **friendly, non-combat task** (e.g. Mew). The body is **invulnerable** and
  doesn't attack; it **flees and blink-teleports** around the arena. You have to **tag** it
  (reach within `touchRadius`) `tagsRequired` times — herd it against the ring to corner it.
  The boss bar shows **task progress** (tags landed). When the task completes it "tires out"
  and becomes the catchable wild Cobblemon (same body-swap). Its preset carries **no** attack
  objectives and `IsInvulnerable:1b`; `chase: { tagsRequired, fleeRadius, touchRadius,
  fleeSpeed, tagCooldownTicks, hoverHeight, blinkChance, blinkRange }` tunes it, and
  `staggerTitle`/`staggerSubtitle` reskin the transition ("GOTCHA!" rather than "STAGGERED").

This is the extension point for more friendly/task nobles — add a new `type` + a `tick<Type>`
branch (hide-and-seek, fetch, riddle…), all reusing the Phase-2 catch.

---

## The engine

### Attack primitives (`NobleAttacks`)
One JSON `type` → one handler. Each is generic and reads its cosmetics/damage/effect from the
noble's element theme. Handlers spawn transient combat objects onto `NobleEncounterState`;
the manager advances them each REALTIME tick (`NobleAttacks.tickTransients`).

| `type` | behavior | params |
|---|---|---|
| `projectile` | N element-themed, dodgeable traveling bolts toward the player | `count`, `speed`, `spread`, `damage`, `lifeTicks` |
| `barrage_aoe` | telegraphed ground impacts (warning ring → strike) | `strikes`, `pattern` (`single`/`circle`/`line`/`random`), `radius`, `damage`, `knockback`, `windupTicks`, `delayBetween`, `tracking` |
| `beam` | a telegraphed line from the noble → damage along the segment | `length`, `width`, `windupTicks`, `damage` |
| `hazard_zone` | a persistent field: ticks damage/effect, optional pull | `radius`, `durationTicks`, `pull` (`none`/`toward`/`away`), `tickDamage` |
| `bolt_strike` | vertical column strike(s) at the player (lightning/pillar/meteor) | `count`, `radius`, `damage`, `windupTicks`, `delayBetween`, `tracking` |
| `dive_charge` | flyer/charger slam: body lunges + a tracking impact | `impactRadius`, `damage`, `knockback`, `windupTicks` |
| `stomp` | melee-range shockwave | `range`, `damage`, `knockback`, `windupTicks` |

Damage never touches blocks (no explosions) — safe on the UPM map. Global difficulty scalar:
`NobleConfig.attackDamageMultiplier`.

### Element themes (`ElementTheme`)
`element` resolves the particle, telegraph color, damage source, on-hit status, cast
sound, **windup sound** (the accelerating telegraph metronome — soonest impact only,
white-hot ring flash in the final 5 ticks), and **impact sound** (each element detonates
with its own voice) — so one primitive reads as lava, tidal wave, or blizzard. Keys:
`fire`, `water`, `ice`, `electric`, `dragon`, `ground`, `rock`, `flying`, `dark`, `steel`,
`psychic` (blend keys like `ground_fire` resolve to the first recognized token).

Per-attack overrides ride the opaque `params` object: `castSound`/`castVolume`/`castPitch`
(the cast voice), `impactSound` (the detonation), and `minRageTier` (hold the move until
the noble enrages). All cast/windup pitches are multiplied by a rage factor that rises as
the body weakens, clamped 0.5–2.0.

### Voice, music, sky
- **Species cries** (`cobblemon:pokemon.<species>.cry`, derived from the first
  `battleSpecies` token, override via `sounds.cry`): under the start title, at the
  REALTIME flip (full volume), on each rage band (pitched down per tier), the wounded
  stagger bellow, and the victory cry (pitch tells caught vs beaten). `NobleFx.playSoundId`
  falls back to `SoundEvent.createVariableRangeEvent` for unregistered ids — never remove
  that fallback or every cry silently dies (asset-only events are not in the registry).
- **Phase-1 boss music**: `sounds.loop` + `sounds.loopSeconds` — vanilla music is ducked
  (`stopsound` on MUSIC) and the track loops on HOSTILE at the player's position; stopped
  at stagger + teardown. `sounds.hornOnStart` adds the global wither horn at the flip.
- **Phase-2 battle theme**: `data/cobblemon_initiative/species_additions/<species>.json`
  sets Cobblemon's per-species `battleTheme` to `cobblemon_initiative:noble_battle`
  (events defined in `assets/cobblemon_initiative/sounds.json` as references to vanilla
  tracks — Cobblemon plays, loops, and ducks it natively; the default wild theme is empty).
- **Fake sky** (`NobleSkyFx`): per-player weather packets from the ambient theme
  (downpour/blizzard/thunderstorm → rain; drought → clear + noon time-lock re-sent every
  10 ticks through BATTLE); restored in teardown on every exit path.
- **Hardcore heartbeat**: warden heartbeat under 40% player HP, doubling under 20%.
- **Flyer drama**: takeoff wing-beat + swoop, airborne flap ping every second, descent
  spiral telegraph, landing thud + **bell = the punish window is open**, actionbar
  countdown while grounded.

### Ambient themes (`AmbientTheme`)
An encounter-level arena aura: a particle wash + a mild, periodically-refreshed status.
Keys: `drought`, `downpour`, `blizzard`, `sandstorm`, `thunderstorm`, `gravity`, or `null`.
Reuses the shrine idioms (refreshed blindness / slowness / freeze).

### Flyer mechanic
`flyer: { hoverHeight, groundedWindowTicks, airTicks }` — the body hovers out of melee reach
and attacks from the air, then descends to a **grounded window** where it's meleeable, then
takes off. Melee wear-down only lands during the window. Omit `flyer` (null) for grounded
nobles (always meleeable).

---

## Config schema

`src/main/resources/data/cobblemon_initiative/noble_encounters/<id>.json`:

```jsonc
{
  "id": "groudon",
  "displayName": "§4§lGroudon — Noble of the Scorched Depths",
  "element": "fire",                                   // ElementTheme key
  "bodyPreset": "easy_npc:preset/cobblemon/noble_groudon.npc.snbt",
  "bodyTag": "noble_groudon_body",                     // unique tag baked into the preset
  "battleSpecies": "groudon level=70",                 // PokemonProperties for the Phase-2 catch
  "startTitle": "§4§lGROUDON", "startSubtitle": "§7The mountain wakes...",
  "introSeconds": 4,
  "arena": { "center": [0,0,0], "radius": 20, "dimension": "minecraft:overworld", "boundaryParticle": "minecraft:flame" },
  "ambientTheme": "drought",                           // AmbientTheme key or null
  "flyer": null,                                        // or { hoverHeight, groundedWindowTicks, airTicks }
  "stagger": { "staggerAtHealthFraction": 0.15, "bossBarColor": "RED", "bossBarOverlay": "NOTCHED_10",
               "script": null },                       // null | "rebirth" | "gotcha"
  "phase1": {
    "attacks": [ { "type": "...", "cooldownTicks": N,
                   "params": { "...": "…", "castSound": "?", "castVolume": 1.0, "castPitch": 1.0,
                                "impactSound": "?", "minRageTier": 0 } } ],
    "attackGapTicks": 25,                               // global telegraph-spacing gap
    "rageThresholds": [0.6, 0.3]                        // omit for the engine default
  },
  "phase2": { "healPartyBeforeBattle": true },
  "rewards": {
    "storyFlag": { "objective": "defeated_noble_groudon", "holder": "@s", "value": 1 },
    "achievement": "cobblemon_initiative:nobles/groudon",
    "commands": [ "loot give {player} loot cobblemon_initiative:npc_gift/training_grand" ],
    "commandsOnCapture": null, "commandsOnDefeat": null // outcome overrides (else "commands")
  },
  "completeTitle": "§6§lNoble Subdued", "completeSubtitle": "§7Groudon acknowledges you",
  "sounds": { "start": "...", "stagger": "...", "complete": "...",
              "loop": "minecraft:music.dragon", "loopSeconds": 125,  // Phase-1 boss music
              "cry": null,                       // override the derived species cry
              "hornOnStart": false,              // global wither horn at the REALTIME flip
              "startVolume": null, "startPitch": null, "staggerPitch": null, "completePitch": null }
}
```

Reward commands use `{player}`/`{uuid}` substitution and the shared economy/loot conventions
(a noble is a one-time "grand" completion).

---

## How to add a noble

1. **Encounter JSON** at `noble_encounters/<id>.json` (schema above). Pick an `element`, an
   `ambientTheme`, whether it's a `flyer`, the `phase1.attacks`, and `battleSpecies`.
2. **Easy NPC preset** at `data/easy_npc/preset/cobblemon/noble_<id>.npc.snbt`. Copy
   `noble_groudon.npc.snbt` and change: `RenderData.EntityModel` = `cobblemon:<species>`,
   `PresetMetadata.name`, `data.CustomName`, `data.Tags` = `["noble_<id>_body"]`,
   `data.Health` + the `minecraft:generic.max_health` attribute, and `ModelData.Root.Scale`.
   Keep the `ObjectiveData` (ATTACK_PLAYER/MELEE_ATTACK/LOOK_AT_PLAYER — **never empty**) and
   the `EntityAttribute` block.
3. **Advancement** at `advancement/nobles/<id>.json` (mirror `groudon.json`).
4. **Register the id** in `NobleEncounterManager.NOBLE_IDS`.
5. Optionally set a story gate / trigger (an Easy NPC dialog button running `noble start <id>`,
   or a shrine-crystal-style item). The `noble` command root must be in the
   `EasyNpcSecurityConfig` allowlist for a dialog button to run it.

That's it — no Java for a standard noble. Add Java only for a genuinely novel attack primitive.

---

## Shipped roster

| Noble | element | flyer | ambient | signature attacks |
|---|---|---|---|---|
| **Groudon** | fire | no | drought | magma `projectile`, fissure `barrage_aoe`, lava-pool `hazard_zone`, `stomp` |
| **Kyogre** | water | no | downpour | water-pulse `projectile`, tidal `barrage_aoe` (line/push), whirlpool `hazard_zone` (pull), tidal `beam` |
| **Rayquaza** | dragon | ✅ | — | dragon-pulse `beam`, draco-meteor `bolt_strike`, air-slam `dive_charge`, `projectile` |
| **Articuno** | ice | ✅ | blizzard | ice-shard `projectile`, blizzard `barrage_aoe` (circle), ice-patch `hazard_zone`, icicle `bolt_strike` |
| **Zapdos** | electric | ✅ | thunderstorm | lightning `bolt_strike`, charge `beam`, static-field `hazard_zone`, spark `projectile` |
| **Moltres** | fire | ✅ | drought | flame-pillar `bolt_strike`, fire-spiral `projectile`, sky-fire `barrage_aoe` |
| **Mew** | psychic | — | — | **`chase` (friendly)** — flees + blink-teleports; tag it 6× to befriend it |

**Pseudo-legendaries** (future, showrunner-selected; each is data-only): Tyranitar
(rock/dark, sandstorm), Garchomp (dragon/ground), Dragonite (dragon/flying), Metagross
(steel/psychic, gravity), Salamence (dragon/flying), Hydreigon (dark/dragon).

---

## Commands
- `/noble start <id>` — begin an encounter (OP 2)
- `/noble stop` — abort (OP 2)
- `/noble list` — list ids (OP 2)
- `/noble-abort` — player-facing withdraw (no OP)
- `/cobblemon-initiative reload` — reload noble configs + `NobleConfig`

## ModMenu knobs (`NobleConfig`, `config/cobblemon-initiative-noble.json`)
Exposed under the **Noble Encounters** category of the ModMenu config screen:
`noblesEnabled`, `arenaRadiusMultiplier` (scales every arena's authored ring radius —
default **1.4 ≈ double the arena AREA**; applied at `/noble start`, the JSON radius stays
canonical; the music-loop range auto-scales to cover the ring), `bossHealthMultiplier` and
`bossMeleeDamageMultiplier` (applied once to the body's attributes at discovery — the
preset's authored values stay canonical), `attackDamageMultiplier` (themed ranged/AoE
damage, live), `bossBarEnabled`, `ringPushback`, `sfxVolume`, `sfxPitch`. Feel/difficulty
only — encounter identity lives in the per-noble JSON.

---

## Showrunner marks (set before shipping)
- **Arena `center` + `dimension`** per noble (themed sites on the UPM map) — currently
  `[0,0,0]` (falls back to the player's position at `/noble start`, fine for dev).
- Per-noble **Phase-1 max health**, `staggerAtHealthFraction`, **body `Scale`**, attack
  damage/cooldowns, and **Phase-2 `battleSpecies` level** (set for catchability under the cap
  ladder).
- **When** each noble unlocks (gate `/noble start` behind a story flag if not always-available).
- Which **pseudo-legendaries** to ship.

## Verify (runtime — no unit tests)
`gradle build` → `dev_sync` a world → `/noble start groudon`: boss-sized model, boss bar
tracks health, native chase+melee, themed ranged/AoE telegraph + land, ambient reads, ring
shoves you back, melee lowers the bar; at the threshold a catchable Groudon battle opens;
rewards/flag/advancement granted; `/noble-abort`, logout, and death each tear down cleanly
(no orphan body / stuck bar); no block damage on terrain. Then test a **flyer** (e.g.
`rayquaza`) for the grounded-window + `beam`/`bolt_strike`/`dive_charge` primitives.

## Key files
- Java: `noble/{NobleEncounterInit, NobleEncounterManager, NobleAttacks, NobleFx,
  ElementTheme, AmbientTheme, NobleEncounterConfig, NobleEncounterState, NobleCommands}`,
  `config/NobleConfig`
- Data: `noble_encounters/<id>.json`, `easy_npc/preset/cobblemon/noble_<id>.npc.snbt`,
  `advancement/nobles/<id>.json`
