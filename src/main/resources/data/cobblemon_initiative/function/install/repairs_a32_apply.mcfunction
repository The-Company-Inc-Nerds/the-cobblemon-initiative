# repairs wave a32 — apply: 0.7.0-alpha.20 playtest wave. Chunks forceloaded by the arm.

# ── (1a) Rho CUT (playtest N2 "Not needed") — latch body killed, character deleted, latch
# machinery regenerated without him. No #amb reset: a true cut, not a re-cast. His shared
# battle team villain_grunt_5 reverts to the neutral "Company Operative" nameplate.
execute positioned 1512.5 77 1722.5 run kill @e[type=easy_npc:humanoid,distance=..3]

# ── (1b) Grid-Reader Chike CUT (playtest N7 "Buried and not needed") ──
execute positioned 1525.5 64 1698.5 run kill @e[type=easy_npc:humanoid,distance=..3]

# ── (1c) Line Tech Volta MOVE (playtest N6): out of the buried substation shelf onto the
# ridge at 1564 76 1656. Latch NPC — kill the old body and reset the latch so the next
# approach respawns him from the recompiled placement (Tunde re-cast idiom).
execute positioned 1520.5 64 1710.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_r12_spotter_pylon ci_ambient 0

# ── (1d) Byte Razer uuid-body move (playtest N5): stationary body — plain tp, no
# Navigation.Home. Bundled-map entities edit deferred (the repairs tp also fires on a fresh
# install's install-run, a14 rule).
tp 69dc379e-4050-45aa-b2cf-5deac79cb508 1569.5 73.0 1796.5

# ── (2) Safari cast anchor TPs (playtest N9/N13): Rurik + Varek drifted while wandering;
# their a20 presets are stationary, so land them on the dev-log pins once.
tp f3463358-a50b-4b78-813c-dcd8d3465ea6 1470.5 84.0 1623.5
tp 776681fa-f4f7-420e-8110-31581ee7d210 1486.5 80.0 1644.5

# ── (3) Tag hygiene for cut content: Rho's defeat tag and Darik's retired apple turn-in
# (Darik is a Safari employee now; the quest no longer exists).
tag @a remove defeated_villain_route_agent_12
tag @a remove darik_apples_done

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 1512 1722
forceload remove 1525 1698
forceload remove 1520 1710
forceload remove 1582 1716
forceload remove 1569 1796
forceload remove 1460 1610 1500 1665
