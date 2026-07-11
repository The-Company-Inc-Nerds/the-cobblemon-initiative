# Scorchspire "Banked Coals" — the per-second heat step (called by gym/scorchspire_heat
# every 20 ticks; see that file for the full design). Groups run in order: clock reset,
# one-time bossbar init, gain, clamps, badge freeze, threshold pings, away cooling,
# stuck-tag failsafe, bossbar upkeep, high-heat flavor.
#
# Arena box: 3640..3680 / 90..130 / 4650..4690 (x,y,z + d=40 selectors below).
# TODO(showrunner): confirm the box — it must contain the warden cluster
# (3657-3663 / y95 / 4664-4672) and Vulcan at 3660 95 4668, and it must NOT reach the
# gym entrance porch or any healer the challenger retreats to between fights.
scoreboard players set #heat_clock scorchspire_heat 0

# One-time bossbar init, latched on a fake player (scoreboard and bossbar both persist
# with the world, so the latch survives restarts with the bar already added).
execute unless score #init scorchspire_heat matches 1 run bossbar add cobblemon_initiative:spire_heat [{"text":"Spire Heat — do not linger","color":"gold"}]
execute unless score #init scorchspire_heat matches 1 run bossbar set cobblemon_initiative:spire_heat max 120
execute unless score #init scorchspire_heat matches 1 run bossbar set cobblemon_initiative:spire_heat style notched_12
execute unless score #init scorchspire_heat matches 1 run bossbar set cobblemon_initiative:spire_heat visible false
scoreboard players set #init scorchspire_heat 1

# Heat gain: +1/s to anyone loitering in the arena who is not mid-battle and holds no
# Ember Badge. scorchspire_forging = warden-button battle pause; in_trainer_battle
# covers any engage:touch forced fight that strays into the box.
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!scorchspire_forging,tag=!in_trainer_battle,tag=!defeated_scorchspire_leader] run scoreboard players add @s scorchspire_heat 1

# Clamps: ceiling 120; floor 0 (a -30 warden vent can overshoot below zero).
execute as @a if score @s scorchspire_heat matches 121.. run scoreboard players set @s scorchspire_heat 120
execute as @a if score @s scorchspire_heat matches ..-1 run scoreboard players set @s scorchspire_heat 0

# Badge holders run cold — the Ember Badge closes the ledger for good.
execute as @a[tag=defeated_scorchspire_leader] if score @s scorchspire_heat matches 1.. run scoreboard players set @s scorchspire_heat 0

# Threshold pings — heat moves 1/s, so a rising gauge sits on each value exactly once
# (!scorchspire_forging keeps a battle frozen exactly at 60/90 from re-pinging).
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!scorchspire_forging] if score @s scorchspire_heat matches 60 run title @s actionbar [{"text":"The coals reignite — Vulcan will not hold back","color":"red"}]
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!scorchspire_forging] if score @s scorchspire_heat matches 60 run playsound minecraft:block.fire.ambient master @s ~ ~ ~ 1 0.7
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!scorchspire_forging] if score @s scorchspire_heat matches 90 run title @s actionbar [{"text":"The spire howls — move or burn","color":"red"}]
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!scorchspire_forging] if score @s scorchspire_heat matches 90 run playsound minecraft:entity.blaze.ambient master @s ~ ~ ~ 1 0.8

# Away cooling: inside the box the grace refills to 10 s; outside it counts down, and at
# zero the whole run cools to nothing (10 s outside = ~200 ticks = full reset — the
# simplest robust rule; stepping out IS the intended counterplay to a hot gauge).
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] run scoreboard players set @s scorchspire_away 10
execute as @a unless entity @s[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] if score @s scorchspire_away matches 1.. run scoreboard players remove @s scorchspire_away 1
execute as @a unless entity @s[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] if score @s scorchspire_away matches 0 if score @s scorchspire_heat matches 1.. run scoreboard players set @s scorchspire_heat 0

# Failsafe: a scorchspire_forging tag that survives its battle (disconnect mid-fight —
# single-player battles do not outlive the session) clears the moment the player stands
# outside the box, so the heat pause can never wedge open.
execute as @a[tag=scorchspire_forging] unless entity @s[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] run tag @s remove scorchspire_forging

# Bossbar upkeep — visible only while a badge-less player stands in the box; value and
# color track that player (single-player production: limit=1 IS the challenger).
bossbar set cobblemon_initiative:spire_heat players @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader]
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader,limit=1] store result bossbar cobblemon_initiative:spire_heat value run scoreboard players get @s scorchspire_heat
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader,limit=1] if score @s scorchspire_heat matches ..59 run bossbar set cobblemon_initiative:spire_heat color yellow
execute as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader,limit=1] if score @s scorchspire_heat matches 60.. run bossbar set cobblemon_initiative:spire_heat color red
execute if entity @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader] run bossbar set cobblemon_initiative:spire_heat visible true
execute unless entity @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40,tag=!defeated_scorchspire_leader] run bossbar set cobblemon_initiative:spire_heat visible false

# High-heat flavor: at 90+ the arena itself complains — every 5th second (= 100 ticks).
scoreboard players add #heat_fx scorchspire_heat 1
execute if score #heat_fx scorchspire_heat matches 5.. as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] if score @s scorchspire_heat matches 90.. at @s run particle minecraft:flame ~ ~1 ~ 0.7 0.6 0.7 0.02 14 normal
execute if score #heat_fx scorchspire_heat matches 5.. as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] if score @s scorchspire_heat matches 90.. at @s run particle minecraft:lava ~ ~0.2 ~ 0.6 0.2 0.6 0 4 normal
execute if score #heat_fx scorchspire_heat matches 5.. as @a[x=3640,y=90,z=4650,dx=40,dy=40,dz=40] if score @s scorchspire_heat matches 90.. at @s run playsound minecraft:block.lava.ambient master @s ~ ~ ~ 0.8 0.85
execute if score #heat_fx scorchspire_heat matches 5.. run scoreboard players set #heat_fx scorchspire_heat 0
