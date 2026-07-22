# PokéPhone VIDEO-CALL delivery (macro). Called AS @s (the player) from phone/ring_<id> after
# the ring flavor (actionbar + chime). Spawns the caller AT the player — always loaded, so a call
# still never dies on an unloaded chunk (the one virtue of the old text system, kept) — then opens
# their Easy NPC call dialog at $(label): the caller's face renders in the dialog GUI, their lines
# read as the call, and a "Hang up" close button ends it. The caller's ON_CLOSE_DIALOG trigger
# (easy_npc delete @s) despawns it on hang-up; the pre-spawn delete below sweeps any straggler.
# $(caller)=preset filename, $(tag)=body tag, $(label)=dialog entry label.
# IN-WORLD TUNING: if the dialog doesn't open the same tick as the spawn, split the open into a
# 1-tick schedule; if the caller shoves the player on spawn, offset the import (~ ~ ~1) or add NoAI.
$execute at @s run easy_npc delete @e[type=easy_npc:humanoid,tag=ci_phone_caller,distance=..64]
$execute at @s run easy_npc preset import_new data easy_npc:preset/humanoid/$(caller).npc.snbt ~ ~ ~
$execute at @s run easy_npc dialog open @e[tag=$(tag),limit=1,sort=nearest] @s $(label)
