# Sango Classic — entry point, run AS THE PLAYER from Deka dialog (ExecAsUser).
# Guards against double entry while a quarter is live, then hands off to begin.
execute if score #on ci_classic matches 1 run tellraw @s [{"text":"The Classic is already running — get back to the pond.","color":"yellow"}]
execute unless score #on ci_classic matches 1 run function cobblemon_initiative:sidequest/derby/begin
