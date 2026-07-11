# FOUR GARDENS finale check — run as @s = the winning player by every gym/hz_wall_N.
# The four walls can fall in ANY order, so every wall runs this recount instead of
# wall 4 owning the beat. Fires the all-gardens-open flourish exactly once, guarded
# by the hz_walls_done player tag. Reads the same defeated_hua_zhan_trainer_1..4
# battle-onwin tags the Aya reveal entry gates on (all_tags) — no new state, no
# scoreboard, and the Aya -> Blossom transform wiring is untouched.
execute if entity @s[tag=defeated_hua_zhan_trainer_1,tag=defeated_hua_zhan_trainer_2,tag=defeated_hua_zhan_trainer_3,tag=defeated_hua_zhan_trainer_4,tag=!hz_walls_done] run tag @s add hz_walls_all
execute if entity @s[tag=hz_walls_all] run title @s title {"text":"The Gardens Stand Open","color":"green","bold":true}
execute if entity @s[tag=hz_walls_all] run title @s subtitle {"text":"Four wardens woken — every vine wall has come down.","color":"gray","italic":true}
execute if entity @s[tag=hz_walls_all] run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 0.7 1
execute if entity @s[tag=hz_walls_all] run playsound minecraft:block.beacon.power_select master @s ~ ~ ~ 1 0.8
execute if entity @s[tag=hz_walls_all] run tellraw @s [{"text":"Every vine wall in the Four Gardens has fallen. ","color":"gray"},{"text":"The groundskeeper by the west stair has been waiting for this.","color":"green"}]
execute if entity @s[tag=hz_walls_all] run tag @s add hz_walls_done
tag @s remove hz_walls_all
