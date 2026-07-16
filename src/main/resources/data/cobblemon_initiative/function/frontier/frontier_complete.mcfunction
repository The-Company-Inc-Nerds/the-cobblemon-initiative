# Fired from Cave Warden Selene's on_win (execute as @1 = the player), AFTER her defeat_tag
# (frontier_all_cleared) is added in the same onwin array. The Frontier is cleared.
# Grand purse (20000 CD) routed through economy/payout (skewed to the stabilised idx 25 →
# ~75% floor). Idempotent: the payout + forward hook are guarded on one-shot tags so a
# rematch can never double-pay.
title @s title [{"text":"FRONTIER CLEARED","color":"gold"}]
title @s subtitle [{"text":"Every hall bowed. The dark stood down.","color":"gray"}]
execute unless entity @s[tag=frontier_purse_paid] run function cobblemon_initiative:economy/payout {amount:20000}
tag @s add frontier_purse_paid
# Forward hook to the vanilla send-off — printed once, guaranteed (never a random say tail).
execute unless entity @s[tag=frontier_sendoff_seen] run title @s actionbar [{"text":"There is a doorstep waiting past the maps. Go and find it.","color":"dark_green"}]
tag @s add frontier_sendoff_seen
