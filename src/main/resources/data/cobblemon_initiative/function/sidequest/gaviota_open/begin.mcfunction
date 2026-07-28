# Gaviota Open (alpha.26 audit rulings) — take the entry fee and start the 300-second
# record round. Run as the player. Sango derby/begin clone at port scale.
# NOTE: cobbledollars remove with an empty wallet is UNVERIFIED — no balance pre-check
# primitive exists; doc fallback is free entry with a smaller purse (Sango caveat).
cobbledollars remove @s 200
tag @s add gaviota_open_active
# Baseline snapshot: only proof items gathered DURING the round count (round-13 exploit
# fix inherited from the Classic — pre-carried items previously auto-won at hand-in).
execute store result score @s ci_open_base_cod run clear @s minecraft:cod 0
execute store result score @s ci_open_base_salmon run clear @s minecraft:salmon 0
execute store result score @s ci_open_base_puffer run clear @s minecraft:pufferfish 0
execute store result score @s ci_open_base_ink run clear @s minecraft:ink_sac 0
scoreboard players set #on ci_open 1
scoreboard players set #time ci_open 300
bossbar set cobblemon_initiative:gaviota_open value 300
bossbar set cobblemon_initiative:gaviota_open players @s
bossbar set cobblemon_initiative:gaviota_open visible true
title @s title [{"text":"THE GAVIOTA OPEN","color":"aqua","bold":true}]
title @s subtitle [{"text":"Ten fish before the clock runs out","color":"gray"}]
# RECORD CATCH OF THE TIDE (Sango chalkboard idiom): roll at entry — 1 finneon / 2
# magikarp / 3 qwilfish / 4 tentacool. Land the rolled species among your ten and
# win_common pays a +100 CD skew-aware bonus. Bonus-only variance, always announced
# (stream-visible roll); the entry fee and purses never move (randomness invariants,
# ENGINE_FINDINGS §3).
execute store result score #species ci_open run random value 1..4
execute if score #species ci_open matches 1 run tellraw @s [{"text":"CHALKBOARD — Record Catch of the Tide: ","color":"aqua"},{"text":"FINNEON","color":"white","bold":true},{"text":" — proof is cod (+100 CD if one lands in your ten)","color":"gray"}]
execute if score #species ci_open matches 2 run tellraw @s [{"text":"CHALKBOARD — Record Catch of the Tide: ","color":"aqua"},{"text":"MAGIKARP","color":"white","bold":true},{"text":" — proof is salmon (+100 CD if one lands in your ten)","color":"gray"}]
execute if score #species ci_open matches 3 run tellraw @s [{"text":"CHALKBOARD — Record Catch of the Tide: ","color":"aqua"},{"text":"QWILFISH","color":"white","bold":true},{"text":" — proof is pufferfish (+100 CD if one lands in your ten)","color":"gray"}]
execute if score #species ci_open matches 4 run tellraw @s [{"text":"CHALKBOARD — Record Catch of the Tide: ","color":"aqua"},{"text":"TENTACOOL","color":"white","bold":true},{"text":" — proof is ink (+100 CD if one lands in your ten)","color":"gray"}]
schedule function cobblemon_initiative:sidequest/gaviota_open/second 1s
