# Sango Classic — the bar emptied. Lose only the entry; Bess invites re-entry. Never a punishment.
scoreboard players set #on ci_classic 0
title @a[tag=classic_active] title [{"text":"TIME","color":"red","bold":true}]
title @a[tag=classic_active] subtitle [{"text":"The quarter closes soft","color":"gray"}]
tellraw @a[tag=classic_active] [{"text":"Bess declares the quarter soft. The sea keeps no other score — enter again whenever the pier calls.","color":"yellow"}]
bossbar set cobblemon_initiative:sango_classic visible false
tag @a remove classic_active
