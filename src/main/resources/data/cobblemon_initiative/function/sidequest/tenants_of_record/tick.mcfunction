# Tenants of Record — farm_1_free bridge latch. Register in #minecraft:tick (orchestrator wires the tag).
# VERIFIER FIX 3: quest/tick is throttled to ~1s and its refresh is gated on the HUD being shown, so the
# latch cannot live behind that gate. It lives here instead: an always-on, top-level tick function —
# the tag latches even with the HUD hidden. Cost: one score check per tick (right_of_way precedent).
# farm_1 field_freed is set to 1 by liberation/free_field {field:farm_1} (Site Manager on_win);
# the objective itself is created by liberation/load on datapack load.
execute if score farm_1 field_freed matches 1 run tag @a[tag=!farm_1_free] add farm_1_free

# Escort tick while a homecoming walk is live (round 13c).
execute as @a[tag=homecoming_walking] at @s run function cobblemon_initiative:sidequest/tenants_of_record/walk_tick
