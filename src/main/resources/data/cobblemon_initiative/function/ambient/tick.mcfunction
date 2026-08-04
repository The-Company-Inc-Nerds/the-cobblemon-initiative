# Proximity spawn checks for all latch-placed NPCs (generated from `placement` fields).
function cobblemon_initiative:ambient/placements
# Despawn a starter stand-in once its species is claimed. The choose button tags the
# PLAYER claimed_starter_<id> (entity-path @s resolves to the player), so we kill the
# matching stand-in — tagged ci_standin_<species> in its preset NBT — from here. Only
# the claimed one dies; the other two remain as cry-only per the design.
execute if entity @a[tag=claimed_starter_skiddo] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_skiddo]
execute if entity @a[tag=claimed_starter_totodile] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_totodile]
execute if entity @a[tag=claimed_starter_hisuian_growlithe] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_growlithe]
# ── Gift-body despawn sweeps (a18) ──
# Every interact-to-join gift body dies here ONE TICK after its joined tag lands — the dialogs'
# old ExecAsUser `kill` actions were SILENTLY BLOCKED (kill is not an allowlisted ExecAsUser root
# in easy_npc security.cfg), which is why joined bodies lingered (playtest N1). The body-tag guard
# runs first so a long-joined player costs one cheap `if entity` miss per line, not a kill scan.
execute if entity @e[tag=marshadow_lantern_gift] if entity @a[tag=marshadow_joined] run kill @e[tag=marshadow_lantern_gift]
execute if entity @e[tag=latios_gift] if entity @a[tag=latios_joined] run kill @e[tag=latios_gift]
execute if entity @e[tag=ci_manaphy_gift] if entity @a[tag=manaphy_joined] run kill @e[tag=ci_manaphy_gift]
execute if entity @e[tag=ci_revived_kabuto] if entity @a[tag=revived_kabuto_joined] run kill @e[tag=ci_revived_kabuto]
execute if entity @e[tag=ci_revived_omanyte] if entity @a[tag=revived_omanyte_joined] run kill @e[tag=ci_revived_omanyte]
execute if entity @e[tag=ci_revived_aerodactyl] if entity @a[tag=revived_aerodactyl_joined] run kill @e[tag=ci_revived_aerodactyl]
execute if entity @e[tag=ci_revived_lileep] if entity @a[tag=revived_lileep_joined] run kill @e[tag=ci_revived_lileep]
execute if entity @e[tag=ci_revived_anorith] if entity @a[tag=revived_anorith_joined] run kill @e[tag=ci_revived_anorith]
execute if entity @e[tag=ci_revived_cranidos] if entity @a[tag=revived_cranidos_joined] run kill @e[tag=ci_revived_cranidos]
execute if entity @e[tag=ci_revived_shieldon] if entity @a[tag=revived_shieldon_joined] run kill @e[tag=ci_revived_shieldon]
execute if entity @e[tag=ci_revived_tirtouga] if entity @a[tag=revived_tirtouga_joined] run kill @e[tag=ci_revived_tirtouga]
execute if entity @e[tag=ci_revived_archen] if entity @a[tag=revived_archen_joined] run kill @e[tag=ci_revived_archen]
execute if entity @e[tag=ci_revived_tyrunt] if entity @a[tag=revived_tyrunt_joined] run kill @e[tag=ci_revived_tyrunt]
execute if entity @e[tag=ci_revived_amaura] if entity @a[tag=revived_amaura_joined] run kill @e[tag=ci_revived_amaura]
# ── Gravel-quarry suspicious-gravel seeding (a18, Hiro's brush task) ──
# Once per world, when a BRUSH-CARRYING player first reaches the quarry benches (~2035/70/2620)
# — proximity means the chunks are loaded, so seed_quarry's setblocks all land. Zero-init first
# (unset scores fail every matches test).
execute unless score #quarry_seeded ci_ambient matches 0.. run scoreboard players set #quarry_seeded ci_ambient 0
execute if score #quarry_seeded ci_ambient matches 0 if entity @a[tag=sq_museum_brush,x=2035,y=72,z=2620,distance=..48] run function cobblemon_initiative:sidequest/museum/seed_quarry
# ── Victor's descent + reveal (Sango) ──
# Victor waits UP on the grain tower (his placement) until the player earns the transform: the
# five anti-Company completions. On qualifying he comes DOWN to the reveal path — the player
# notices him where he wasn't before (the "something special" signal) — and the reveal then plays
# IN PLACE at the path (a15-safe: no mid-scene camera teleport, the bug that broke the game).
# 1) DESCEND: fire once per qualified player (gate tag=!victor_descended); consume the tower
#    latch, arm the path latch, mark the player, best-effort kill the tower body.
execute as @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp,tag=!victor_descended,tag=!victini_joined] unless entity @e[tag=victor_victini,type=!minecraft:player] run function cobblemon_initiative:sango/victor_descend
# 2) SELF-HEAL: if the descent's kill no-opped because the tower chunk was unloaded, clear the
#    stray tower body whenever it next loads. Position-scoped so it never touches the path body
#    (85 blocks away). tag=!victini_joined latches it OFF once the reveal is complete (else the
#    victini_joined kill of victor_victini would re-open the `unless entity` guard and this would
#    scan forever).
execute if entity @a[tag=victor_descended,tag=!victini_joined] unless entity @e[tag=victor_victini,type=!minecraft:player] positioned 2522.5 131 2815.5 run kill @e[type=easy_npc:humanoid,tag=victor_apprentice,distance=..8]
# 3) ARRIVE: spawn Victor's body at the reveal path the first time the descended player comes
#    within 40 of it (chunk guaranteed loaded), exactly once (#amb_victor_path 0 -> 1).
execute if score #amb_victor_path ci_ambient matches 0 as @a[tag=victor_descended,x=2536.5,y=106,z=2900.5,distance=..40] unless entity @e[tag=victor_victini,type=!minecraft:player] run function cobblemon_initiative:sango/victor_arrive_path
# 4) TRANSFORM: the path-Victor's dialog tags the player victor_transformed; next tick this plays
#    the reveal cutscene IN PLACE. Guard: a DEDICATED one-shot player tag (victor_transform_fired,
#    set on victor_transform's first line) — the old `unless entity victor_victini` guard looped
#    forever (victor_victini only spawns at cutscene tick 60, but this line re-ran victor_transform
#    EVERY tick and CutsceneManager.play() restarts an active scene, so it reset each tick and
#    never reached tick 60 — the player was trapped in a scene that never revealed, a game-break).
execute as @a[tag=victor_transformed,tag=!victor_transform_fired] run function cobblemon_initiative:sango/victor_transform
# Soft-lock recovery: if the scene ended BEFORE its tick-60 swap (mid-scene logout or a
# hardcore death — CutsceneManager.end() fires without spawning Victini), the player keeps
# victor_transform_fired forever and can never re-trigger. Strip it so the one-shot above
# re-dispatches a fresh scene. Guards: gamemode=!spectator (the player is a SPECTATOR for the
# whole live scene, so this NEVER fires mid-scene, which would restart the loop) AND the
# apprentice body must STILL EXIST — i.e. the scene aborted BEFORE the tick-60 swap killed him.
# If the swap already ran (apprentice gone) but Victini never materialised, re-dispatching
# would just replay the scene forever, so we don't: recover only the genuine pre-swap abort.
execute as @a[tag=victor_transformed,tag=victor_transform_fired,tag=!victini_joined,gamemode=!spectator] unless entity @e[tag=victor_victini,type=!minecraft:player] if entity @e[tag=victor_apprentice,type=!minecraft:player] run tag @s remove victor_transform_fired
# Once Victini has joined the player's party, remove the reveal-site Victini NPC.
execute if entity @a[tag=victini_joined] run kill @e[type=easy_npc:cobblemon_npc,tag=victor_victini]
# Kalahar mirage sweep — the gym 6 hunt scatters heat-shimmer fake doubles of the gym cast
# (tag ci_mirage_fake, baked into the summon-only kalahar_mirage_* presets; KalaharManager
# import_new's them on the guide-triggered scatter). The Reach out button runs the Java
# command cobblemon-initiative kalahar reach; on the poof roll KalaharManager.reach() adds
# ci_mirage_popped to that fake, and this sweep plays the FX then kills it one tick later
# (the hostile-Doppler roll instead discards the fake and import_new's a Doppler in Java).
# FX lines first, then the kill.
execute at @e[tag=ci_mirage_popped] run particle minecraft:block{block_state:{Name:"minecraft:sand"}} ~ ~1 ~ 0.4 0.8 0.4 0 50 force
execute at @e[tag=ci_mirage_popped] run particle minecraft:cloud ~ ~1.2 ~ 0.35 0.7 0.35 0.02 30 force
execute at @e[tag=ci_mirage_popped] run particle minecraft:poof ~ ~1 ~ 0.3 0.8 0.3 0.05 40 force
execute at @e[tag=ci_mirage_popped] run playsound minecraft:block.sand.break player @a[distance=..24] ~ ~ ~ 1 0.6
execute at @e[tag=ci_mirage_popped] run playsound minecraft:entity.breeze.idle_air player @a[distance=..24] ~ ~ ~ 1 1.3
kill @e[tag=ci_mirage_popped]
# Aya's reveal — the west-stair groundskeeper (uuid a9ed3a64) was Leader Blossom all along.
# A challenger who cleared all four garden wardens tags themselves aya_transformed via her
# dialog; next tick, positioned at her body, spawn Leader Blossom + despawn the groundskeeper.
# Guard: skip once the leader body already exists, so it happens exactly once.
execute as @a[tag=aya_transformed] at @e[tag=aya_groundskeeper,type=!minecraft:player,limit=1] unless entity @e[tag=hz_leader_body,type=!minecraft:player] run function cobblemon_initiative:hua_zhan/aya_transform
# ── Zwiggo, the swampert (Gaviota easter egg; a22: latch-placed + robust vanish) ──
# The recruitable Swampert now stands on the quay as its own latch body (place/zwiggo_swampert
# at 406.8/64/3501.2 — no more water-summon + walk-up, which was the "large jump" bug). On
# recruit the player gets zwiggo_joined; here, POSITION-INDEPENDENT (the old distance=..3
# pop-tag missed when the body drifted, so it never vanished), FX+kill the body every tick a
# joined player exists. Also self-heals the double-recruit case: if repairs_a22 re-latches a
# body for an already-joined player, this removes it before it can be recruited twice.
execute as @e[type=easy_npc:cobblemon_npc,tag=zwiggo_body] at @s if entity @a[tag=zwiggo_joined] run function cobblemon_initiative:gaviota/zwiggo_pop
