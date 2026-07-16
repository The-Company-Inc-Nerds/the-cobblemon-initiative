# The Iron Ladder - Round 2 (Deepcore SQ2). Run as the player (dialog button, gated
# defeated_sq_ladder_1 + not_tag defeated_sq_ladder_2). NO heal between rounds - carried
# damage stands. Tag-only onwin; the purse is paid once at ladder_claim. Single-quoted SNBT.
execute if entity @s[tag=defeated_sq_ladder_1] unless entity @s[tag=defeated_sq_ladder_2] run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_2 onwin {1: ['tag @1 add defeated_sq_ladder_2', '@2 say Two. The rock does not soften. One more waits.'], 2: ['@1 say The stone had you. No shame - the shaft has broken harder than you. Come back and swing again.']}
