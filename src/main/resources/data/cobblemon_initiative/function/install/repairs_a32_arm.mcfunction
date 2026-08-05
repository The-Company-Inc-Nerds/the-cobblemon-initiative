# repairs wave a32 — arm: 0.7.0-alpha.20 playtest wave (2026-08-04 log).
# Scope: (1) gym6→7 route cast rework — Rho + Grid-Reader Chike CUT (latch bodies killed, no
# re-latch), Line Tech Volta MOVED out of the buried substation onto the ridge (latch kill +
# score reset -> respawns at the new placement), Byte Razer uuid body to his new pylon-bend
# post. (2) Safari cast anchor TPs — Rurik and Varek were wander bodies that drifted off their
# marked posts; their a20 presets are stationary, so one tp each converges live saves and fresh
# installs on the dev-log pins (a14 rule: this tp also fires on a fresh install's install-run).
# (3) Tag hygiene for the cut content (Rho's defeat tag, Darik's retired apple turn-in).
scoreboard players set #repair_a32 ci_ambient 1
# Rho (1512 77 1722) + Chike (1525 64 1698) + Volta old spot (1520 64 1710)
forceload add 1512 1722
forceload add 1525 1698
forceload add 1520 1710
# Byte Razer old + new posts
forceload add 1582 1716
forceload add 1569 1796
# Safari squatter drift band — Rurik + Varek wander ranges (home ±10) in one block-range add
forceload add 1460 1610 1500 1665
schedule function cobblemon_initiative:install/repairs_a32_apply 3s
