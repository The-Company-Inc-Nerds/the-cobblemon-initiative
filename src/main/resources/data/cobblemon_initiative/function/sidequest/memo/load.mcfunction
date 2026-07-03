# Per My Last Memo — checkpoint loiter timer objective. Register in #minecraft:load
# (orchestrator wires the tag; this file must NOT edit function tags itself).
# ci_loiter counts ticks a player has held position at the checkpoint tent while
# no checkpoint agent has can_see_player=1. 160 ticks (8 s) arms tag memo_loiter.
scoreboard objectives add ci_loiter dummy
