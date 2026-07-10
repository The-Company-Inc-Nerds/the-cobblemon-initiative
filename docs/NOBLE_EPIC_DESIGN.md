# Noble Encounters — Epic-ness Upgrades & the Mini-Noble Layer

Design document, 2026-07-09. Produced from a multi-agent survey + design + adversarial
feasibility review of the shipped `noble/` subsystem. **Every API/packet/sound/property claim
below was verified against the pinned jars** (Cobblemon 1.7.3 remapped jar, mojmap 1.21.1)
or the live source unless explicitly marked *needs-verify*. Line refs: M =
`NobleEncounterManager.java`, A = `NobleAttacks.java`, S = `NobleEncounterState.java`,
F = `NobleFx.java`, C = `NobleEncounterConfig.java`.

Two deliverables in one doc:
1. **Part 1 — make the seven shipped fights epic** (effects, SFX, music, set pieces).
2. **Part 2 — the mini-noble layer** ("Asset Recovery Program"): 12+ smaller nobles placed
   across the gym ladder, wagering the hardcore run against perfect-IV/EV prize Pokémon.

---

## 0. Verified foundations (read before implementing anything)

### Bugs found during the review — fix these regardless of scope

1. **The engine cannot roar.** `NobleFx.playSoundId` (F:25-32) gates on
   `BuiltInRegistries.SOUND_EVENT.getOptional(rl)` — but Cobblemon's **1061 species cries**
   (`cobblemon:pokemon.<species>.cry`, verified present for the entire noble roster **and**
   all six pseudo-legendaries) are asset-only, never registry-registered. Every cry id
   passed to this helper **silently plays nothing**. Fix: fall back to
   `SoundEvent.createVariableRangeEvent(rl)` (jar-verified: `ClientboundSoundPacket`
   carries the Holder directly; `/playsound` works the same way). Bonus: variable-range
   events project `16 × max(volume,1)` blocks — volume 4.0 = a 64-block roar (used by the
   approach cue below). *This one 3-line fix unblocks most of the audio plan.*
2. **`sfxPitch` ignored** at M:412 (stagger) and M:446 (complete) — hard-coded `1.0f`.
3. **`arena.boundaryParticle` is dead config** (C:63): set in all 7 JSONs, never read —
   the ring always draws element dust. Either wire it or drop it from the JSONs.
4. **CLAUDE.md's "safe zones suspend Nuzlocke" is not implemented** —
   `NuzlockeInit.handleBattleFainted` (373-421) has no zone check; zones only suppress mob
   spawns. Noble Phase-2 battles carry full Nuzlocke stakes *everywhere*. Part 2's wager
   design leans on this; the CLAUDE.md claim should be corrected separately.

### Verified capability baseline

- **Perfect-IV rewards are pure JSON.** `PokemonProperties.Companion.parse` keys, bytecode
  verified: per-stat `<stat>_iv` / `<stat>_ev` with **British spelling** (`hp, attack,
  defence, special_attack, special_defence, speed`; IV coerced 0-31, EV 0-252 per stat —
  510 total NOT enforced, keep spreads legal by construction), **`min_perfect_ivs=N`**
  (shuffles which stats get 31s), `nature` (bare `adamant` resolves to `cobblemon:adamant`
  — verified), `ability`, `shiny`, `held_item`, `moves` (comma-split showdown ids),
  `level`, `form`, `gender`, `friendship`, `pokeball`, `nick`. `battleSpecies` already
  flows through the full parser (M:417) → **zero code needed** for prize tuning.
- **`PokemonEntity.cry()` is public** — plays the real cry + posable animation on the
  Phase-2 entity (sends `PlayPosableAnimationPacket`). No registry issues.
- **Phase-2 is musically silent by default**: Cobblemon's `battle.pvw.default` event has
  an **empty** sounds list. Nothing to clash with; a noble battle theme is free real estate.
  Two data-only routes verified (see §1.6).
- **Level cap = XP clamp only** (`InitiativeInit:250-274`): nothing blocks catching,
  fielding, or obedience of an at/over-cap mon — it just earns 0 XP until the cap passes
  it. **An at-cap prize catch is immediately usable in the next gym.** Caveat: every
  capture arrives **fainted at 0 HP and goes to the PC** (`NuzlockeInit:269-282`) — all
  reward copy must tell the player to revive it.
- Vanilla 1.21.1 spectacle tools (all jar-verified): `ClientboundHurtAnimationPacket(int id,
  float yaw)` (directional red tilt, no damage); `ClientboundGameEventPacket`
  RAIN/THUNDER_LEVEL_CHANGE + START/STOP_RAINING (per-player fake weather — persists,
  vanilla only re-broadcasts on real change); `ClientboundSetTimePacket` (fake sky —
  **must re-send every ≤15 ticks**, vanilla re-syncs at `tickCount % 20`);
  `LightningBolt.setVisualOnly(true)` (**caveat**: `powerLightningRod()` and
  `clearCopperOnLightningStrike()` are NOT visualOnly-gated — guard strikes against
  weathering copper / lightning rods or you permanently mutate the UPM map);
  `ClientboundStopSoundPacket`; Display entities driven via NBT (`transformation`,
  `interpolation_duration`, `start_interpolation`, `teleport_duration`); LevelEvent horns
  1023 (wither spawn) / 1028 (dragon death, both global), 2001 (block-crack particles+sound,
  client-only), 2003 (eye-of-ender shimmer).
- `ServerBossEvent.setName/setColor` are **unused seams** (set once at start today) and
  no-op + skip broadcast when unchanged — safe to drive per tick.
- `PendingImpact.totalWindup` / `PendingBeam.totalWindup` (S:87, S:121) are **written by
  every attack and read nowhere** — a free hook for telegraph escalation.
- Client-side work is legitimate: single source set, in-process single-player. Precedents:
  `NuzlockeClientInit` static-flag poll (22-28), `QuestTrackClient` integrated-server read.
  No custom payloads needed, ever.

---

## Part 1 — Making the seven fights epic

Ordered by build phase, not by beat. Costs: S ≈ ≤40 lines or JSON-only; M ≈ one focused
feature; L ≈ new phase/state machinery.

### 1.1 Phase A — the voice pass (all S, ship together, highest identity-per-line)

All six bosses currently share **one identical sound trio** (ender_dragon.growl /
generic.explode / challenge_complete). Articuno opening a fight sounds exactly like Groudon.

| Change | Seam | Notes |
|---|---|---|
| **Fix the cry gate** (§0 bug 1) | F:25-32 | Prerequisite for everything below |
| **Species cry everywhere**: `sounds.start` → `cobblemon:pokemon.<id>.cry`; full-volume cry (3.0 → 48-block) + explosion crack at the INTRO→REALTIME flip (M:240, currently silent); wounded cry pitch 0.6 + `elder_guardian.curse` sting at stagger; triumphant cry pitch 1.25 on complete | JSON + M:240, M:411, M:445 | `cryId(cfg)` helper: `sounds.cry` override else `"cobblemon:pokemon." + first-token-of-battleSpecies + ".cry"` |
| **`gr.cry()` on the Phase-2 spawn** — the real model rears and roars as it materializes | after M:422 | Works today, no dependency |
| **Per-attack `castSound`/`castVolume`/`castPitch`/`impactSound` params** — `Attack.params` is an opaque JsonObject by design (C:110-117); one `castCue()` helper replaces the six cast sites (A:89, 124, 153, 178, 212, 228) and **adds the missing stomp sound** (A:181-195 is the only silent cast); `impactSound` rides the transient (new String field on PendingImpact/ProjectileBolt/PendingBeam, read at A:284/251/313) | NobleAttacks + JSON | Bolt hits (A:251-255) are currently **completely silent** — fix via element impact default |
| **Element windup + impact sound columns on `ElementTheme`** (two new enum columns + getters): fire=`block.fire.ambient`/`generic.explode`, water=`bubble_column.upwards_ambient`/`player.splash.high_speed`, ice=`glass.place`/`glass.break`, electric=`sculk_sensor.clicking`/`lightning_bolt.impact`, dragon=`ender_dragon.ambient`/`dragon_fireball.explode`, psychic=`amethyst_block.resonate`/`amethyst_block.chime` — fill all 11 so mini-nobles inherit the palette | E:20-30 | Replaces the universal generic.explode impact (A:284, A:313) |
| **Telegraph metronome**: accelerating `note_block.hat` tick + pitch ramp at every impact point as it arms; last 5 ticks flip the dust ring **white** + bigger size (drawRing size param — hardcoded 1.4f at F:43) | A:270-278, A:297-301 | Use `progress = Mth.clamp(1 - min(ticksLeft, totalWindup)/totalWindup, 0, 1)` — naive formula goes negative on staggered barrage strikes. Metronome only the **soonest** pending impact to avoid barrage cacophony |
| **Flyer wing audio + punish-window bell**: takeoff = `ender_dragon.flap` + `phantom.swoop`; soft `phantom.flap` every 20t while airborne (track the boss by ear); landing = thud (`generic.explode` 0.6/0.7 + `ravager.step`) then `block.bell.resonate` at +5t — "bell = GO" teaches the melee rhythm in two cycles | tickFlyer M:581-598 | **Careful: the flip lines are inverted vs intuition** — M:590 `setAirborne(false)` is the LANDING (thud+bell), M:595 `setAirborne(true)` is the TAKEOFF (flap+swoop) |
| **Hardcore heartbeat**: below 40% player HP, `warden.heartbeat` every 1.5s; below 20%, every 0.75s, pitch tightened. The player's hearts are the real boss bar on a hardcore stream | after M:275 | Registered vanilla id — no cry-fix dependency. Skip for chase type |
| **Sfx pitch bugs** (§0 bug 2) + `sounds` schema extension: `loop`, `loopSeconds`, `cry`, `approach`, `hornOnStart`, nullable per-cue volume/pitch overrides | C:138-142 | Gson ignores unknown keys — fully backward compatible |

### 1.2 Phase B — game-feel (S/M)

- **Melee hit-confirm** (M): the core loop is "melee it down" and a landed hit currently
  gives only vanilla feedback. Track `lastHealthFraction` on state; on any health drop:
  CRIT-particle burst scaled to damage, `entity.player.attack.crit` chirp pitch-scaled,
  boss bar flashes WHITE 3 ticks. Big hits (≥4% max HP) get a louder layered confirm —
  viewers read DPS off the screen. Compute the fraction **from the body, not inside the
  `bar != null` branch** (works with bossBarEnabled=false).
- **Directional hurt tilt** (S): element damage sources are positionless so the vanilla
  red tilt is directionless. In `hit()` (A:343-347, already has fromX/fromZ):
  `ClientboundHurtAnimationPacket(target.getId(), yaw)` where
  `yaw = atan2(fromZ−z, fromX−x)·180/π − getYRot()` — **no −90 offset** (vanilla hurtDir
  convention; the −90 variant reads 90° wrong). Also on hazard-zone ticks (A:331-334).
- **Arena-edge curtain** (S): the barrier is invisible until it rubber-bands you. When the
  player is within 1.5 blocks of the ring, draw a bright vertical dust **arc** (player
  azimuth ±0.26 rad, heights 0-3) on their side; the repel itself gains a GUST puff +
  element flash. New `NobleFx.drawArc(...)` = the drawRing loop with a clamped angle range.
- **Boss bar as a combat channel** (S): `⚠` name prefix while any telegraph is live;
  under 25% body health flip YELLOW + "— Weakened!" so the stagger never comes from
  nowhere. Priority if everything ships: **rage tint > weakened > danger > hit-flash**.
- **Screen shake via in-process bridge** (M, optional): no packet exists for real shake.
  New `NobleClientBridge` static volatile fields (the pendingSacrifice/QuestTrackClient
  idiom); server writes intensity in `hit()`; client END_CLIENT_TICK applies
  `mc.player.turn(...)` jitter. Verifier notes: `turn()` internally scales by 0.15 (retune
  amplitude up), the random walk leaves residual aim offset (store/restore pre-shake
  rotation), gate on `client.screen == null`.

### 1.3 Phase C — rage bands (M, the escalation spine)

Today literally nothing changes between 100% health and the 15% stagger check (M:267).
Add `rageTier` + `lastHealthFraction` to state; thresholds from optional
`phase1.rageThresholds` (default `{0.6, 0.3}`), detected right after the bar update
(M:261-264, fraction computed from the body). On a downward crossing:

- **Roar**: species cry pitched one step deeper per band (0.85 → 0.75 → 0.65), 40-particle
  element nova + `EXPLOSION_EMITTER` at the body, no-damage `NobleFx.knockback` (0.8, 0.3)
  + `ClientboundHurtAnimationPacket` — the roar physically shoves you.
- **Bar**: `setColor` (element → YELLOW → RED) + `setName("§c‼ " + displayName)`; flash
  title `§c§lENRAGED` (5/20/10); actionbar flavor.
- **Cadence**: `globalAttackGap = 25 − 5·tier`; per-attack cooldowns × `(1 − 0.2·tier)`
  at M:545-546.
- **Held-back moves**: optional `minRageTier` params key, checked in the ready-gather loop
  (M:537-541 — `attacks.get(i).params` is already in scope). Rayquaza only dive-charges
  when furious; Moltres gains its missing 4th attack (closing fire ring) below 33%.
- **Ambient escalation hook**: pass the tier into `AmbientTheme.tick` (both call sites,
  M:275 + M:319, chase passes 0) — powers the Articuno frost vignette (§1.7).
- A final pre-stagger band at `staggerAtHealthFraction + 0.10` queues a faint
  `warden.roar` under-layer: "ready your dodge and your Pokéballs."

### 1.4 Phase D — the overture (intro, M)

The 4-second intro currently draws a dust ring and nothing else.

- **Body materializes** in an element burst + cloud puff (hook after the `import_new`
  command in spawnBody, M:602-608).
- **The sky answers**: new `NobleSkyFx` helper wrapping per-player weather/time packets,
  mapped from `AmbientTheme` — DOWNPOUR/BLIZZARD → rain 1.0, THUNDERSTORM → rain+thunder
  1.0, DROUGHT → stop rain + fake noon. Fake time must re-send every ≤15 ticks — **including
  `tickBattle`**, or the sky snaps back mid-catch (verifier catch); restore in `teardown()`
  (M:463-482), which every exit path already funnels through (logout self-heals — vanilla
  re-sends weather/time on join).
- **Ambient wash during intro** — run the ambient particle tick in tickIntro (M:233 area),
  but **skip `applyPeriodic`** so blizzard/sandstorm debuffs can't hit during the "safe" intro.
- **The horn**: at the INTRO→REALTIME flip (M:240), LevelEvent **1023** wither-spawn horn
  (global) + the full-volume cry. Gate the horn behind `sounds.hornOnStart` (weather trio
  only — it should mean something).

### 1.5 Phase E — the stagger set piece (L): `Phase.STAGGERED`

The money shot is currently a single-tick despawn/spawn masked by a title. Make it a
3.5-second collapse cinematic. Add `STAGGERED` to the Phase enum (S:18-24) + `staggerTicks`;
split `enterStaggerAndSwap`:

- **t=0**: existing transient clears + ambient clear (M:402-406); bar → progress 0,
  `setColor(WHITE)`; pin the body **and `((Mob) body).setNoAi(true)`** — the verifier
  proved the pin alone is NOT safe: the Easy NPC's native melee can still one-tap a low-HP
  hardcore player mid-cinematic; horn 1028 (dragon death); cry pitch 0.6.
- **t=0-40**: spiral cocoon — `drawRing` radius shrinking arena→0.5, heights rising,
  element dust + END_ROD.
- **t=40**: `despawnBody` inside a 40× CLOUD + EXPLOSION_EMITTER + FLASH burst; run the
  existing spawn block; **spawn at the body's captured X/Z but clamp Y to the arena floor**
  (flyers stagger mid-air; the real mon shouldn't fall into its own battle); `gr.cry()`.
- **t=40-70**: STAGGERED title (per-noble `staggerTitle` — a shipped field only Mew uses).
- **t=70**: healParty + `BattleBuilder.pve` → Phase.BATTLE.
- No attacks fire in STAGGERED (fireAttacks only runs in tickRealtime) — hardcore-safe.
  Route the body-killed-early path (M:254-256) and chase completion (M:338-340) through it.
  Death/logout during STAGGERED already funnels to teardown cleanly.

### 1.6 Phase F — music (M)

Phase 1 plays over whatever calm vanilla track is on; Phase 2 plays over **silence**.
All Mojang OST = Twitch/YouTube-safe (streaming rights granted, incl. Lena Raine discs).

- **Phase-1 loop**: at the REALTIME flip send `ClientboundStopSoundPacket(null, MUSIC)`,
  then loop a per-noble track on **HOSTILE** (deliberately not MUSIC/RECORDS — Cobblemon
  pauses those in Phase 2): weather trio `music.dragon`, Articuno `music_disc.ward`,
  Moltres `music_disc.pigstep`, Zapdos `music_disc.precipice` (1.21), Mew
  `music_disc.creator_music_box`. `sounds.loop` + `sounds.loopSeconds` (server can't read
  ogg length — JSON declares it; treat listed durations as tunables). Verifier fixes:
  re-trigger **at the player's position** each cycle (positional at arena center pumps/fades
  across a 20-block ring), re-send the MUSIC stop on each re-trigger (vanilla can schedule
  a new track mid-fight), null-guard the stop packet in teardown (logout path).
  Stop on HOSTILE at stagger for a clean handoff to…
- **Phase-2 battle theme, two data routes** (pick one):
  - *(a) species_additions* — `Species.battleTheme` is a data-driven mutable property;
    ship `data/cobblemon_initiative/species_additions/<species>.json` with
    `{"target": "cobblemon:groudon", "battleTheme": "cobblemon_initiative:noble_battle"}`.
    Loaded at startup, zero code, Cobblemon plays/loops/ducks it natively. (The originally
    proposed `species_feature`/`species/` paths are **wrong** — features are aspects, and a
    species file would replace the base species.)
  - *(b) BattleMusicPacket* — after `pve()` send
    `com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket` (jar-verified
    package + ctor `(ResourceLocation, float, float, boolean)`; `sendToPlayer` is a
    NetworkPacket default method). Lets the theme vary per **element** rather than species.
  - Either way the sound events ship in our own `assets/cobblemon_initiative/sounds.json`
    as **event-type references to vanilla tracks** (`{"name":"minecraft:music.dragon",
    "type":"event","stream":true}`) — zero ogg files. Recommend (a) for simplicity; (b) if
    per-element theming is wanted. Don't ship both with §Phase-1's "let the loop run through
    Phase 2" fallback — pick one owner for Phase-2 audio.

### 1.7 Phase G — per-noble signatures (each independently shippable)

| Noble | Signature | Key mechanics (verified) |
|---|---|---|
| **Groudon** | *Continental Stomp* — sub-bass rear-up (+0.35 vertical hop, the diveCharge idiom), impact = LevelEvent **2001** magma-block crack (client-only particles+crunch) + expanding triple crack-ring + hurt-tilt/CONFUSION jolt for everyone in range×1.5 | FX flag must ride `PendingImpact` — impact resolution happens in tickImpacts, not the handler. The shrine earthquake (ShrineChallengeManager:843-882) is the jolt precedent |
| **Kyogre** | *The arena drowns* — beam casts slam fake rain to max + the ring becomes a 5-high FALLING_WATER curtain for the beam window; at the 33% band the **whole arena becomes a whirlpool**: directly construct a HazardZone (radius=arenaRadius, pull=toward, tickDamage 0.5 raw, 100t) — reuses the entire zone tick path incl. pull physics | Needs a `drawRing` particle-type overload (dust is hardcoded). Direct construction bypasses `mult()` — set tickDamage deliberately, playtest the stack with melee |
| **Rayquaza** | *Midnight set piece* — sky locked to 18000 (resend ≤15t incl. tickBattle), aurora halo = **3 phase-offset arc segments** at y+12 rotating by gameTime (a full ring reads as static — verifier), contrail requires **orbiting the airborne pin** (angle from gameTime) since the current pin is motionless, dive = LevelEvent 1016 sonic boom + dust ribbon (needs a small diveTicks state) + guarded visual bolt | The only lvl-75 noble deserves the flagship treatment |
| **Zapdos** | *Real lightning* — `"lightning": true` params flag on bolt_strike → visual-only `LightningBolt` at each column impact under the fake thunderstorm | Flag rides PendingImpact (set at cast). **Copper/rod guard mandatory** (§0) — skip the bolt entity over WeatheringCopper/lightning rods, element particles still fire |
| **Articuno** | *Creeping frost* — drive the vanilla powder-snow vignette via `setTicksFrozen`: hold at 0.4×/0.7× of `getTicksRequiredToFreeze` per rage band. The stakes render on the stream frame with zero HUD code; frost shatters (2001 packed-ice ×3 + glass.break) at stagger | Strictly below the freeze threshold → freeze damage can never tick (same clamp the code already uses). `clear()` already resets. Cleanest proposal in the whole review |
| **Moltres** | *False death* — scripted stagger variant (`stagger.script: "rebirth"`): collapse in falling embers + fire.extinguish, 2s of smoke and near-silence (free — transients already cleared), then an 80-flame IGNITION eruption + full cry, title "FROM ASHES". Plus the rage-gated 4th attack (§1.3) | Boss bars have no BLACK — blackout beat = `setProgress(0)` + PURPLE or `setVisible(false)` |
| **Mew** | *Escalating tag* — blinks get louder/higher per tag (enderman.teleport out, amethyst chime in, giggle = cry ×1.3-1.5); near-miss whiff + hot/cold chime — pitch formula `0.8 + 1.2·(touchRadius+1.2−dist)/1.2` (the naive `2.2−dist` clamps flat at 0.5 — verifier); penultimate tag = hearts trail + "One more!"; finale = freeze-frame GOTCHA through Phase.STAGGERED (face the player, 3 rising allay chimes, enchant spiral, then the shipped `staggerTitle`) | Tag counter is `state.getTaskProgress()` (not getChaseTagsLanded). Pitch ladder: `1.2 + 0.12·taskProgress`. Escalate fleeSpeed/blinkChance from taskProgress — locals, don't mutate config |

### 1.8 Phase H — danger discs (L, ambitious, biggest telegraph upgrade)

Replace "faint dust ring you might miss" with MMO-language **growing translucent glowing
glass discs** (BlockDisplay, element-colored stained glass) that scale from 0.2 to the full
blast radius over the windup and vanish at impact; beams get a stretched glowing bar.
Verifier corrections that make it actually work:
- Build the update tag via `entity.saveWithoutId(new CompoundTag())` then merge display
  keys — `Entity.load()` with a tag missing `Pos` resets the entity to (0,0,0).
- `setGlowingTag(true)` (the NBT key is `Glowing`, capitalized).
- BlockDisplays scale from the block's **min corner** — animate translation `[-r, 0, -r]`
  alongside scale or the disc grows off-center.
- Track display UUIDs on PendingImpact/PendingBeam; discard at impact, at the stagger
  clears (M:402-405), **and in `teardown()` (M:463)** — teardown does not currently clear
  transients, and display entities are real saved entities: skip this and glowing discs
  persist into the hardcore save after death/logout.

### 1.9 Bookends (S)

- **Victory**: cry pitch 1.2, LevelEvent 2003 eye-of-ender shimmer, 60-FIREWORK fountain
  (HEARTs for Mew), sky restore. Differentiate by outcome: capture = levelup chime layer;
  KO = deeper, solemn cry (viewers hear caught-vs-beaten).
- **Failure/withdraw**: `StopSound(HOSTILE)` cuts all combat audio dead, one cry at 0.5,
  3s creeping DARKNESS, actionbar `§8It watches you go.` — **gate the dread package on
  `!player.isDeadOrDying()`** (fail() also handles hardcore death; don't play theater over
  the death screen).

---

## Part 2 — The mini-noble layer: "Asset Recovery Program"

**Fantasy**: the minis are Company property — escaped specimens from the bio-logistics
division, each with an asset ledger number. The player, amnesiac ex-CEO, is unknowingly
recovering assets they originally requisitioned. Surveyor dialog drops one throwaway line
per act: the requisition signature is one the player "might recognize."

**The wager** (all mechanics already live, verified): Phase-1 attacks are real player
damage in a hardcore world → death ends the run. Phase 2 is a real Nuzlocke wild battle →
each faint chips the player (`maxHealth/partySize`) **and releases the fainted mon**; a
wipe force-kills the player; fleeing sacrifices a party mon. The engine's own fail path is
penalty-free — the risk is all Nuzlocke. The prize is a Pokémon no hardcore Nuzlocke player
could ever grind safely.

### 2.1 Roster

**Warden tier** (gyms 1-6; each typing counters the NEXT gym — the catch is the run-saving
prize). Uses the dead ElementThemes (GROUND, FLYING, STEEL, ROCK, DARK) + dead AmbientThemes
(SANDSTORM, GRAVITY). All sites are `mobsSpawn:true` stakes-live zones except where flagged:

| # | Warden | Element/ambient | Site (window) | Countering | battleSpecies core |
|---|---|---|---|---|---|
| R-001 | Onix | ground/sandstorm* | Blossom Path [2226,~,2744] (15-22) | Bug gym 1 | `onix level=15` + 4×31 IV |
| R-002 | Staraptor | flying/none, short-air flyer | Harvest Road (22-30) | Grass gym 2 | `staraptor level=22` |
| R-003 | Steelix | steel/gravity | Willowmire Path [1353,~,2330] (30-37) | Fairy gym 3 | `steelix level=30` |
| R-004 | Alakazam | psychic/gravity, frail + ranged-heavy | Quarry Road [1225,~,2651] (37-44) | Fighting gym 4 | `alakazam level=37` |
| R-005 | Ampharos | electric/thunderstorm | Gullwing Coast lighthouse [852,~,3349] (44-50) | Water gym 5 | `ampharos level=44` — Amphy homage |
| R-006 | Milotic | water/downpour, whirlpool pull | Old Caravan Road (50-56) | Ground gym 6 | `milotic level=50` |

*Verifier: soften SANDSTORM for Onix — its 80-tick blindness every 3s is harsher than any
permitted gym-1 hit (see difficulty table). Cries + geo models jar-verified for all six.*

**Executive Asset tier** (gyms 7-10 + endgame; the Board's personal specimens — four map to
the four canon Board members `defeated_board_{madeline,matt,micah,lauren}`, the other two
belong to DJ and **a director who was erased from the records** — amnesia-arc breadcrumb):

| # | Executive | Element/ambient | Site (window) | Countering | Level |
|---|---|---|---|---|---|
| X-01 | Garchomp | ground/sandstorm | Kalahar desert edge, Pylon Path (56-62) | Electric gym 7 | 56 |
| X-02 | Dragonite | dragon, flyer | Dragonspine Pass [2017,~,1076] (62-68) | Dragon gym 8 | 62 |
| X-03 | Metagross | steel/gravity | Frostveil Pass [3092,~,2466] (68-74) | Ice gym 9 | 68 |
| X-04 | Tyranitar | rock/sandstorm | Cinderfall Descent [3380,~,3066] (74-80) | Fire gym 10 | 74 |
| X-05 | Salamence | dragon, flyer | **ROAMS** late-game routes (80-85) | Royal League | 80 |
| X-06 | Hydreigon | dark (blind+darkness on-hit = set dressing) | **NOT Deep Dark Cave** — mobsSpawn:false + roadmap gives the Deep Dark to Giratina; use a stakes-live Frontier Causeway leg (85-100) | Battle Frontier | 85 |

Executives' flyers use shorter air time than the birds ({6.5, 130, 150} vs 170) — faster
melee rhythm. Attack kits reuse shipped primitives: Tyranitar/Garchomp stomp + bolt_strike
"rock pillar"; Metagross hazard_zone pull "magnetic well" + beam; Hydreigon tri-head
projectile ×3 + beam.

**Chase counterpoint**: Alpha **Zorua** in the Safari Zone (mobsSpawn:false — the one region
where a no-stakes encounter belongs), 5 tags, `zorua level=56 min_perfect_ivs=3
nature=timid` — deliberately weaker prize. The design statement: perfect mons are PAID FOR
in hardcore risk; calm zones pay calm prizes. **Conflict to rule on**: roadmap 16 already
assigns the Safari Zone to Mew's chase (post-gym-7) — either Zorua is the explicit warm-up
act (56-62) with Mew as the payoff, or Zorua relocates.

### 2.2 Rewards — the mon IS the prize (zero code)

Pure `battleSpecies` string edits, all keys bytecode-verified:

- **Wardens**: four deterministic 31s in the stats that matter + half EV spread + nature +
  held item + curated moves. Deterministic beats `min_perfect_ivs` here — min_perfect
  shuffles which stats get 31s, and a physical Garchomp with a wasted 31 SpA is a dud prize.
  Example: `ampharos level=44 hp_iv=31 special_attack_iv=31 defence_iv=31 speed_iv=31
  nature=modest special_attack_ev=128 hp_ev=128 held_item=cobblemon:magnet
  moves=thunderbolt,dragonpulse,cottonguard,voltswitch`
- **Executives**: five 31s + full 252/252 + competitive ability/item/moves. Example:
  `garchomp level=56 hp_iv=31 attack_iv=31 defence_iv=31 special_defence_iv=31 speed_iv=31
  nature=jolly attack_ev=252 speed_ev=252 ability=roughskin
  moves=earthquake,dragonclaw,stoneedge,swordsdance`
- **Legendaries**: append `min_perfect_ivs=6` to all 7 shipped JSONs — all-perfect stays
  exclusive to the top tier. (This also fixes the critic's "the flagship legendary is a
  random-IV empty-handed mon" gap.)
- Remaining clerical work: validate each held_item/move id offline against the jar
  (established procedure in `reference_item_id_validation`).
- **Loot realignment**: `training_grand`'s own comment declares it "the ONLY GRAND pack in
  the game," yet all 7 nobles already pay it. Wardens → `training_standard`, Executives →
  `training_major` (capture path only; KO consolation = standard), legendaries keep grand.

### 2.3 Level rule + legendary retune

Prize level = **the window's entry cap** (usable immediately — §0; XP resumes next badge).
The shipped legendaries are all 70/75 and mistuned for their intended eras (a Kyogre in its
post-gym-5 era is unwinnable in-window). Retune per roadmap 16's proposed numbers — Kyogre
50, Zapdos 60, Mew 60, Rayquaza 66, Groudon 78 — **or** get an explicit ruling for at-cap
(the roadmap prefers cap-minus-2; the difference is a showrunner call, don't silently
override). Articuno/Moltres are dropped from roadmap 16's roster (replaced by
Regirock/Uxie/Celebi/Manaphy/Giratina) — park their levels at the nearest window without
asserting new era placements until ruled.

### 2.4 Placement & gating — Field Surveyors

One latch-placed Company "Field Surveyor" grunt at each arena: bounty-board dialog ("Asset
R-042 sighted. The Company pays for recovery. Engage?") → button runs bare `noble start <id>`.
The trigger owns one-time-ness (the engine deliberately has no repeat gate — TODO.md:34).
Verified plumbing:
1. Add `noble` to `REQUIRED_ROOTS` (EasyNpcSecurityConfig.java:42-48) — already a TODO.
2. **Tag mirror**: noble completion writes only a scoreboard flag, but Easy NPC conditions
   read TAGS. Append to `rewards.commands` (BOTH capture and KO lists): `tag {player} add
   defeated_noble_<id>`. Declare the not-gate in dialog-src and **let the band_tags compiler
   own the `no_defeated_noble_<id>` inverse** (PLAYER_TAG ignores NOT_EQUALS; hand-wiring
   the inverse into the install latch is fragile — compiler ownership is relog-safe).
3. Surveyor NPCs via the `#amb_<key>`/`ci_ambient` placement latch + `npc_preset_builder`;
   action gates via the DOUBLED `ConditionDataSet` key; badge-window honesty gate via the
   existing `badges_gte_N` band tags (the fight is *offerable* early — the gate is honesty,
   not a wall).

### 2.5 Roaming asset — Salamence (L, set piece)

One executive roams instead of being placed (resolves the design conflict: Dragonite stays
placed — its 62-68 window needs a findable counter; Salamence hunts). On stakes-live routes
in the 80-85 window: rare roll → herald beat — distant cry, actionbar "Something enormous is
circling overhead…", cries repeating louder over 90s. Stand your ground (stay within 24
blocks) → it dives and the encounter starts **at the player's position** — the engine's
(0,0,0) player-feet fallback becomes the feature. Run → it passes over, roll re-arms.
Verifier requirements: new `RoamingNobleScheduler` must (a) skip players in ANY Cobblemon
battle (BattleRegistry) or shrine challenge, not just `hasActive`; (b) own its herald-state
cleanup on logout/death/server-stop; (c) gate on cap achievements or the `badges_gte_7` band
tag (PlayerProgress has no badge-count getter); (d) one-shot latch via `noble_roam_<id>`
scoreboard. Player-feet arenas on water/cliff terrain are untested — runtime-verify.

### 2.6 The wager's teeth

- **Capture-or-lose-it** (S, the core mechanic): split `complete()` by outcome — CAPTURE = the
  perfect mon + pack + "Asset Recovered"; KO = consolation pack + "Asset Decommissioned",
  and the asset is **gone for the run**. Every extra turn spent weakening/balling under
  Nuzlocke rules is voluntarily-held risk; one crit too many destroys the prize forever.
  **CRITICAL TRAP (verifier)**: a successful wild capture fires BOTH `PokemonCapturedEvent`
  AND `BattleVictoryEvent` (proven by NuzlockeInit's existing `getWasWildCapture()` guard).
  In `onBattleVictory`, check `event.getWasWildCapture()` — otherwise a real catch can pay
  the KO consolation and consume the state before `onPokemonCaptured` runs. Implement as
  optional `commandsOnCapture`/`commandsOnDefeat` on Rewards (falls back to `commands` —
  backward compatible).
- **Flee economics** (M): `/noble-abort` stays a free clean Phase-1 escape (never trap a
  hardcore player); Phase-2 flee already costs a sacrificed mon (global rule). Add the
  re-attempt price: a 250 CD "Re-engagement Bond" at the Surveyor before re-offering —
  inside the established 120-250 fee band, fixed price per the no-random-prices invariant.
  Verifier: tag `noble_fled_<id>` only from `abort()` and the tickBattle flee branch (a
  bare fail() hook would charge 250 CD for engine errors like a missing preset); build the
  pay button on the **heal_paid pay-probe pattern** — `cobbledollars remove` clamps at 0
  and would let broke players re-engage free.
- **Difficulty table** (Phase 1, under the legendaries' 220-300 HP / 6.0 melee / 0.15 stagger):

| Tier | HP | Melee | Attacks | Per-hit dmg | Stagger | Scale | Radius |
|---|---|---|---|---|---|---|---|
| Warden (gyms 1-2) | 60-80 | **≤3.0 hard ceiling** | 2 | ≤3.0 (no-armor player must survive ≥4 clean hits — the no-death-loop principle) | 0.25 | 1.5-1.8 | 14 |
| Warden (gyms 3-6) | 90-150 | 3.0-4.0 | 2-3 | 2-5 | 0.25 | 1.8-2.2 | 16 |
| Executive | 200-240 | 5.5-6.0 | 3-4 | 5-7 | 0.18 | 2.0-2.5 | 18 |
| Legendary (shipped) | 220-300 | 6.0 | 3-4 | 4-9 | 0.15 | 2.0-3.0 | 20 |

- **Pacing knob** (S): promote `ATTACK_GAP_TICKS` to optional `phase1.attackGapTicks`
  (default 25) — Onix telegraphs lazily at 40, Hydreigon swarms at 15. ~6 lines, verified
  backward compatible.
- **Consent ritual** (from the critic — the run-ending risk must never be accidental): the
  Surveyor dialog IS the waiver — "Sign here. The Company is not liable for dismemberment."
  Confirm-to-start, so a hardcore death at a noble is always a dramatized choice. (The
  alternative — clamping Phase-1 damage at half a heart — is rejected: it deletes the wager
  that makes the prize legitimate.)

### 2.7 Showrunner rulings needed before authoring

1. At-cap vs cap-minus-2 prize levels (§2.3).
2. Articuno/Moltres roster status vs roadmap 16's replacement five.
3. Onix site vs the proposed Celebi grove [2050,~,2700]; Milotic's Oasis vs Regirock's
   claim on the only Oasis landmark [1735,64,4255].
4. Zorua vs Mew ownership of the Safari Zone.
5. Firstfurrow Farm is a conditional wheat-war zone — Staraptor moved up Harvest Road.
6. Which two executives belong to DJ / the erased director.

---

## Part 3 — Critic gaps (extensions worth their own passes)

1. **Advancement flavor**: per-noble advancements are bare one-liners. Add "Hostile
   Takeover" (all-7 meta), hidden feats as clip markers (flawless stagger, first-ball catch,
   survive a rage tier at <2 hearts), "Asset Recovery Program: Ledger Closed" chain,
   capture-vs-KO variant text — all in the corporate-memo voice.
2. **Rumor pipeline**: nobles have zero `quest_targets.json` entries. A "Noble Sightings"
   stage per noble — barkeep/Surveyor dialog sets a sighting flag → trackable target at the
   arena (region-center "last reported grid" for the roamer, so the hunt stays a hunt) →
   JM waypoint clears on completion. Turns every noble into discoverable content instead of
   a showrunner `/noble start`.
3. **Post-catch specialness**: nickname beat after capture ("The ledger requires a name for
   this asset" — a natural 30-second stream moment). A permanent "Noble" aspect/mark via
   PokemonProperties is *needs-verify* (aspect keys weren't in the verified list).
4. **Rematch mode**: after the story flag, `/noble start` re-runs Phase 1 only (no catch,
   no grand loot), small CD purse, scoreboard best-time ("previous record: 4:12"), optional
   "Audit" remix tier (rage tiers start active, +1 attack, 85-100 scaling) — the subsystem
   stays alive post-Royal-League instead of dying after 7 fights.
5. **Shrine → noble summon keys**: clearing an elemental shrine awards the shrine-crystal
   item that unlocks the matching-element noble (fire→Moltres/Groudon, ice→Articuno,
   electric→Zapdos, water→Kyogre) + a post-shrine herald beat (title + distant roar +
   sighting waypoint). Gives shrines a payoff beyond themselves and nobles a diegetic gate.
6. **Fight recap card**: on COMPLETE/FAILED, hold the frame ~3s and print a Company
   incident report ("ASSET R-001 REACQUISITION REPORT"): duration, damage taken, closest
   call, rage tiers survived, tags landed, balls thrown. Persist per-noble bests (existing
   Gson pattern). The natural clip out-point; free VOD timestamps.
7. **Chase depth**: escalate the mechanic itself (later tags raise fleeSpeed/blinkChance;
   decoy after-images on blink) and prove the `tick<Type>` extension point with one
   hide-and-seek variant (body relocates between 3 fixed spots in a town) before the finale
   needs it.

---

## Build order

1. **Foundations** (one PR-sized pass): cry-gate fix + pitch bugs + `cryId()` helper +
   sounds schema extension + per-attack sound params + `min_perfect_ivs=6` on the 7
   legendaries + capture/KO split with the `getWasWildCapture` guard + `attackGapTicks`.
2. **Phase A/B bundles**: voice pass JSONs, telegraph metronome, hit-confirm, hurt tilt,
   heartbeat, flyer audio, edge curtain, bookends.
3. **Rage bands** (the spine everything hangs on) + Articuno frost + Moltres 4th attack.
4. **Overture + music** (NobleSkyFx, Phase-1 loop, species_additions battle theme).
5. **STAGGERED set piece** + Mew finale + Moltres rebirth script.
6. **Warden tier** (6 JSONs + presets + advancements + Surveyors + gating plumbing) —
   after the showrunner rulings in §2.7.
7. **Executive tier** + Salamence roamer + Zorua.
8. **Ambitious extras as stream-schedule allows**: danger discs, Rayquaza midnight set
   piece, screen shake, recap card, rematch mode, shrine keys, rumor pipeline.

Runtime verification stays per `docs/NOBLE_ENCOUNTERS.md` (no unit tests): every new beat
must survive the teardown matrix — abort, logout, death, server-stop — with no orphan
bodies, bars, sky state, loops, or display entities.
