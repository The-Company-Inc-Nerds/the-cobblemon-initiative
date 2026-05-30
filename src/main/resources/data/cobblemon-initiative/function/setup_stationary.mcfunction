$tag $(uuid) add sight_npc
$tag $(uuid) add sight_run_command
$scoreboard players set $(uuid) can_see_player 0
$scoreboard players set $(uuid) sight_range $(range)
$tellraw @a [{"text":"[NPC Sight] Stationary NPC setup: ","color":"green"},{"text":"$(uuid)","color":"yellow"}]

