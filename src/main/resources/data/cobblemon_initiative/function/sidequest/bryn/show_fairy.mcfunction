# Marsh-Child Bryn — "show them a Fairy-type" (doc 01 §2, tiny flavor errand).
# partyhas tags synchronously, so this same function branches on the probe tag right
# after the check. One-shot via bryn_wisp_done; replays route to the done dialog band.
tag @s remove bryn_fairy_ok
execute unless entity @s[tag=bryn_wisp_done] run cobblemon-initiative partyhas fairy bryn_fairy_ok
execute if entity @s[tag=bryn_fairy_ok] run function cobblemon_initiative:sidequest/bryn/show_fairy_success
execute unless entity @s[tag=bryn_fairy_ok] unless entity @s[tag=bryn_wisp_done] run title @s actionbar [{"text":"Bryn squints at your party. ","color":"gray"},{"text":"\"None of these are the glimmery kind…\"","color":"aqua"}]
tag @s remove bryn_fairy_ok
