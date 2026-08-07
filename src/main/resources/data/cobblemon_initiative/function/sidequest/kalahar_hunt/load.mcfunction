# Kalahar TREASURE HUNTS — two repeatable buried-treasure hunts, one per giver (playtest
# 2026-08-06 N18 final design). SHARED find-logic, different payout:
#   KILN  (Nadia Khepra) : buried LootTable chest -> player opens + KEEPS the loot -> done.
#   WARDEN(Warden Ossa)  : buried chest yields ONE marked "Warden's Cache" chest item ->
#                          player carries it back to Ossa -> dialog turn-in pays.
# Each giver's "start a hunt" button ARMS its hunt: bury a chest under sand at that giver's
# FIXED dig site + raise a subtle smoke column there for the player to follow. Repeatable:
# once found (kiln looted / warden cache filed) the giver can arm it again. Guarded against
# double-arming while a hunt is already active for that hunt.
#
# STATE (single-player only per CLAUDE.md, so global fake-player flags are fine):
#   #hunt_kiln   ci_hunt = 1 while the kiln hunt is armed/active   (0 = idle)
#   #hunt_warden ci_hunt = 1 while the warden hunt is armed/active (0 = idle)
#   #hunt_t      ci_hunt = smoke-column tick accumulator
#   per-player script tags kiln_hunt_active / warden_hunt_active mirror the flags for
#   dialog gating + the quest sidebar.
scoreboard objectives add ci_hunt dummy
execute unless score #hunt_kiln ci_hunt matches 0.. run scoreboard players set #hunt_kiln ci_hunt 0
execute unless score #hunt_warden ci_hunt matches 0.. run scoreboard players set #hunt_warden ci_hunt 0
execute unless score #hunt_t ci_hunt matches 0.. run scoreboard players set #hunt_t ci_hunt 0

# FIXED dig sites (macro compounds). cx/cz = the site chunk (forceload during place/find).
# chest x/y/z = the buried chest column; s1/s2 = the two sand blocks above it (dig-through).
# smx/smy/smz = the smoke-column base (chest surface + a little, so the shimmer stands over it).
# loot = the buried chest's loot table.
# --- COORDS AUTHORED FROM THE a38 GUESSES; TERRAIN-UNSCANNED — NEED IN-WORLD VERIFICATION. ---
#   KILN   dig site ~ the clay-run bend SE of the kiln quarter (a38 was 2232/135/4224).
#   WARDEN dig site ~ the open sand E of Ossa's records post (Ossa @ 2050/129/4085).
data modify storage cobblemon_initiative:kalahar_hunt kiln set value {cx:2232,cz:4224,cbx:2232,cby:133,cbz:4224,s1y:134,s2y:135,smx:2232,smy:136,smz:4224,loot:"cobblemon_initiative:sidequest/nadia_kiln_cache",bearing:"Leave the kiln by the smoke-side door and walk toward the low sun until the ground goes pale, then follow the old clay-run to where it bends back on itself."}
data modify storage cobblemon_initiative:kalahar_hunt warden set value {cx:2064,cz:4085,cbx:2064,cby:127,cbz:4085,s1y:128,s2y:129,smx:2064,smy:130,smz:4085,loot:"cobblemon_initiative:sidequest/warden_cache",bearing:"Walk east out of the records post onto the open sand, past the last well-marker, and watch for the dust standing where no wind should raise it."}
