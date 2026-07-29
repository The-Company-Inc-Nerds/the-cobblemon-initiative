# Orc encampments — load-time state init (registered in #minecraft:load). a22 REDESIGN (playtest):
# was 8 FIXED, permanent, one-shot camps (vindicator/husk). Now ONE rotating camp — a new random
# pin rolls each dawn AFTER the previous is cleared — built from easy_npc:orc / easy_npc:orc_warrior
# bodies (attack-on-sight AI baked into the orc presets). State on ci_ambient (#-holders hidden from
# the sidebar):
#   #orc_active   = the live pin id (2..9), or 0 = none/cleared (waiting for the next dawn to roll)
#   #orc_raised   = 0 armed (pin chosen, mobs not yet spawned) / 1 mobs live
#   #orc_last_pin = the pin cleared last (rerolled against so the camp moves)
#   #orc_last_day = day-change latch (economy/dawn idiom)
# Init-if-unset so a live/mid-fight camp survives relog. #orc_last_day starts -1 so the very first
# tick of a fresh world rolls the opening camp immediately (no dawn wait). The old per-pin
# #orc_camp_p2..p9 latches are abandoned (harmless leftover scores); repairs_a22 sweeps any stale
# vanilla ci_orc bodies from the retired fixed camps.
scoreboard objectives add ci_ambient dummy
execute unless score #orc_active ci_ambient matches -2147483648.. run scoreboard players set #orc_active ci_ambient 0
execute unless score #orc_raised ci_ambient matches -2147483648.. run scoreboard players set #orc_raised ci_ambient 0
execute unless score #orc_last_pin ci_ambient matches -2147483648.. run scoreboard players set #orc_last_pin ci_ambient 0
execute unless score #orc_last_day ci_ambient matches -2147483648.. run scoreboard players set #orc_last_day ci_ambient -1
