# repairs wave a5 — apply: chunks are live now; sweep the stale monument bodies,
# re-arm their latches, sweep any leaked phase-1 noble bodies, release the chunks.
# Latches respawn each monument at its CURRENT authored coords on the next visit.

# stale monuments at their old posts (groudon's was buried at y110 and likely never
# spawned — sweep defensively)
kill @e[type=easy_npc:humanoid,x=655.5,y=63,z=3300.5,distance=..4]
kill @e[type=easy_npc:humanoid,x=2156.5,y=240,z=884.5,distance=..4]
kill @e[type=easy_npc:humanoid,x=3805.5,y=110,z=3746.5,distance=..4]
scoreboard players set #amb_noble_monument_kyogre ci_ambient 0
scoreboard players set #amb_noble_monument_rayquaza ci_ambient 0
scoreboard players set #amb_noble_monument_groudon ci_ambient 0

# leaked phase-1 bodies from failed starts (tag-unique; the forceloads above make the
# candidate sites loaded so the global selector can reach them)
kill @e[type=easy_npc:cobblemon_npc,tag=noble_kyogre_body]
kill @e[type=easy_npc:cobblemon_npc,tag=noble_rayquaza_body]
kill @e[type=easy_npc:cobblemon_npc,tag=noble_groudon_body]

forceload remove 640 3285 670 3315
forceload remove 685 3315 715 3345
forceload remove 2141 869 2171 899
forceload remove 3790 3731 3820 3761
forceload remove 231 2332 261 2362
forceload remove 717 4574 747 4604
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"World repairs applied — the noble monuments now stand at their arenas.","color":"gray"}]
