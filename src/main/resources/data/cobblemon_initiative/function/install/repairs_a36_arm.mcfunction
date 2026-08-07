# repairs wave a36 — arm: playtest 2026-08-06 — Cyber City / Cyber-approach reseats.
# Scope: Prism (uuid) + Jax Databet (uuid) nudge moves. uuid tps also fire on a fresh
# install's install-run (a14 rule); no bundled-map nbt edit needed. Guarded once-per-world
# by #repair_a36 like every wave.
scoreboard players set #repair_a36 ci_ambient 1
# Prism — old post (1336 1184) + new post (1342 1177); one block-range covers both columns.
forceload add 1336 1177 1342 1184
# Jax Databet — old post (file CSV comment says 1718/1198; ambient_wander so the body may
# be several blocks off) + new post (1718 1189). Box spans both columns wide.
forceload add 1712 1185 1722 1200
schedule function cobblemon_initiative:install/repairs_a36_apply 3s
