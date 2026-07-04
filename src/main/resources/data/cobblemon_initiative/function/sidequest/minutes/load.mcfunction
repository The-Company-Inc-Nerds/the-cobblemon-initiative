# Minutes of the Quarterly Review — branch office loiter objective. Register in
# #minecraft:load (orchestrator wires the tag; this file must NOT edit function tags
# itself). ci_loiter_hz counts TICKS a player has held the door-side landing outside
# the Branch Manager office while no office staff (entity tag hz_office_staff) has
# can_see_player=1. 160 ticks (8 s) fires hear_minutes — same units, same threshold,
# same countdown bands as the shipped sidequest/memo checkpoint loiter.
#
# DELIBERATELY NOT the shared ci_loiter objective: sidequest/memo/tick.mcfunction
# resets ci_loiter every tick for any scoring player who is not within 14 blocks of a
# checkpoint_agent — which is every player standing in this office — so sharing the
# objective would zero this quest forever. A namespaced clone keeps both pollers honest.
scoreboard objectives add ci_loiter_hz dummy
