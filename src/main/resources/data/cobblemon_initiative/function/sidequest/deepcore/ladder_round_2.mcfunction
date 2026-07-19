# The Iron Ladder - Round 2 (Deepcore SQ2). Run as the player (dialog button, gated
# defeated_sq_ladder_1 + not_tag defeated_sq_ladder_2). NO heal between rounds - carried
# damage stands. Tag-only onwin; the purse is paid once at ladder_claim. Single-quoted SNBT.
# TBCS refuses an unattached trainer ("X is not attached to an entity" — the Stadium
# finding; live-caught for this dispatch 2026-07-18). Anchor idiom: invisible stand at
# the player, attach, battle; both onwin branches sweep the anchor.
execute if entity @s[tag=defeated_sq_ladder_1] unless entity @s[tag=defeated_sq_ladder_2] run kill @e[tag=ci_anchor_sq_ladder_2]
execute if entity @s[tag=defeated_sq_ladder_1] unless entity @s[tag=defeated_sq_ladder_2] at @s run summon minecraft:armor_stand ~1.5 ~ ~ {Invisible:1b,NoGravity:1b,Tags:["ci_anchor_sq_ladder_2"]}
execute if entity @s[tag=defeated_sq_ladder_1] unless entity @s[tag=defeated_sq_ladder_2] run tbcs attach rctmod:sq_ladder_2 @e[tag=ci_anchor_sq_ladder_2,limit=1]
execute if entity @s[tag=defeated_sq_ladder_1] unless entity @s[tag=defeated_sq_ladder_2] run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_2 onwin {1: ['kill @e[tag=ci_anchor_sq_ladder_2]', 'tag @1 add defeated_sq_ladder_2', '@2 say Two. The rock does not soften. One more waits.'], 2: ['kill @e[tag=ci_anchor_sq_ladder_2]', '@1 say The stone had you. No shame - the shaft has broken harder than you. Come back and swing again.']}
