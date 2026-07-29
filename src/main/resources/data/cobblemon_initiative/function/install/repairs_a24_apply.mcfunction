# Kill the old Warning Buoy body at the previous latch spot + reset the latch so it re-imports at the
# new water's-edge spot (259.5/62/2351.5) on next approach. Nearest other humanoid is far (Mystic
# Island shore) — r4 safe.
execute positioned 234.5 65.0 2347.5 run kill @e[type=easy_npc:humanoid,distance=..4]
scoreboard players set #amb_noble_monument_kyogre ci_ambient 0
forceload remove 228 2341 266 2358
