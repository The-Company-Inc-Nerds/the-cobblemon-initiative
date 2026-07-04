# Greenspace 7, Under-Performing — Yield Analyst audit loiter poller. Register in
# #minecraft:tick via the orchestrator (tick_functions) — VERIFIER FIX 2: NOT quest/tick,
# which is the throttled HUD poller, not the sidequest tick bus.
# Cheap: every line is gated on the yield_analyst entity tag, which only exists on the one
# analyst at the Hua Zhan gym gate (tagged at placement). Once the report is lifted the
# yield_report_taken player tag silences the meter for good.
#
# NPC Sight is the sensor: the analyst is npcsight-registered, so the mod writes his
# can_see_player scoreboard every tick (scoreboard-as-IPC, the documented pattern).

# One-time approach beat.
execute as @a[tag=!audit_warned] at @s if entity @e[tag=yield_analyst,distance=..30] run function cobblemon_initiative:sidequest/audit/approach_note

# Leaving the gate area resets loiter progress (the 8 s must be continuous).
execute as @a[scores={ci_audit=1..}] at @s unless entity @e[tag=yield_analyst,distance=..6] run scoreboard players reset @s ci_audit

# ON THE RECORD / OFF THE RECORD meter + loiter countdown for players at the gate
# who have not yet lifted the Yield Report.
execute as @a[tag=!yield_report_taken] at @s if entity @e[tag=yield_analyst,distance=..6] run function cobblemon_initiative:sidequest/audit/near_gate
