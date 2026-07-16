# The Reach Remembers (Boundary Stones, Q1) turn-in. Run AS the player (Ossa's file button).
# Redundant safety re-check: only fire if all three rubbings are held and not already filed
# (the dialog gate already guarantees this; this guards a hand-typed re-run). economy/payout
# skews the 600 face by the instability haircut; the npc_gift is the keepsake bundle. Latches
# kalahar_claim_filed (later farm_5 liberation callback) + boundary_stones_done (the tombstone).
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run function cobblemon_initiative:economy/payout {amount:600}
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run loot give @s loot cobblemon_initiative:npc_gift/kalahar_ground
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run tag @s add kalahar_claim_filed
# Receipt stings fire on the real turn-in only (before the tombstone tag latches).
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run title @s subtitle [{"text":"ADJUSTMENT: rounding, in the Company favor","color":"gray"}]
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run title @s title [{"text":"CLAIM FILED","color":"gold"}]
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3] run tag @s add boundary_stones_done
