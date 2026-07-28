# Gaviota Open (alpha.26 audit rulings) — entry point, run AS THE PLAYER from the
# Weighmaster Enzo dialog (ExecAsUser). Guards against double entry while a round is
# live, then hands off to begin. Sango derby/start clone.
execute if score #on ci_open matches 1 run tellraw @s [{"text":"The Open is already running — get back to the water.","color":"yellow"}]
execute unless score #on ci_open matches 1 run function cobblemon_initiative:sidequest/gaviota_open/begin
