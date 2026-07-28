# repairs wave a17 — arm: Hua Zhan alpha.25 playtest wave (0.6.0-alpha.25).
# Scope (all latch NPCs — uuid'd bodies refresh in place via update_npc_presets):
#   * four warden statues re-latched for the NEW posed/armed visuals (poses, Stone
#     Knight sword+shield+facing, Quiet Garden tip-over) — coords unchanged
#   * Yan the yield analyst MOVED gym gate (1505.5,86,2043.5) -> branch office door
#     (1546.5,84,2002.5); kill by tag yield_analyst so nothing else is touched
#   * Auntie Song MOVED (1538.5,86,2064.5 -> 1534.5,86,2058.5) + new grandma skin +
#     price-stop-3 dialog; bare r3 kill (nearest uuid body, Linh Hua, is 7.8 away)
#   * Madam Qiu RELEASED (char deleted) — kill her stale body at 1488.5,87,2090.5,
#     NO latch reset (the placement row no longer exists)
#   * Bo Huan re-latched for the seven-colors page split (coords unchanged)
#   * Ning MOVED (1540.5,86,2001.5 -> 1538.5,87,1999.5); kill by tag hz_office_staff
#     so the Ping wheat-trader uuid body one desk over can NEVER be hit
#   * Lan the analyst re-latched for the hz_office_staff sensor re-tag; kill by tag
#     hz_analyst (coords unchanged)
#   * Cloud the Wooloo MOVED home (1513.5,84,1988.5 -> 1510.5,84,1990.5); kill by his
#     unique dedup tag ci_amb_companion_wooloo (catches the y76 wander-drifted body)
#   * Scorchspire healer re-latched for the rumor-hub page split (coords unchanged)
#   * Nurse Mei Lin's uuid body tp'd to the counter spot (playtest ping P1) — the one
#     uuid-body move in this wave; every other uuid re-cast refreshes in place
# Forceload the sites so the apply's kills run with the bodies live, then schedule.
# Guards itself via #repair_a17 so it applies exactly once per world.
scoreboard players set #repair_a17 ci_ambient 1

# Hua Zhan cluster: statues + market row + office + gym gate + mill (covers Cloud's
# wander drift radius around the old home) + the Pokemon Center (the nurse-body tp —
# a uuid body in an unloaded chunk is not addressable). One rectangle, removed in apply.
forceload add 1424 1968 1560 2160

# Scorchspire healer site (3672,68,4576)
forceload add 3664 4568 3680 4584

schedule function cobblemon_initiative:install/repairs_a17_apply 3s
