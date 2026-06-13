# Wheat-trader tier gating + post-trade ambush poller. Runs every tick (cheap: pure score checks).
#
# TIER GATING off the SHARED fields_liberated counter (owned by Field Liberation designer):
#   0-1 liberated  -> trade only          (no tag)
#   2-3 liberated  -> recognition         (wheat_trader_suspicious tag)
#   4+  liberated  -> ambush              (wheat_trader_hostile tag)
# Tags are additive one-way for the run; we set the highest reached and never clear them,
# so this is relog-safe (tags persist in world data; re-running just re-asserts the same tag).

# Recognition tier: 2+ fields liberated -> tag every player suspicious (idempotent).
execute as @a if score @s fields_liberated matches 2.. run tag @s add wheat_trader_suspicious
# Ambush tier: 4+ fields liberated -> upgrade to hostile (suspicious stays; hostile dialog has higher Priority).
execute as @a if score @s fields_liberated matches 4.. run tag @s add wheat_trader_hostile

# Note: fields_liberated is single-player so @a == the one player; using @a keeps it relog-safe
# (the score lives on the player, the tag re-applies on the next tick after rejoin).
