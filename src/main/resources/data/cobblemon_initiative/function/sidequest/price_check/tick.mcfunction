# Price Check tick — mirror the world instability index (#idx cd_instability, set by
# economy/gym_destabilize, liberation, hq_stabilize) onto the player score so the
# compiler-generated dialog band tags (cd_instability_gte_8/16/20 for this quest, and the
# pre-existing cd_instability_gte_40) actually fire: dialog/band_tags reads @s cd_instability,
# but the runtime keeps the index on the #idx fake player. Single-player: @a == the player.
# Guarded so an unset #idx (pre economy/load) never errors the tick.
execute if score #idx cd_instability matches -2147483648..2147483647 run scoreboard players operation @a cd_instability = #idx cd_instability
