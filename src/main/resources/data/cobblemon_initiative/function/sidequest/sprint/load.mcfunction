# Quarterly Sprint — timer objective + notched bossbar setup. NEEDS #minecraft:load
# registration (reported via loadFunctions; function tag JSON is not edited here).
# Idempotent + relog-safe, following the quest/load #init latch pattern.
scoreboard objectives add ci_sprint dummy
scoreboard objectives add ci_sprint_day dummy
execute unless score #init ci_sprint_day matches 1 run bossbar add cobblemon_initiative:sprint [{"text":"⚑ Quarterly Sprint — ring the bell","color":"yellow"}]
execute unless score #init ci_sprint_day matches 1 store result score #last_day ci_sprint_day run time query day
scoreboard players set #init ci_sprint_day 1
bossbar set cobblemon_initiative:sprint color yellow
bossbar set cobblemon_initiative:sprint style notched_10
bossbar set cobblemon_initiative:sprint visible false
