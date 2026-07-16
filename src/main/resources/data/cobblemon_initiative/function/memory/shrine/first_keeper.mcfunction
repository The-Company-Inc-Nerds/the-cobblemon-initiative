# The Land Remembers (14_shrines Q3) — one-time elemental-recognition sting on the FIRST
# shrine leader defeated. Called from every shrine leader on_win as:
#   execute as @1 unless entity @1[tag=shrine_frag_seen] run function ...:memory/shrine/first_keeper
# Runs in the winning player's context (@s). The shrine_frag_seen latch makes only the
# first clear show it; the other four keepers fire this line into a no-op. Circles the
# amnesia mystery from the ELEMENTAL angle without touching cd_instability or naming the
# Founder. Macro-safe: no apostrophes, no percent, no double-quotes in the delivered text.
tag @s add shrine_frag_seen

title @s times 10 80 20
title @s subtitle [{"text":"Four more keepers will know your weight before this is done.","color":"gray","italic":true}]
title @s title [{"text":"The keeper called it old gravity.","color":"#7A5CA8","bold":true}]

# Low chime + the soul-surface tone, matching the memory-fragment idiom.
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 0.7 0.5
playsound minecraft:particle.soul_escape master @s ~ ~ ~ 0.4 0.8

# Chat echo so the first-person flash is re-readable during the stream.
tellraw @s [{"text":"[Memory] ","color":"#7A5CA8","bold":true},{"text":"Something in you is older than the badges you have earned. The keepers feel it before you do.","color":"gray","italic":true}]
