# Company HQ access enforcement — load (playtest 2026-08-05 canon: the HQ is the built
# tower x1608-1636 z1086-1118; ground/lobby y~92, penthouse floors y179/y197). Registered
# in data/minecraft/tags/function/load.json alongside sidequest/minutes/load.
# ci_hq_kick_cd: escort-out cooldown (200t = 10 s), set by villain/hq_bounce and
# villain/hq_bounce_penthouse, decayed 1/t by villain/hq_tick — the sidequest/minutes
# ci_kick_cd shape, namespaced so the two escorts never eat each other's cooldowns.
scoreboard objectives add ci_hq_kick_cd dummy
