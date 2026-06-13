# Macro: render the current gym objective. Arg: town.
$bossbar set cobblemon_initiative:objective name [{"text":"⚔ Defeat the $(town) Gym","color":"gold"}]
$scoreboard players display name #main ci_quest [{"text":"▶ Defeat the $(town) Gym","color":"yellow"}]
