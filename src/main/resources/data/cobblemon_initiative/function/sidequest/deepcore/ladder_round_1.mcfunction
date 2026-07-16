# The Iron Ladder - Round 1 (Deepcore SQ2). Run as the player (dialog button, gated
# iron_ladder_active + not_tag defeated_sq_ladder_1). NO heal is ever run between rounds -
# the party carries damage forward, the whole point of the gauntlet. Tag-only onwin (no cash;
# the purse is paid once at ladder_claim). onwin lists are single-quoted SNBT.
execute unless entity @s[tag=defeated_sq_ladder_1] run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_1 onwin {1: ['tag @1 add defeated_sq_ladder_1', '@2 say One down. No water. Next.'], 2: ['@1 say Down you go. The ladder does not lend a hand up - climb it again when you can stand.']}
