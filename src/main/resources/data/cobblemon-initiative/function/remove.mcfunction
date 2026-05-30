$tag $(uuid) remove sight_npc
$tag $(uuid) remove sight_track_player
$tag $(uuid) remove sight_run_command
$scoreboard players reset $(uuid) can_see_player
$scoreboard players reset $(uuid) sight_range
$tellraw @a [{"text":"[NPC Sight] Removed NPC: ","color":"red"},{"text":"$(uuid)","color":"yellow"}]

