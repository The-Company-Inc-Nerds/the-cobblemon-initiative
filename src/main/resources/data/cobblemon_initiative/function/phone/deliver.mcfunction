# PokéPhone VIDEO-CALL delivery (macro). Called AS @s (the player) from phone/ring_<id> after
# the ring flavor (actionbar + chime). Spawns the caller AT the player — always loaded, so a call
# still never dies on an unloaded chunk (the one virtue of the old text system, kept) — then opens
# their Easy NPC call dialog at $(label): the caller's face renders in the dialog GUI, their lines
# read as the call, and a "Hang up" close button ends it. The caller's ON_CLOSE_DIALOG trigger
# (easy_npc delete @s) despawns it on hang-up; the pre-spawn delete below sweeps any straggler.
# $(caller)=preset filename, $(tag)=body tag, $(label)=dialog entry label.
# OPEN IS DEFERRED (fixes "the call never opened the dialog", 0.6.0-alpha.14): easy_npc
# preset import_new spawns the body this tick, but its DialogData is not registered until a
# tick or two later, so a same-tick `dialog open` silently no-opped. Stash {tag,label} in
# storage and schedule phone/open_tick 5t out — the body is fully initialised by then.
# Single-player only (CLAUDE.md), so the deferred open targets @a[limit=1] = the player.
$execute at @s run easy_npc delete @e[type=easy_npc:humanoid,tag=ci_phone_caller,distance=..64]
$execute at @s run easy_npc preset import_new data easy_npc:preset/humanoid/$(caller).npc.snbt ~ ~ ~
$data modify storage cobblemon_initiative:phone open set value {tag:"$(tag)",label:"$(label)"}
schedule function cobblemon_initiative:phone/open_tick 5t
