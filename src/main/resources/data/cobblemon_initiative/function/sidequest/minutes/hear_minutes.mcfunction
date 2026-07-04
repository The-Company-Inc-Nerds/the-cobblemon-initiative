# 8 unseen seconds held at the Branch Manager door. Run as the player. One-time via
# the hz_minutes_heard tag (gates every caller in tick/near_office).
#
# VERIFIER FIX 2 (cover-story regression): the closing item is containment-only
# language plus the EXACT memo_44c boilerplate — Reminder — there was never a founder.
# — one floor of rank higher than the checkpoint. No retirement language anywhere:
# a later, higher-rank document walking the erasure backwards would break the staging.
scoreboard players reset @s ci_loiter_hz
tag @s add hz_minutes_heard
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8

# The quarterly review, read aloud on the other side of the door (staged tellraw).
tellraw @s [{"text":"Through the door, a voice reads slowly — the way people read to an empty room.","color":"gray","italic":true}]
tellraw @s [{"text":"Item one. ","color":"gold"},{"text":"Farm parcels at or above quota across the eastern corridor. The fields perform. Keep them performing.","color":"gray"}]
tellraw @s [{"text":"Item two. ","color":"gold"},{"text":"Greenhouse dispatch runs ahead of plan. Advance the calendar, not the questions.","color":"gray"}]
tellraw @s [{"text":"Item three. ","color":"gold"},{"text":"Confidence recalibration proceeds in the eastern district. Belief is a number. Adjust it gently.","color":"gray"}]
tellraw @s [{"text":"Closing item, over the signature of Regional Manager Shade. ","color":"gold"},{"text":"The incident on the Blossom corridor is contained. No further distribution. Reminder — there was never a founder.","color":"gray"}]
tellraw @s [{"text":"A page turns. A circulation copy of the minutes sits in the out tray beside the door.","color":"gray","italic":true}]

# The prop: renamed paper + lore, the memo_44c ship shape.
loot give @s loot cobblemon_initiative:npc_gift/quarterly_minutes
tellraw @s [{"text":"Quarterly Minutes secured. ","color":"gold"},{"text":"Lucian the archivist in Sango will want these.","color":"gray"}]

# Off the Org Chart — only if the whole visit stayed off the sight ledger
# (hz_office_seen latches in tick on any office-staff can_see_player hit).
execute unless entity @s[tag=hz_office_seen] run tellraw @s [{"text":"OFF THE ORG CHART","color":"gold","bold":true},{"text":" — you heard the district read itself out, and nobody logged your visit.","color":"gray"}]
execute unless entity @s[tag=hz_office_seen] run playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 0.8 1
