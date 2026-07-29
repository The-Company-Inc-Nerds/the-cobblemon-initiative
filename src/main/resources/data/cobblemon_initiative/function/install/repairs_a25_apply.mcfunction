# repairs wave a25 — apply: 0.7.0-alpha.8 playtest wave. Chunks forceloaded by the arm.
# Typed kills only (a10/a13/a14 laws); the fairy kills use limit=1,sort=nearest so each takes
# exactly the targeted household member and never a sibling (Thimble/Bramblea/Wick sit ~2.8
# blocks apart; Wick is NOT moving and must be left standing).

# ── Mystic Marsh ──────────────────────────────────────────────────────────────────
# Liora Starquill (nurse) — uuid nudge +1 z into the Center doorway (bundled-map nbt moved too).
tp f85d9ab0-65ef-4683-905a-edb61a04b848 1175.5 69.0 2361.5

# Korrin Reedshade — uuid tp to the north-boardwalk corner toward Route 3 (bundled-map nbt
# moved too). He is ambient_wander, so ALSO move his Navigation.Home or the soft home-tether
# strolls him back toward the old spot; data modify round-trips through the entity load so the
# stroll-around-home goal picks up the new anchor (self-heals on the next chunk reload too).
tp 30ad5811-874f-43ad-9592-8c1f3bed4b9d 1165.5 69.0 2375.5
data modify entity 30ad5811-874f-43ad-9592-8c1f3bed4b9d Navigation.Home set value {X:1165,Y:69,Z:2375}

# Thimble (household fairy) — re-latch down onto the fen bank (1039.5/68/2510.5). Kill the ONE
# nearest fairy to the old stilt-house spot; Bramblea (2.8b) and Wick (5.7b) are untouched.
execute positioned 1026.5 75.0 2503.5 run kill @e[type=easy_npc:fairy,distance=..2,limit=1,sort=nearest]
scoreboard players set #amb_mm_fairy_thimble ci_ambient 0

# Bramblea Mossglen — re-latch +1 y (75->76). Kill the ONE nearest fairy to her old spot;
# Wick (2.8b) is left standing.
execute positioned 1028.5 75.0 2505.5 run kill @e[type=easy_npc:fairy,distance=..2,limit=1,sort=nearest]
scoreboard players set #amb_bramblea_mossglen ci_ambient 0

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 1168 2352 1182 2366
forceload remove 1116 2372 1168 2410
forceload remove 1022 2500 1034 2510
