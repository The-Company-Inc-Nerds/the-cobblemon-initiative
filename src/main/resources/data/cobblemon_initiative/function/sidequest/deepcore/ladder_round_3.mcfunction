# The Iron Ladder - Round 3, the last swing (Deepcore SQ2). Run as the player (dialog
# button, gated defeated_sq_ladder_2 + not_tag defeated_sq_ladder_3). NO heal between rounds.
# Tag-only onwin (defeated_sq_ladder_3 arms the claim button); the purse is paid at ladder_claim.
execute if entity @s[tag=defeated_sq_ladder_2] unless entity @s[tag=defeated_sq_ladder_3] run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_3 onwin {1: ['tag @1 add defeated_sq_ladder_3', '@2 say Three, no water, still standing. That is the ladder. Old Dun will want to see you.'], 2: ['@1 say The last rung is iron for a reason. You reached it - that is more than most. Heal up and take the swing again.']}
