# 8 unseen seconds held at the gym gate. Run as the player.
tag @s add audit_loiter
scoreboard players reset @s ci_audit
# Direct-open on arming (see memo/loiter_ready — same Easy NPC dedup trap).
# audit_listener is applied to the Yield Analyst body at placement (runbook protocol).
execute at @s run easy_npc dialog open @e[tag=audit_listener,limit=1,sort=nearest] @s overheard_valuation
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8
