# First Night Watch — one tick of an active watch. Run as/at the watching player.
execute store result score #t ci_watch run time query daytime
# DAWN: daytime wrapped past 23999 into the morning band with the watcher still tagged.
execute if score #t ci_watch matches 0..11999 run function cobblemon_initiative:sidequest/night_watch/win
execute if score #t ci_watch matches 0..11999 run return 0
# Bossbar = remaining night ticks. Re-show + re-attach every tick (idempotent) so a
# mid-watch relog self-heals without load-order tricks.
scoreboard players set #rem ci_watch 24000
scoreboard players operation #rem ci_watch -= #t ci_watch
execute store result bossbar cobblemon_initiative:night_watch value run scoreboard players get #rem ci_watch
bossbar set cobblemon_initiative:night_watch players @s
bossbar set cobblemon_initiative:night_watch visible true
# Cull ledger: sum the four kill-criteria counters into nw_total.
scoreboard players operation @s nw_total = @s nw_z
scoreboard players operation @s nw_total += @s nw_k
scoreboard players operation @s nw_total += @s nw_p
scoreboard players operation @s nw_total += @s nw_c
# PRESENCE: the Firstfurrow polygon envelope (x 1547-1615, z 2459-2495). VERIFIER FIX (1):
# explicit full-height y bounds — an omitted dy is NOT full height, it is dy=0 at origin y.
execute if entity @s[x=1547,dx=68,y=-64,dy=384,z=2459,dz=36] run scoreboard players set @s nw_grace 0
execute unless entity @s[x=1547,dx=68,y=-64,dy=384,z=2459,dz=36] run scoreboard players add @s nw_grace 1
# 10-second grace (200 ticks) with a standing warning; expiry is fail-soft, never damage.
execute if score @s nw_grace matches 1 run playsound minecraft:block.note_block.didgeridoo master @s ~ ~ ~ 1 0.6
execute if score @s nw_grace matches 1..199 run title @s actionbar [{"text":"Back inside the fence — the watch is failing","color":"red"}]
execute if score @s nw_grace matches 200.. run function cobblemon_initiative:sidequest/night_watch/fail
