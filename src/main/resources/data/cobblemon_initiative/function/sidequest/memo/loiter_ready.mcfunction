# 8 unseen seconds held at the tent flap. Run as the player.
tag @s add memo_loiter
scoreboard players reset @s ci_loiter
# Direct-open the eavesdrop the moment it arms (Easy NPC distance-event dedup would
# otherwise require a full leave+re-enter of the 16-block band — a silent dead end).
# ckpt_listener is applied to the FIELD AGENT body at placement (runbook protocol).
execute at @s run easy_npc dialog open @e[tag=ckpt_listener,limit=1,sort=nearest] @s overheard_memo
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8
