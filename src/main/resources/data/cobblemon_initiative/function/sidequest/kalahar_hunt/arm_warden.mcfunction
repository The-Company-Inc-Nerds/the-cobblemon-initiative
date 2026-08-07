# Arm Warden Ossa's WARDEN hunt. Run AS the player (Ossa's "start a hunt" button; the button
# also tags @s warden_hunt_active first). Guard against double-arming: if the warden hunt is
# already active, re-chalk the bearing and bail (never bury a second chest). Otherwise flag it
# active and bury + smoke via the shared arm_apply macro reading storage warden.
execute if score #hunt_warden ci_hunt matches 1.. run tellraw @s [{"text":"Ossa taps her records board. ","color":"gold"},{"text":"That cache is still out on the sand, unfiled — watch for the dust standing where no wind should raise it, and bring the Warden's Cache back to me.","color":"gray"}]
execute if score #hunt_warden ci_hunt matches 1.. run return 0
scoreboard players set #hunt_warden ci_hunt 1
tag @s add warden_hunt_active
function cobblemon_initiative:sidequest/kalahar_hunt/arm_apply with storage cobblemon_initiative:kalahar_hunt warden
schedule function cobblemon_initiative:sidequest/kalahar_hunt/place_warden 2s
tellraw @s [{"text":"Ossa chalks a bearing off her ledger. ","color":"gold"},{"text":"Walk out onto the open sand and watch for the dust-shimmer standing where no wind should raise it. Dig there, and bring the Warden's Cache back to this drawer.","color":"gray"}]
