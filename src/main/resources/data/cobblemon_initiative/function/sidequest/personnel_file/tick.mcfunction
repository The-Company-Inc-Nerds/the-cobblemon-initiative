# The Incomplete File (stage 2) — surveyor sight reset. Register in #minecraft:tick
# (orchestrator wires the tag). Cheap: the outer selector only matches a player who is
# BOTH holding pulled notices (ci_notices >= 1) and not yet done, so it is a no-op the
# rest of the run. The inner check needs a live Company Surveyor placed, tagged surveyor,
# and npcsight-registered — NpcSightManager writes her can_see_player score (scoreboard
# IPC, the documented pattern). If she sees such a player, the audit trail resets to zero.
execute as @a[scores={ci_notices=1..},tag=!notices_filed] at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged

# Stage 1 pickups off real container blocks (no prop-NPCs needed). Once the file is open,
# a player who reaches the block "finds" the document, one-shot per doc tag.
#   chest on the cart by Lumo  -> portrait backing
#   barrel by the farm fountain -> re-signed ledger page
execute positioned 2591 111 2815 as @a[distance=..3,tag=file_opened,tag=!doc_portrait] run function cobblemon_initiative:sidequest/personnel_file/give_doc_portrait
execute positioned 2584 107 2925 as @a[distance=..3,tag=file_opened,tag=!doc_ledger] run function cobblemon_initiative:sidequest/personnel_file/give_doc_ledger
