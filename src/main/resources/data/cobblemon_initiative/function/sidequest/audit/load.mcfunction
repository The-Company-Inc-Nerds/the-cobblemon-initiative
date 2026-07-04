# Greenspace 7, Under-Performing — gym-gate loiter timer objective. Register in #minecraft:load
# (orchestrator wires the tag via load_functions; this file must NOT edit function tags itself).
# ci_audit counts ticks a player has held position at the Hua Zhan gym gate while the
# Yield Analyst has can_see_player=0. 160 ticks (8 s) arms tag audit_loiter.
scoreboard objectives add ci_audit dummy
