# The mirror breaks. Run as @s = the player (the Founder's battle on_win calls
# `execute as @1 at @1 run function cobblemon_initiative:reveal/founder_defeated`).
#
# The Founder's nameplate stays fully §k-obfuscated for the whole run; the name is
# only ever spoken HERE — and it is the defeating player's own name, rendered live
# via a selector component. No name is baked into any shipped file.
title @s times 20 80 30
title @s subtitle {"text":"The founder, reclaimed.","color":"gold","italic":true}
title @s title {"selector":"@s","color":"gold","bold":true}
tellraw @s [{"text":"The Company is overthrown. The name on the chair was always ","color":"gold"},{"selector":"@s","color":"gold","bold":true},{"text":".","color":"gold"}]
