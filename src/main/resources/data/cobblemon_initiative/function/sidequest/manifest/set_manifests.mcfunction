# Macro: render the manifest-audit side objective (clone of price_check/set_prices). Arg: manifests.
# Called from the quest/render additions with storage cobblemon_initiative:quest.
$scoreboard players display name q.side_freight ci_quest [{"text":"• Cross-check the freight manifests  $(manifests)/3","color":"yellow"}]
