# One-shot chat beat on first approach to the branch office staff. Run as the player.
# ALPHA.25: second line added so the stealth contract is stated up front — staff
# sightlines are the hazard, the top-floor reading is the prize, unseen is the style.
# ALPHA.26 (N8): third line states the fiction outright — the Branch Manager is
# ambient_static now and NEVER looks up (the alpha.25 stationary-look stare read as
# him catching you while the meter said EAVESDROPPING); his r14 staff sensors do the
# looking for him.
# ALPHA.2 (playtest): fourth line — Rong joined the sensor pool ON the loft itself
# (static watcher, fixed cone over the lectern + stair landing); the reveal names her
# so the player knows the top floor is no longer a free listening post.
tag @s add hz_office_warned
tellraw @s [{"text":"A Company branch office. ","color":"yellow"},{"text":"Floor privileges are assigned, not assumed.","color":"gray","italic":true}]
tellraw @s [{"text":"Staff on every floor log what they see. ","color":"gray"},{"text":"Something is being read aloud on the top floor — reach the door and listen, and stay out of their sightlines if you want your visit off the books.","color":"dark_gray","italic":true}]
tellraw @s [{"text":"The Branch Manager never looks up from his lectern. ","color":"gray","italic":true},{"text":"His staff do the looking for him.","color":"dark_gray","italic":true}]
tellraw @s [{"text":"An overseer stands watch on the loft floor itself, and she does not blink. ","color":"gray","italic":true},{"text":"Her back is to the south wall. Backs are honest that way.","color":"dark_gray","italic":true}]
