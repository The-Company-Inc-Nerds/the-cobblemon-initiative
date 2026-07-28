# repairs wave a19 — arm: audit rulings wave (0.7.0-alpha.1, same-day follow-up).
# Scope (latch bodies whose presets changed; uuid bodies refresh via update_npc_presets):
#   * Tunde (sango_company_liaison) re-latched — podium double-purse entry deleted,
#     re-voiced as Off-the-Record agent #1 (kill r3 only: Musa carts ~13 blocks away)
#   * auditors Bomani/Jelani re-latched — dead stand_and_be_counted entries removed
#   * Dune the Hippopotas (ex fourth Puddle) re-latched for the rename
#   * ghost-cast/scenario save hygiene: drop the retired ci_notices objective and strip
#     tags only harness/scenario saves can hold (checkpoint/notice/memo/errand latches)
scoreboard players set #repair_a19 ci_ambient 1

# Sango cluster (Tunde 2581/2822, auditor_a 2611/2792, auditor_b 2578/2942)
forceload add 2560 2780 2630 2950

# Kalahar hippo latch (2078 121 3947)
forceload add 2070 3940 2090 3956

schedule function cobblemon_initiative:install/repairs_a19_apply 3s
