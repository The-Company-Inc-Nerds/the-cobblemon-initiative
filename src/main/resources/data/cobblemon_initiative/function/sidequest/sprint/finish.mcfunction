# Quarterly Sprint — runner crossed the finish zone in time. Run as/at the runner.
tag @s remove ci_sprinting
bossbar set cobblemon_initiative:sprint visible false
playsound minecraft:block.bell.use master @s ~ ~ ~ 1 1
# First completion pays the insured parcel rate; daily rematches pay the courier rate.
execute unless entity @s[tag=race_won] run function cobblemon_initiative:sidequest/sprint/win
execute if entity @s[tag=ci_sprint_daily] run function cobblemon_initiative:sidequest/sprint/win_daily
