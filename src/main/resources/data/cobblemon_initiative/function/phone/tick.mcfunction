# PokéPhone driver (registered in #minecraft:tick). Delivers remote story beats as PokéGear-style
# "calls" (Pokémon Gold/Silver): an actionbar "☎ ringing", a chime, then the caller's lines. Text
# delivery is deliberate over a live Easy NPC host — a call then NEVER fails on an unloaded chunk,
# it rings anywhere. Gated on the ci_flavor #phone toggle (published by MinecraftFlavorConfig).
# Each call fires ONCE via a call_<id>_done tag. Throttled to ~2s so the affordability probe below
# is not a per-tick cost. (#phone_tick rides ci_dawn, declared in economy/load.)
execute unless score #phone ci_flavor matches 1.. run return 0
scoreboard players add #phone_tick ci_dawn 1
execute unless score #phone_tick ci_dawn matches 40.. run return 0
scoreboard players set #phone_tick ci_dawn 0
# Mom — "I want to watch your Pokémon" (after the 3rd badge, Mystic Marsh).
execute as @a[tag=defeated_mystic_leader] unless entity @s[tag=call_mom_watch_done] run function cobblemon_initiative:phone/ring_mom
# Mayor Suzune — "a beacon came in" (freed the first field, and you can now afford the next beacon).
execute as @a[tag=farm_1_free] unless entity @s[tag=call_beacon_stock_done] run function cobblemon_initiative:phone/beacon_check
