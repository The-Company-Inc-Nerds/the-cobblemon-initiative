# The Old Nets (Mystic SQ N17, Corvin Thistlefen) — 5 living-water cod, counted aloud.
# Run AS the player from the claim dialog button (claim entry gated corvin_fish_done +
# not corvin_fish_paid). Single-fire: guarded once with unless corvin_fish_paid, tag
# latches before any grant (nifl/lanterns_reward pattern). Pays 350 via the skewed
# payout; keepsake 4 great balls — the marsh still stocks its own shelves.
execute if entity @s[tag=corvin_fish_paid] run return 0
tag @s add corvin_fish_paid
function cobblemon_initiative:economy/payout {amount:350}
give @s cobblemon:great_ball 4
tellraw @s [{"text":"The Old Nets — ","color":"gold"},{"text":"five live-water cod, counted out loud within earshot of the field ledger. Proof on record: the waters the Company did not drain still give.","color":"gray"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8
