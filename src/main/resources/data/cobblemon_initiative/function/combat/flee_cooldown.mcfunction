# flee_cooldown — 15 s re-engage grace after fleeing a trainer battle (alpha.26).
# Hand-authored; registered in #minecraft:tick. NuzlockeInit.handleBattleFled sets
# ci_flee_cd=300 when the player FLEES a TRAINER battle; this tick decrements it and
# maintains the INVERSE tag no_recent_flee, exactly the band_tags pattern: Easy NPC
# 6.25's PLAYER_TAG condition IGNORES the Operation field (bytecode: contains() only),
# so "no recent flee" must be an EQUALS gate on an inverse tag, never NOT_EQUALS.
# Compiled engage:touch presets gate their forced ON_DISTANCE_VERY_CLOSE battle (and
# the CLOSE hail) on no_recent_flee — a fled trainer cannot re-force the fight while
# the player is still inside the 4-block band. NpcSightManager reads the same score to
# hold PURSUE movement. This tag is maintained HERE, not by the auto-generated
# band_tags (no not_tag gate ever names recent_flee, so band_tags never touches it).
#
# Zero-init unset scores first so the scores= selector below always matches.
execute as @a unless score @s ci_flee_cd = @s ci_flee_cd run scoreboard players set @s ci_flee_cd 0
execute as @a if score @s ci_flee_cd matches 1.. run scoreboard players remove @s ci_flee_cd 1
tag @a[scores={ci_flee_cd=0}] add no_recent_flee
execute as @a if score @s ci_flee_cd matches 1.. run tag @s remove no_recent_flee
