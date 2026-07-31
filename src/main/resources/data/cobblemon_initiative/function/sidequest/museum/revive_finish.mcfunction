# Resurrection machine cinematic — PHASE 3 (finish, +60t). Scheduled from revive_begin. Removes the
# floating fossil, plays the birth beat, and gives each reviving player their revived mon (branch on
# the ci_reviving_<species> carry tag), then clears the carry tags. Runs at world level.
kill @e[tag=ci_fossil_float]
playsound cobblemon:fossilmachine.finished master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound cobblemon:fossilmachine.retrieve_pokemon master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound minecraft:entity.player.levelup master @a[distance=..24] 1902.5 116 2313.8 0.7 0.8
execute positioned 1902.5 116 2313.8 run particle minecraft:end_rod ~ ~ ~ 0.5 0.7 0.5 0.06 60
execute positioned 1902.5 116 2313.8 run particle minecraft:glow ~ ~ ~ 0.4 0.6 0.4 0.0 24
# Kabuto (dome fossil)
execute as @a[tag=ci_reviving_kabuto] run cobblemon-initiative givemon kabuto level=10
execute as @a[tag=ci_reviving_kabuto] run tellraw @s [{"text":"Ten thousand years of sediment let go. Kabuto lives again — and it is yours.","color":"gold"}]
# Anorith (claw fossil)
execute as @a[tag=ci_reviving_anorith] run cobblemon-initiative givemon anorith level=10
execute as @a[tag=ci_reviving_anorith] run tellraw @s [{"text":"The claw uncurls after an age asleep. Anorith swims once more — and it is yours.","color":"gold"}]
# Clear the carry tags so the platform is idle again.
tag @a remove ci_reviving_kabuto
tag @a remove ci_reviving_anorith
