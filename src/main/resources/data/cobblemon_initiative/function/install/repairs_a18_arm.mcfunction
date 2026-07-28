# repairs wave a18 — arm: 0.7.0-alpha.1 playtest wave (Hua Zhan -> Mystic Marsh ->
# fairy shrine run, notes N1-N30 / P1-P8).
# Scope:
#   * Stone Knight (hz_statue_terrace) re-latched for the new scale-1.3 visuals; Quiet
#     Garden warden (hz_statue_pond) lowered y 85 -> 84 — both kill + latch reset
#   * Nurse Mei Lin uuid body tp refined 1435.3 -> 1432.5 90 2151.5 (N3, a17 precedent)
#   * Nana the Chansey MOVED mill yard -> Pokemon Center beside the nurse
#     (1536.5,93,2005.5 -> 1436.5,90,2148.5); kill by dedup tag (catches wander drift)
#   * Rong uuid body tp Kaito-house roof -> branch office loft 1541.5 96 1992.5 (N7)
#   * Bo Huan MOVED 1512.5,85,2082.5 -> 1512.5,84,2085.5 (N5); wool re-cast itself
#     (Shu=wool, Kaito=redirect, Chen Bao static, office sensor range) rides
#     update_npc_presets — no kills for the uuid bodies
#   * Leader Blossom: if already transformed, her hz_leader_body is killed and
#     re-imported at the P1 battle spot 1381.2 93 2047.4 with the corrected skin
#     (the swap-back of leader/groundskeeper textures); untransformed worlds get the
#     new spot from the updated aya_transform
#   * Mystic Marsh: Elowen tp behind her stall (N13), Branith tp NW (N21), Rowan tp to
#     the final-gatekeeper bend 1233.5 65 2368.5 (N12), Osric MOVED to gym plaza
#     1111.5,66,2444.5 (N20), Sedge re-latched for the perch-gift dialog (N19),
#     Bramblea + Morveth uuid bodies RELEASED (converted to fairy-/bogged-type latch
#     characters — new bodies spawn from their latches on approach)
#   * Fairy shrine descent redesign (P3-P8): Last Pilgrim RELEASED (char deleted, no
#     latch reset), old Aurora humanoid body killed + latch reset — she respawns as the
#     fairy-type body on the drowned floor; the five allay vows are NEW latches and
#     need no repairs
# Forceload the sites so the apply runs with the bodies live, then schedule.
# Guards itself via #repair_a18 so it applies exactly once per world.
scoreboard players set #repair_a18 ci_ambient 1

# Hua Zhan cluster, WIDENED west vs a17 to cover the Blossom battle spot (1381,2047):
# statues + wool house + Center + office loft + apricorn cart + garden arena.
forceload add 1368 1960 1560 2170

# Mystic Marsh cluster: boardwalk moves + conversions + Rowan's bend (1233,2368).
forceload add 1020 2320 1250 2530

# Fairy shrine floor: Last Pilgrim + old Aurora sites.
forceload add 936 2700 960 2724

schedule function cobblemon_initiative:install/repairs_a18_apply 3s
