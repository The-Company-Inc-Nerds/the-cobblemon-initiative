# Orc camps — shared cleared ceremony. Run positioned at the camp pin from a
# camp_cleared_pN wrapper (which owns the latch write and the tiered spoils line).
# Audience is @a[distance=..64] — wide enough to catch a player who backpedaled off the
# pin mid-fight. Corporate flavor per the wave brief: The Company keeps its muscle on
# the books; freelance raiders in the hills are an embarrassment, not a subsidiary.
title @a[distance=..64] title [{"text":"CAMP BROKEN","color":"gold","bold":true}]
title @a[distance=..64] subtitle [{"text":"The raider banner comes down","color":"yellow"}]
playsound minecraft:event.raid.horn master @a[distance=..64] ~ ~ ~ 1 0.8
tellraw @a[distance=..64] [{"text":"Freelance muscle — no Company sigil, no ledger line, no severance. Whoever armed this camp did it off the books, and the hills breathe easier without them.","color":"gray","italic":true}]
tellraw @a[distance=..64] [{"text":"The camp strongbox gives up its plunder.","color":"yellow"}]
