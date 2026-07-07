# GRAIN IN GOODS OUT (The Miller Walk) — market survey sensor. Register in #minecraft:tick.
# Cheap: both lines gate on hz_survey_active, so it is a no-op the rest of the run.
# The miller hires the player to hear the open pitch (either wheat-trader body, tagged
# hz_wheat_trader at placement) and to see the company store (the granary body, tagged
# hz_granary at placement). Standing within 5 blocks of either logs that half of the survey.
# The authored wheat_trader / granary_keeper dialog stays byte-identical — this only reads
# proximity. Single-player: @a == the player. Each half routes through a logger so the
# silent tag-add gets a receipt (MARKET SURVEY — logged (n/2), page_turn; the proven
# price-check Noted (n/3) pattern); quest/render flips the sidebar line at 2/2.
execute as @a[tag=hz_survey_active,tag=!hz_saw_pitch] at @s if entity @e[tag=hz_wheat_trader,distance=..5] run function cobblemon_initiative:sidequest/grain_survey/log_pitch
execute as @a[tag=hz_survey_active,tag=!hz_saw_granary] at @s if entity @e[tag=hz_granary,distance=..5] run function cobblemon_initiative:sidequest/grain_survey/log_granary
