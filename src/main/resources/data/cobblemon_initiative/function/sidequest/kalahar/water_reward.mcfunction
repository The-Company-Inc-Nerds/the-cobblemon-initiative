# Dry Season (Q2) turn-in. Run AS the player (Marisol's report path once the pump is shut).
# Redundant safety re-check: only fire on oasis_pump_off and not already claimed. economy/payout
# skews the 750 face by the instability haircut; the gives are the goodwill bundle. NOTE: the
# spec's cobblemon:fresh_water id does NOT exist in 1.7.3 (jar-checked) - substituted the valid
# cobblemon:mystic_water x2 as the water keepsake; super_potion x2 stays. Latches oasis_restored
# (Marisol's tombstone + the sidebar close) + dry_season_done. Company-voice receipt subtitle.
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run function cobblemon_initiative:economy/payout {amount:750}
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run give @s cobblemon:mystic_water 2
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run give @s cobblemon:super_potion 2
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run title @s subtitle [{"text":"ADJUSTMENT: rounding, in the Company favor","color":"gray"}]
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run title @s title [{"text":"THE OASIS HOLDS","color":"aqua"}]
execute if entity @s[tag=oasis_pump_off] run tag @s add oasis_restored
execute if entity @s[tag=oasis_pump_off] run tag @s add dry_season_done
