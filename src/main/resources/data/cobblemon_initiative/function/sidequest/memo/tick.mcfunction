# Per My Last Memo — Voluntary Verification Checkpoint poller. Register in #minecraft:tick
# (orchestrator wires the tag). Cheap: every line is gated on the checkpoint_agent
# entity tag, which only exists on the two tent agents (villain_grunt_1/2, tagged at
# placement). Once both agents are beaten they despawn and the meter dies with the tent.
#
# NPC Sight is the sensor: the agents are npcsight-registered, so the mod writes their
# can_see_player scoreboard every tick (scoreboard-as-IPC, the documented pattern).

# One-time approach beat (constraint-safe text per the doc).
execute as @a[tag=!ckpt_warned] at @s if entity @e[tag=checkpoint_agent,distance=..30] run function cobblemon_initiative:sidequest/memo/approach_warn

# Leaving the tent area resets loiter progress (the 8 s must be continuous).
execute as @a[scores={ci_loiter=1..}] at @s unless entity @e[tag=checkpoint_agent,distance=..14] run scoreboard players reset @s ci_loiter

# EYES ON YOU / CLEAR meter + loiter countdown for players at the tent who lack the memo.
execute as @a[tag=!memo_heard] at @s if entity @e[tag=checkpoint_agent,distance=..14] run function cobblemon_initiative:sidequest/memo/near_tent

# Fight path (fork C — both paths valid): both agents beaten -> recover the memo
# from the abandoned checkpoint paperwork. memo_heard latches it one-time.
execute as @a[tag=!memo_heard,tag=defeated_villain_grunt_1,tag=defeated_villain_grunt_2] run function cobblemon_initiative:sidequest/memo/recover_memo
