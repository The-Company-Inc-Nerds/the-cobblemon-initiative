# cinderfall_rematch — makes the four Cinderfall Descent sight trainers RE-BATTLEABLE
# once the 10th badge is earned, turning the Descent into a repeatable fire gauntlet
# (playtest design: gym 9 -> gym 10 CINDERFALL trainers retrigger after badge 10).
#
# Hand-authored; registered in #minecraft:tick. NEW quest content added by hand — if a
# fifth Cinderfall trainer is ever authored, add its defeated_ tag line here too.
#
# MECHANISM: a Cinderfall trainer stops all three of its engagement paths the moment the
# player carries defeated_cinderfall_<id>:
#   1. NpcSightManager.standDown() — the PURSUE stop_tag (defeated_cinderfall_<id>),
#   2. the compiled ON_DISTANCE_VERY_CLOSE forced battle — gated not_tag defeated
#      (= PLAYER_TAG EQUALS no_defeated_cinderfall_<id>, an inverse band tag that
#      dialog/band_tags maintains from the presence of defeated_cinderfall_<id>),
#   3. the dialog battle button — gated not_tag defeated.
# Removing the player's defeated_cinderfall_<id> tag re-arms ALL THREE at once (band_tags
# re-sets the no_defeated_ inverse next tick), so the trainer becomes fightable again.
#
# We remove those four defeat tags every tick for any player who has badges_gte_10
# (the auto band tag maintained by dialog/band_tags from memory_fragment >= 10, i.e. the
# 10th badge is earned) AND is NOT currently inside a forced trainer battle
# (in_trainer_battle — set by the engage:touch VERY_CLOSE trigger at battle start,
# cleared in both onwin branches). The in_trainer_battle guard means the just-won onwin
# re-adds defeated_cinderfall_<id> AFTER this tick's clear during the fight, and the fight
# is never interrupted; the next full leave+re-enter of the 4-block band (or a fresh
# dialog open) then finds the tag cleared again and re-forces — an endless gauntlet.
#
# Before badge 10 this function is a no-op (badges_gte_10 absent), so the four trainers
# behave as ordinary one-and-done sight spotters on the first run down the Descent.
execute as @a[tag=badges_gte_10,tag=!in_trainer_battle] run tag @s remove defeated_cinderfall_ignar
execute as @a[tag=badges_gte_10,tag=!in_trainer_battle] run tag @s remove defeated_cinderfall_vexa
execute as @a[tag=badges_gte_10,tag=!in_trainer_battle] run tag @s remove defeated_cinderfall_drakar
execute as @a[tag=badges_gte_10,tag=!in_trainer_battle] run tag @s remove defeated_cinderfall_karn
