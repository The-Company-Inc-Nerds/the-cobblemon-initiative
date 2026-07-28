# Gaviota Open (alpha.26 audit rulings) — the clock ran out. Lose only the entry; Enzo
# invites re-entry. Never a punishment (Sango fail idiom).
scoreboard players set #on ci_open 0
title @a[tag=gaviota_open_active] title [{"text":"TIME","color":"red","bold":true}]
title @a[tag=gaviota_open_active] subtitle [{"text":"The tide keeps the rest","color":"gray"}]
tellraw @a[tag=gaviota_open_active] [{"text":"The Weighmaster calls the round dead. The entry stays with the house — the Open runs whenever the water does.","color":"yellow"}]
bossbar set cobblemon_initiative:gaviota_open visible false
tag @a remove gaviota_open_active
