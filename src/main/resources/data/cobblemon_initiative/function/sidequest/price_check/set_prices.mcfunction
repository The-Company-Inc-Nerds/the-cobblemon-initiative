# Macro: render the price-check side objective (clone of quest/set_wheat). Arg: prices.
# Called from the quest/render additions with storage cobblemon_initiative:quest.
$scoreboard players display name q.side_prices ci_quest [{"text":"• Price checks noted  $(prices)/3","color":"yellow"}]
