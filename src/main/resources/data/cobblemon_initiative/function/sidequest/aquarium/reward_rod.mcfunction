# Gaviota aquarium — Tier 3 / completion reward: the enchanted Master Rod (Lure + Luck of the Sea).
# Run AS the player from GaviotaManager.donate once every donatable species is in the tanks. A vanilla
# fishing_rod is used because Cobblemon poke_rods do not take the vanilla fishing enchants the note asks
# for. 1.21.1 component syntax — tweak enchant levels/name here if desired.
give @s minecraft:fishing_rod[enchantments={levels:{"minecraft:lure":3,"minecraft:luck_of_the_sea":3,"minecraft:unbreaking":3,"minecraft:mending":1}},custom_name='{"text":"Master Rod","italic":false,"color":"aqua"}'] 1
title @s title [{"text":"The Master Rod","color":"aqua"}]
title @s subtitle [{"text":"The aquarium is complete. The tide owes you nothing now.","color":"gray"}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
