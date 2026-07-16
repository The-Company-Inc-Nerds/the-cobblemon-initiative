# The mirror breaks. Run as @s = the player (the Founder's battle on_win calls
# `execute as @1 at @1 run function cobblemon_initiative:reveal/founder_defeated`).
#
# The Founder's nameplate stays fully §k-obfuscated for the whole run; the name is
# only ever spoken HERE — and it is the defeating player's own name, rendered live
# via a selector component. No name is baked into any shipped file.
# The onwin latches company_overthrown (the canon flag); this alias serves the HUD ladder
# and Mom's homecoming entry, which gate on the defeated_<id>-pattern tag.
tag @s add defeated_villain_final_boss
title @s times 20 80 30
title @s subtitle {"text":"The founder, reclaimed.","color":"gold","italic":true}
title @s title {"selector":"@s","color":"gold","bold":true}
tellraw @s [{"text":"The Company is overthrown. The name on the chair was always ","color":"gold"},{"selector":"@s","color":"gold","bold":true},{"text":".","color":"gold"}]
# Minecraft send-off: the story is won, the Ender Dragon fell at Ryujin, the world is open.
# Hand the reclaimed founder the wings to leave the curated map behind and fly into generated
# terrain — the post-story sandbox. Guarded so a rematch/relog cannot re-grant it.
execute unless entity @s[tag=founder_wings_given] run give @s minecraft:elytra 1
execute unless entity @s[tag=founder_wings_given] run give @s minecraft:firework_rocket 32
tag @s add founder_wings_given
tellraw @s [{"text":"The map ends; the world does not. ","color":"dark_green"},{"text":"Beyond its edge is open Minecraft — build, mine, settle, survive. Still hardcore, still Nuzlocke, but the world is yours now. Fly.","color":"gray"}]
