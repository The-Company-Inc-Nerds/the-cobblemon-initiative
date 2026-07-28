# repairs wave a21 — arm: 0.7.0-alpha.3 playtest wave (36 NPC notes + 13 pins).
# Scope:
#   * Fairy shrine: First Vow allay re-latched (one-button start + re-homed reveal cutscene
#     ride the new preset)
#   * Mystic Marsh: Bryn re-latched (kid skin single/marsh_child); Thistrel uuid tp home to
#     the stall spot (wander -> stationary vendor); Titania uuid re-aim yaw 180 (faces the
#     arena at rest); stray phone-caller body parked off the battle stage to the SE lane;
#     Mom-call re-ring heal (the a2 same-pass collision ate her congratulation call)
#   * Mirebloom Paddies: old Steward Halvard body killed at the fence anchor (re-roled to
#     the farmer at 1222/2821) — the kill also takes Nao's stacked body, both latches re-armed
#   * Deepcore: Sten Vale re-latched pit-head -> east-row Pokemart; Rilka same-spot re-latch
#     (de-nursed to quarry civilian; Orrin's uuid body is the Center nurse now); Osei killed
#     (character CUT, Bruno two-track); Ken re-latched (new dc_pit_lead tag)
#   * Gaviota: Coralie latch body killed (character retired — Lucia Marelli's uuid body is
#     the Center nurse), Lucia tp'd into the Center AFTER the kill; Marlin tp'd behind the
#     mart CobbleMerchant register
scoreboard players set #repair_a21 ci_ambient 1

# Fairy shrine First Vow (943/2644)
forceload add 935 2636 951 2652

# Mystic Marsh: Bryn (1064/2470), Thistrel stall (1122/2330), Titania stage+body (943/2427-2451),
# SE lane caller park (1210/2502)
forceload add 1052 2458 1076 2482
forceload add 1110 2318 1134 2342
forceload add 936 2427 950 2451
forceload add 1203 2495 1218 2510

# Mirebloom fence gate + Halvard's paddy house (1222-1229/2817-2821)
forceload add 1215 2810 1235 2830

# Deepcore gym block (Osei 996/3188, Ken 984/3173)
forceload add 950 3150 1020 3200

# Deepcore mart/center rows (Sten old 1100/3215 -> new 1160/3268, Rilka 1092/3208)
forceload add 1085 3200 1168 3276

# Gaviota Center + Open podium (Coralie 560/3540, Lucia source 586/3435)
forceload add 555 3430 592 3546

# Gaviota mart / championship pier (Marlin 655/3537 -> 656/3533)
forceload add 650 3528 661 3543

schedule function cobblemon_initiative:install/repairs_a21_apply 3s
