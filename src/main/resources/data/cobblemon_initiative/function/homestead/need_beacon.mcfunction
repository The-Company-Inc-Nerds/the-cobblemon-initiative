# Homestead onboarding — the player took the ground as a homestead (Deng, home_again) but has NO
# beacon yet (gated: not first_beacon_given). Tell them, and route them to Mayor Liang who gifts the
# first one free. Run AS the player.
#  - Liang freed + first not given + not yet called -> he rings the player to come collect it.
#  - Liang freed + first not given + already called  -> remind them it is waiting at the roof.
#  - Liang not freed yet                             -> point them at him (he is being cornered).
tellraw @s ["",{"text":"You have freed this ground — but you have no beacon to raise on it.","color":"gray"}]
execute if entity @s[tag=defeated_sq_mayor_suits,tag=!first_beacon_given,tag=!call_first_beacon_done] run function cobblemon_initiative:phone/ring_first_beacon
execute if entity @s[tag=defeated_sq_mayor_suits,tag=!first_beacon_given,tag=call_first_beacon_done] run tellraw @s [{"text":"Mayor Liang is holding your first beacon on the gym roof. Go and collect it.","color":"gray"}]
execute unless entity @s[tag=defeated_sq_mayor_suits] run tellraw @s [{"text":"The town keeps its beacons with the mayor — but Company suits have him cornered on the gym roof. Get them off him first.","color":"gray"}]
