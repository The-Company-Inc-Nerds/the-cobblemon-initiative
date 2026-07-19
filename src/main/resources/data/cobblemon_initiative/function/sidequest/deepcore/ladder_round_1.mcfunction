# The Iron Ladder - Round 1 (Deepcore SQ2). Run as the player (dialog button, gated
# iron_ladder_active + not_tag defeated_sq_ladder_1). NO heal is ever run between rounds -
# the party carries damage forward, the whole point of the gauntlet. Tag-only onwin (no cash;
# the purse is paid once at ladder_claim). onwin lists are single-quoted SNBT.
# TBCS refuses an unattached trainer ("X is not attached to an entity" — the Stadium
# finding; live-caught for this dispatch 2026-07-18). Anchor idiom: invisible stand at
# the player, attach, battle; both onwin branches sweep the anchor.
execute unless entity @s[tag=defeated_sq_ladder_1] run kill @e[tag=ci_anchor_sq_ladder_1]
execute unless entity @s[tag=defeated_sq_ladder_1] at @s run summon minecraft:armor_stand ~1.5 ~ ~ {Invisible:1b,NoGravity:1b,Tags:["ci_anchor_sq_ladder_1"]}
execute unless entity @s[tag=defeated_sq_ladder_1] run tbcs attach rctmod:sq_ladder_1 @e[tag=ci_anchor_sq_ladder_1,limit=1]
execute unless entity @s[tag=defeated_sq_ladder_1] run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_ladder_1 onwin {1: ['kill @e[tag=ci_anchor_sq_ladder_1]', 'tag @1 add defeated_sq_ladder_1', '@2 say One down. No water. Next.'], 2: ['kill @e[tag=ci_anchor_sq_ladder_1]', '@1 say Down you go. The ladder does not lend a hand up - climb it again when you can stand.']}
