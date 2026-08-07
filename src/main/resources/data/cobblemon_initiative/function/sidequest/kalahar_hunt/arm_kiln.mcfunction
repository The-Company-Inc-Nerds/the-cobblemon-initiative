# Arm Nadia's KILN hunt. Run AS the player (Nadia's "start a hunt" button; the button also
# tags @s kiln_hunt_active first). Guard against double-arming: if the kiln hunt is already
# active, just re-chalk the bearing and bail (never bury a second chest). Otherwise flag it
# active and bury + smoke via the shared arm_apply macro reading storage kiln.
execute if score #hunt_kiln ci_hunt matches 1.. run tellraw @s [{"text":"Nadia nods. ","color":"gold"},{"text":"The cache is still out there where the old clay-run bends — watch for the dust-shimmer standing over the sand, and dig where it stands.","color":"gray"}]
execute if score #hunt_kiln ci_hunt matches 1.. run return 0
scoreboard players set #hunt_kiln ci_hunt 1
tag @s add kiln_hunt_active
function cobblemon_initiative:sidequest/kalahar_hunt/arm_apply with storage cobblemon_initiative:kalahar_hunt kiln
schedule function cobblemon_initiative:sidequest/kalahar_hunt/place_kiln 2s
tellraw @s [{"text":"Nadia chalks the kiln-master's bearing on your map-hand. ","color":"gold"},{"text":"Follow the old clay-run to the bend, and watch for the dust-shimmer standing over the sand — brush the drift off and dig. What is inside is yours.","color":"gray"}]
