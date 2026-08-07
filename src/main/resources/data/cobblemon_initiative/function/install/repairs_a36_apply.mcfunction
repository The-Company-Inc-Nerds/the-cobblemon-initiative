# repairs wave a36 — apply: playtest 2026-08-06. Chunks forceloaded by the arm.

# ── Prism uuid-body move (playtest N5). 1336.5/100/1183.5 -> 1342.5/98/1177.5.
# Prism is ambient_stationary_look — Home is the anchor; reseat it to the new post.
tp c7d9f2a1-6b38-4e81-9f70-abcd1234ef56 1342.5 98.0 1177.5
data modify entity c7d9f2a1-6b38-4e81-9f70-abcd1234ef56 Navigation.Home set value {X:1342,Y:98,Z:1177}

# ── Jax Databet uuid-body move (playtest N7). -> 1718.5/90/1189.5. Jax is ambient_wander;
# resetting Navigation.Home reseats his stroll anchor to the new spot (the tp only
# positions the current body).
tp 72964746-c26f-42cf-9101-4b8b5d88f9df 1718.5 90.0 1189.5
data modify entity 72964746-c26f-42cf-9101-4b8b5d88f9df Navigation.Home set value {X:1718,Y:90,Z:1189}

# ── teardown ────────────────────────────────────────────────────────────────────────
forceload remove 1336 1177 1342 1184
forceload remove 1712 1185 1722 1200
