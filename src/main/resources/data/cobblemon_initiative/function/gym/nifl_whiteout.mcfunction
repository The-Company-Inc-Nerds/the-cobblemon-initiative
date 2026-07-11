# Nifl whiteout approach — the blizzard crossing before Leader Boreas. Register in
# #minecraft:tick (tags/function/tick.json — ORCHESTRATOR wires the tag); objective
# nifl_wo (per-player taunt cooldown, off_record obs_cd precedent) is added in
# gym/load (ORCHESTRATOR adds the line). Three Frost Sentinel statues (tag
# nifl_sentry, PASSIVE npcsight tag-profile range 12 — compiled from the
# nifl_sentry_1..3 sight blocks) write can_see_player every tick; the whole
# mechanic is this scoreboard-IPC consumer:
#   seen in the corridor  -> 3s slowness + mining fatigue, taunt, tag nifl_seen
#   reach the leader-side marker unseen -> latch nifl_whiteout_clear (once) —
#     flavor + the Boreas respect entry only, NO combat buff (hardcore fairness)
#   reach it seen        -> nifl_seen quietly cleared (retry on the next crossing)
#
# Corridor box: x 3618..3638, y 112..126, z 1901..1934 (sentries 3628/1912,
# 3624/1920, 3632/1928; leader-side marker 3628/119/1903; leader body 3628/119/1900).
# TODO(showrunner): confirm the box and every coordinate in nifl_whiteout_run.
#
# Cheap guard: the per-tick work only runs while someone is inside the corridor.
execute if entity @a[x=3618,y=112,z=1901,dx=20,dy=14,dz=33] run function cobblemon_initiative:gym/nifl_whiteout_run
