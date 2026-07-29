# PokéPhone VIDEO-CALL delivery (macro). Called AS @s (the player) from phone/ring_<id> after
# the ring flavor (actionbar + chime). Spawns the caller AT the player — always loaded, so a call
# still never dies on an unloaded chunk (the one virtue of the old text system, kept) — then opens
# their Easy NPC call dialog at $(label): the caller's face renders in the dialog GUI, their lines
# read as the call, and a "Hang up" close button ends it. The caller's ON_CLOSE_DIALOG trigger
# (easy_npc delete @s) despawns it on hang-up; the pre-spawn delete below sweeps any straggler.
# $(caller)=preset filename, $(tag)=body tag, $(label)=dialog entry label.
# PRIMARY BUG (why the phone never opened, any build): the delete line below shipped with a
# leading `$` but no `$(...)` variable, so Minecraft rejected the whole file — "Macro without
# variables" — and deliver never LOADED, so nothing downstream ran. `$` only belongs on lines
# that use a macro variable; the delete line has none, so it is a plain command now.
# OPEN IS ALSO A BOUNDED RETRY (defends a secondary client-side race, bytecode+log-verified vs
# Easy NPC 6.25.0): the server open path is synchronous and correct (import_new loads DialogData
# BEFORE the body is added to the world), so NO server-side defer — 5t or 20t — ever helped. The break is
# CLIENT-side: the freshly-spawned body isn't in the client's LivingEntityManager tracking map
# yet when the open-menu packet lands, so DialogScreen.<init> calls getEasyNPC().getLivingEntity()
# on a null entity and NPEs on the render thread → the screen never builds, silently. (Matching
# NPE is in logs/latest.log for a persisted NPC the instant the player tp'd out of its tracking
# range.) A single fixed delay keeps losing that race; a small self-retry wins it once the client
# tracks the body. phone/open re-issues the open each tick and stops on a spent try budget.
# Single-player only (CLAUDE.md), so the open targets @a[limit=1] = the player.
execute at @s run easy_npc delete @e[type=easy_npc:humanoid,tag=ci_phone_caller,distance=..64]
# a7: the caller is INVISIBLE again (showrunner call — the visible-in-front a6 read badly), so spawn
# it 2 blocks BEHIND the player at foot level (local ^ ^-1.6 ^-2 from eye height) — off-camera, never
# clipping. The dialog carries the call; the body is just the host. phone/tick sweeps any orphan.
$execute at @s anchored eyes positioned ^ ^-1.6 ^-2 run easy_npc preset import_new data easy_npc:preset/humanoid/$(caller).npc.snbt ~ ~ ~
$data modify storage cobblemon_initiative:phone open set value {tag:"$(tag)",label:"$(label)"}
scoreboard players set #phone_open_tries ci_dawn 4
schedule function cobblemon_initiative:phone/open_tick 3t
