# Warden Ossa's turn-in for the WARDEN treasure hunt (repeatable, playtest 2026-08-06 final design).
# Run AS the player (Ossa's "File the cache" button, ExecAsUser via the allowlisted `function` root).
# The player brushed + dug the buried chest at the warden dig site and opened it, receiving ONE
# marked "Warden's Cache" chest item (custom_data ci_warden_cache:1b). Consume-probe that item, pay
# a MODEST fixed reward, and reset the hunt (#hunt_warden -> 0) so Ossa can arm it again. REPEATABLE:
# no training pack, no big purse, no randomness on the committed reward (hardcore Nuzlocke rule).
# The FIRST-ever turn-in also carries the TWO-SEAL founder beat (one-shot, latch warden_two_seals).
execute store result score #warden_t ci_item run clear @s minecraft:chest[minecraft:custom_data~{ci_warden_cache:1b}] 0
execute if score #warden_t ci_item matches ..0 run title @s actionbar [{"text":"You have brought me no cache. The dust still stands where it was buried.","color":"red"}]
execute if score #warden_t ci_item matches ..0 run return 0
clear @s minecraft:chest[minecraft:custom_data~{ci_warden_cache:1b}] 1
scoreboard players set #hunt_warden ci_hunt 0
tag @s remove warden_hunt_active
function cobblemon_initiative:economy/payout {amount:300}
tellraw @s [{"text":"Ossa breaks the seal, reads, and files it into the ledger. ","color":"gold"},{"text":"\"Honest coin for honest sand,\" she says, sliding the drawer shut. \"The Reach remembers who does the digging.\"","color":"gray"}]
playsound minecraft:block.chest.locked master @s ~ ~ ~ 0.7 0.9
# ── FIRST turn-in only: the two-seal founder beat (moved here from the old one-shot cache) ──
execute if entity @s[tag=!warden_two_seals] run title @s title [{"text":"TWO SEALS","color":"gold"}]
execute if entity @s[tag=!warden_two_seals] run title @s subtitle [{"text":"the Company mark - and an older hand beneath it","color":"gray"}]
execute if entity @s[tag=!warden_two_seals] run tellraw @s [{"text":"Ossa turns the cache-slip to the lamp. ","color":"gold"},{"text":"Under the Company stamp there is an older, personal seal — a hand she has seen on exactly one charter in her life. She looks from the slip to your face, then away, and files it face down. \"Some finds you file,\" she says, quietly. \"Some you file face down.\"","color":"gray"}]
execute if entity @s[tag=!warden_two_seals] run playsound minecraft:block.amethyst_block.resonate master @s ~ ~ ~ 0.8 0.6
tag @s add warden_two_seals
