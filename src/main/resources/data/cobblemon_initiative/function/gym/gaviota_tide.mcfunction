# Gaviota Port — Tide Clock. NEEDS tick registration (tags/function/tick.json) and the
# gaviota_tide objective added in gym/load.mcfunction — both are SHARED files, reported
# via shared_file_requests; do not edit them from a content task.
#
# World tide cycle on a fake-player score: #tide_clock gaviota_tide counts 0..4799 and
# wraps. Phase 0..2399 = HIGH tide, 2400..4799 = LOW tide (2400 ticks each — a 4 minute
# full cycle). During high tide the tide_high PLAYER_TAG routes pier trainer dialogs to
# their _hightide rain-team entries; low tide falls back to the base teams.
# Pier anchor = Leader Neptune stand at 596 87 3646 (pier trainers sit 593-599 / 3644-3648).
# TODO(showrunner): confirm the pier anchor + the 64-block bell radius covers the wharf.

# 1) CLOCK — advance and wrap. scoreboard add initializes an unset score to 0 before
#    adding, so the first tick reads 1; flip announces only fire on real boundaries.
scoreboard players add #tide_clock gaviota_tide 1
execute if score #tide_clock gaviota_tide matches 4800.. run scoreboard players set #tide_clock gaviota_tide 0

# 2) FLIP ANNOUNCE — loud on both boundaries: dockside bell + actionbar to @a within 64
#    of the pier. HIGH flip = wrap to 0; LOW flip = 2400.
execute if score #tide_clock gaviota_tide matches 0 positioned 596 87 3646 as @a[distance=..64] at @s run playsound minecraft:block.bell.use player @s ~ ~ ~ 1 1.2
execute if score #tide_clock gaviota_tide matches 0 positioned 596 87 3646 as @a[distance=..64] run title @s actionbar [{"text":"HIGH TIDE. ","color":"aqua","bold":true},{"text":"The harbor bell rings — the pier crews fight with the sea at their backs.","color":"gray"}]
execute if score #tide_clock gaviota_tide matches 2400 positioned 596 87 3646 as @a[distance=..64] at @s run playsound minecraft:block.bell.use player @s ~ ~ ~ 1 0.7
execute if score #tide_clock gaviota_tide matches 2400 positioned 596 87 3646 as @a[distance=..64] run title @s actionbar [{"text":"LOW TIDE. ","color":"gold","bold":true},{"text":"The harbor bell rings — the water pulls back and the crews fall back on old habits.","color":"gray"}]

# 3) tide_high PLAYER_TAG — idempotent add/remove every tick (band_tags style, relog
#    safe). High phase + within 64 of the pier = tagged; leaving the pier or the tide
#    going out clears it.
execute if score #tide_clock gaviota_tide matches 0..2399 positioned 596 87 3646 run tag @a[distance=..64] add tide_high
execute if score #tide_clock gaviota_tide matches 0..2399 positioned 596 87 3646 run tag @a[distance=64..] remove tide_high
execute if score #tide_clock gaviota_tide matches 2400.. run tag @a remove tide_high
