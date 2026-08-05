# repairs wave a33 — arm: 0.7.0-alpha.20 PokéPhone rework (calls moved off Easy-NPC dialogs onto
# the mod's own call screen; the invisible per-call caller bodies are cut from content).
# Scope: purge stray ci_phone_caller bodies from live saves — the phone/tick orphan sweep that
# used to backstop them is gone with the system, so any body a save still carries would linger
# invisibly forever. Callers spawned AT the player (loaded whenever the player is), but
# repairs_a21 parked one on the Takehara SE lane — forceload that spot so the kill reaches it.
scoreboard players set #repair_a33 ci_ambient 1
forceload add 1210 2502
schedule function cobblemon_initiative:install/repairs_a33_apply 3s
