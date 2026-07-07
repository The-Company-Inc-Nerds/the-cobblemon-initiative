# Miller Walk — the open-pitch half of the survey logs. Run as/at the player by the
# grain_survey tick sensor. Latch FIRST, then print the count the latch just made true.
tag @s add hz_saw_pitch
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1
execute if entity @s[tag=hz_saw_granary] run title @s actionbar [{"text":"MARKET SURVEY","color":"gold","bold":true},{"text":" — logged (2/2). Report to Guo the Miller.","color":"gray"}]
execute unless entity @s[tag=hz_saw_granary] run title @s actionbar [{"text":"MARKET SURVEY","color":"gold","bold":true},{"text":" — logged (1/2). The company store remains.","color":"gray"}]
