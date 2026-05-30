$scoreboard players set $(uuid) sight_range $(range)
$tellraw @a [{"text":"[NPC Sight] Range set to ","color":"green"},{"text":"$(range)","color":"yellow"},{"text":" for ","color":"green"},{"text":"$(uuid)","color":"yellow"}]

