# repairs wave a20 — arm: 0.7.0-alpha.2 playtest wave (marsh crash + notes).
# Scope:
#   * Hua Zhan: Linh Hua uuid tp to the new stall row; Bo Huan re-latched to the
#     market corner (1502,84,2087); Cloud the Wooloo re-latched beside the cart
#     (tag-kill catches wander drift); Rong uuid tp UP onto the loft (1533.5 99 2000.5,
#     yaw -151 — her static cone is the new eavesdrop hazard) + belt-and-braces
#     hz_office_staff sensor tag (the preset-refresh addTag also delivers it)
#   * Mystic Marsh: Fen-Nurse Wisteria latch body killed (character CUT — Liora
#     Starquill on the Center body is the marsh nurse now)
#   * Fairy shrine: vows 1-4 re-latched one block higher; the Fifth Vow allay killed
#     (character CUT — resolve is sworn to Aurora); Aurora re-latched at y1.5
#     (float:true + Root.Scale 1.2 ride the new preset)
scoreboard players set #repair_a20 ci_ambient 1

# Hua Zhan market + branch office cluster (Linh 1544/2048, Bo Huan 1512/2085 -> 1502/2087,
# Cloud ~1506/1988 -> 1502/2085, Rong 1541/1992 -> 1533/2000)
forceload add 1495 1980 1550 2095

# Mystic Marsh Center row (Wisteria latch 1068/2465)
forceload add 1060 2457 1076 2473

# Fairy shrine drowned stair, top to floor (vows 943/2644 .. 928/2716, Aurora 947/2703)
forceload add 916 2640 984 2724

schedule function cobblemon_initiative:install/repairs_a20_apply 3s
