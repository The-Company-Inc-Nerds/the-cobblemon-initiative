# Granary post-trade ambush poller. Runs every tick (cheap: pure score checks).
#
# The Granary keeper at hostile tier (4+ fields liberated) still trades — greed beats
# caution — but the trade button arms granary_ambush_armed=1. This poller gives the
# player ~15s to finish the trade screen, then the keeper files the report and the
# ambush fires: a direct trainer battle vs granary_ambush (villain_team.json), run as
# the player (same tbcs shape content_compile emits for dialog battles).
#
# One-shot: the battle only fires while the player lacks defeated_granary_ambush, and
# the latch resets either way (a beaten keeper does not re-file; an unbeaten one will
# re-arm on the next hostile trade). Single-player: @a == the player.

# countdown — each armed tick increments; 1 -> 300 is ~15 seconds
execute as @a[scores={granary_ambush_armed=1..}] run scoreboard players add @s granary_ambush_armed 1

# the filing — menace beat, then the battle, then reset (order matters: reset last)
execute as @a[scores={granary_ambush_armed=300..}] unless entity @s[tag=defeated_granary_ambush] run tellraw @s [{"text":"The keeper folds the receipt into a grey envelope. ","color":"gray"},{"text":"Asset located. Initiating retrieval.","color":"red"}]
execute as @a[scores={granary_ambush_armed=300..}] unless entity @s[tag=defeated_granary_ambush] run tbcs battle GEN_9_SINGLES @s vs granary_ambush onwin {1: ['cobbledollars add @1 600', 'tag @1 add defeated_granary_ambush'], 2: []}
execute as @a[scores={granary_ambush_armed=300..}] run scoreboard players set @s granary_ambush_armed 0
