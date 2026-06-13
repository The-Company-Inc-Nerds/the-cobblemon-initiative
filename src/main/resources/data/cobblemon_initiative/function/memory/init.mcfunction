# Memory Fragment scoreboard init.
# Append a call to this from data/cobblemon_initiative/install.json's run, or call once via
#   /function cobblemon_initiative:memory/init
# Dummy objective tracks highest fragment reached (display/debug + datapack gating).
scoreboard objectives add memory_fragment dummy {"text":"Memories Recovered"}
