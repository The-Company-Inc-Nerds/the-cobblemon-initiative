# Macro (run as @s=player): the "CURRENCY STABILIZED" beat after the HQ raid. Arg: idx.
title @s times 10 70 20
title @s subtitle {"text":"The Company's grip on the ledger loosens — for now.","color":"gray"}
title @s title {"text":"CURRENCY STABILIZED","color":"#55FF55","bold":true}
$tellraw @s [{"text":"[Economy] ","color":"gold","bold":true},{"text":"CobbleDollar stability restored to $(idx)/100.","color":"gray"}]
playsound minecraft:block.beacon.activate master @s ~ ~ ~ 1.0 1.2
