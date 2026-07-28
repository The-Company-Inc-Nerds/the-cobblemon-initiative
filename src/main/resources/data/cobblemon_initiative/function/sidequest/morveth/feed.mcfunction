# The North Rise Rent (Mystic SQ N22, Morveth the bogged) — REPEATABLE rotten-flesh feed,
# no latch tag BY DESIGN (the witch always takes rent). Run AS the player from the feed
# dialog button. ci_morveth_flesh is a per-run counter; the objectives-add is inline and
# idempotent (quest/render.mcfunction precedent — no #minecraft:load hook required, the
# add simply fails once the objective exists). clear ... 0 COUNTS without removing; the
# paid branch takes exactly 8, then pays 100 via the skewed payout + 2 slime balls
# (bog-rendered). The stored score is read AFTER the paid clear, but store happened
# first, so every branch sees the same pre-clear count — single consistent tick.
scoreboard objectives add ci_morveth_flesh dummy
execute store result score @s ci_morveth_flesh run clear @s minecraft:rotten_flesh 0
execute if score @s ci_morveth_flesh matches 8.. run clear @s minecraft:rotten_flesh 8
execute if score @s ci_morveth_flesh matches 8.. run function cobblemon_initiative:economy/payout {amount:100}
execute if score @s ci_morveth_flesh matches 8.. run give @s minecraft:slime_ball 2
execute if score @s ci_morveth_flesh matches 8.. run title @s actionbar [{"text":"The bogged takes the flesh without hurry. ","color":"gray"},{"text":"Rent received. Rents are receipted.","color":"green"}]
execute if score @s ci_morveth_flesh matches ..7 run title @s actionbar [{"text":"The bogged waits. Eight pieces of rotten flesh make one rent.","color":"gray"}]
