# Proximity spawn checks for all latch-placed NPCs (generated from `placement` fields).
function cobblemon_initiative:ambient/placements
# Claimed starter stand-ins despawn one tick after the choose click (the choose
# button tags the NPC entity-path; killing NEXT tick lets the dialog CLOSE packet
# go out first — killing mid-action-list would race the deferred close).
kill @e[type=easy_npc:cobblemon_npc,tag=ci_claimed_standin]
