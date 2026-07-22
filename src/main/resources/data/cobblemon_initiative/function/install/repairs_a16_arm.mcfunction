# repairs wave a16 — arm: Victor descent rework (0.6.0-alpha.16).
# alpha.15 parked Victor permanently at the reveal PATH (2536/106/2900). The new design keeps
# him UP on the grain tower (2522/131/2815) until the player EARNS the transform, then he
# descends to the path (see ambient/tick + sango/victor_descend / victor_arrive_path). This wave
# reconciles an already-installed save: forceload BOTH sites so the apply's kills/checks run with
# the bodies live, then (in apply) either adopt the a15 path body as the descended Victor (player
# already qualified) or move him back UP to the tower (not yet qualified). Guards via #repair_a16.
scoreboard players set #repair_a16 ci_ambient 1

# Grain-tower top (2522/131/2815) — the same four chunks a15 forceloaded for its tower kill.
forceload add 2512 2800
forceload add 2512 2816
forceload add 2528 2800
forceload add 2528 2816
# Reveal path (2536/106/2900) — the a15 body sits here; cover its 8-block kill radius.
forceload add 2528 2888
forceload add 2544 2888
forceload add 2528 2900
forceload add 2544 2900

# 5s (not 3s): on a save that never ran a15 (e.g. an alpha.14 save jumping straight to a16),
# a15_arm and a16_arm both dispatch this pass and a15_apply resets #amb_victor UNGATED at 3s —
# scheduling a16_apply strictly later guarantees it reconciles against fully-settled a15 state.
schedule function cobblemon_initiative:install/repairs_a16_apply 5s
