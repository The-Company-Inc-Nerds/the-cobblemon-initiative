# Macro (run as @s=player): narrate the current instability index. Arg: idx.
$title @s actionbar [{"text":"CobbleDollar stability ","color":"gold"},{"text":"$(idx)","color":"red"},{"text":"/100","color":"dark_gray"},{"text":"  —  the Company's ledgers waver.","color":"gray"}]
playsound minecraft:block.sculk_sensor.clicking master @s ~ ~ ~ 0.4 0.7
