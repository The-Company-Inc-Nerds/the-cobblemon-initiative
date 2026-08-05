# Bruno's "straight to the pit" button (dc_track_pit) + its relaunch button. Referenced by
# deepcore_leader.npc.snbt (the two pit-track buttons) and gym_leader_deepcore.json — it was
# REFERENCED-BUT-MISSING since alpha.27 (a silent no-op; the pit actually opened via the
# deepcore_pvp_tick proximity poll). BUILT a22: fire the pit opener immediately from Bruno.
# Runs AS the player (as_player -> bare function -> execute-as-wrapped).
#
# Guard on stage 0/unset (`unless matches 1..` treats an unset score as not-started, per the
# unset-fails-matches rule): the pit opens only when it has not already started, so a relaunch
# press while a Striker is live or the pit is cleared is a safe no-op (never double-raises).
# After a KO, gym/dojo_reset rewinds #dc_pit_stage to 0, so the relaunch works on the common
# lost-pit path. dojo_pit_striker uses absolute coords + @a[distance] selectors, so no
# positioning is needed here.
execute unless score #dc_pit_stage ci_gym matches 1.. run tellraw @s [{"text":"Bruno: ","color":"gold","bold":true},{"text":"Down the stairs, then. No floor, no crowd — just you and the apprentices. Don't stay down.","color":"white"}]
execute unless score #dc_pit_stage ci_gym matches 1.. run function cobblemon_initiative:gym/dojo_pit_striker
