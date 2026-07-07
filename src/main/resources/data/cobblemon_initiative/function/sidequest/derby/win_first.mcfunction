# Sango Classic — first win: 500 CD (skew-aware), a Poke Rod, 2 poke balls, 5 oran berries, champion latch.
# heal_ball deliberately lives at the Invitational podium, not here (marquee-item audit).
function cobblemon_initiative:economy/payout {amount:500}
loot give @s loot cobblemon_initiative:npc_gift/training_major
give @s cobblemon:poke_rod 1
give @s cobblemon:poke_ball 2
give @s cobblemon:oran_berry 5
tag @s add sango_classic_champion
tellraw @s [{"text":"First place in the Sango Classic. Purse: 500 CD, a Poke Rod, two Poke Balls, five oran berries.","color":"aqua"}]
