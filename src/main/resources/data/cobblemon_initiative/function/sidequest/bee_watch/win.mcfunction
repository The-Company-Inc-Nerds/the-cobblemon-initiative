# Bee-Swarm Wave Defense — the last bee is down. Run as/at the player.
# Sets the turn-in gate tag (bee_watch_cleared, read by the sq_ume_notices turn_in entry) and
# pays the reward the old poster turn-in used: 400 CD + 3x potion + 1x net_ball (hardcore — no
# heal balls) + economy/reward/standard, latched by the dialog's sq_posters_done on collect.
tag @s remove bw_active
tag @s add bee_watch_cleared
# Clear any straggler bees still standing so the dawn field... er, the stall is clean.
kill @e[tag=bw_mob,distance=..128]
bossbar set cobblemon_initiative:bee_watch visible false
title @s title [{"text":"SWARM DOWN","color":"gold","bold":true}]
title @s subtitle [{"text":"Mei's stall stands — go collect your pay","color":"yellow"}]
playsound minecraft:block.bell.use master @s ~ ~ ~ 1 1
tellraw @s [{"text":"Every bee culled off the stall. ","color":"gray"},{"text":"Printmaker Mei owes you.","color":"yellow"}]
