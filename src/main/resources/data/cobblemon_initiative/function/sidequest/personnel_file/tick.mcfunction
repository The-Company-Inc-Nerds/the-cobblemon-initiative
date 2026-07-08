# The Incomplete File (stage 2) — surveyor sight reset. Register in #minecraft:tick
# (orchestrator wires the tag). Cheap: the outer selector only matches a player who is
# BOTH holding pulled notices (ci_notices >= 1) and not yet done, so it is a no-op the
# rest of the run. The inner check needs a live Company Surveyor placed, tagged surveyor,
# and npcsight-registered — NpcSightManager writes her can_see_player score (scoreboard
# IPC, the documented pattern). If she sees such a player, the audit trail resets to zero.
execute as @a[scores={ci_notices=1..},tag=!notices_filed] at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged

# Pre-pull warning meter: the surveyor is watching before the first notice comes down.
execute as @a[tag=docs_filed,tag=!notices_filed] unless score @s ci_notices matches 0.. run scoreboard players set @s ci_notices 0
execute as @a[tag=docs_filed,tag=!notices_filed,scores={ci_notices=0}] at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run title @s actionbar [{"text":"CLIPBOARD UP","color":"red","bold":true},{"text":" — she is watching this stretch of wall","color":"gray"}]

# Stage 1 pickups are CLICK-based, not proximity: the ledger barrel (2584 107 2925) and the
# portrait chest (2591 111 2815) are handled in Java by docprop.DocPropManager — clicking the
# container swallows the vanilla open and "finds" the document via give_doc_ledger /
# give_doc_portrait. (Container open can only be cancelled from Java, not a datapack.) The old
# walk-near auto-give lived here and was removed 2026-07-08.
