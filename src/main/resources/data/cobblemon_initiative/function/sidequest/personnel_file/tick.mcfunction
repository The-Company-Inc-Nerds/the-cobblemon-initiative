# The Incomplete File (stage 2) — surveyor sight reset. Register in #minecraft:tick
# (orchestrator wires the tag). Cheap: the outer selector only matches a player who is
# BOTH holding pulled notices (ci_notices >= 1) and not yet done, so it is a no-op the
# rest of the run. The inner check needs a live Company Surveyor placed, tagged surveyor,
# and npcsight-registered — NpcSightManager writes her can_see_player score (scoreboard
# IPC, the documented pattern). If she sees such a player, the audit trail resets to zero.
execute as @a[scores={ci_notices=1..},tag=!notices_filed] at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged
