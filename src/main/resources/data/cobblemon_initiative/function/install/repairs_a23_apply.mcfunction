# repairs wave a23 — apply (chunks live, 3s after arm).
# ── Gaviota Center/podium swap ──────────────────────────────────────────────────
# Kill Lucia's old podium latch body FIRST + reset her latch (she re-latches at the docks 474/3552),
# THEN tp Nurse Marina's uuid onto the podium (order matters — a21 nurse precedent).
execute positioned 585.5 90.0 3435.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_lucia_marelli ci_ambient 0
tp 603263f0-b8ef-4da3-945e-fdb7d0c16c5b 585.5 90.0 3435.5
# ── Daycare keepers (uuid tp onto their pens; were ambient_wander) ───────────────
tp f85cee27-f6a8-4114-a707-fafdc94ac59c 549.7 87.1 3477.2
tp afd87dc0-ff01-4363-9f49-7ee972f939dd 556.7 87.2 3493.8
# ── Zwiggo re-couple ────────────────────────────────────────────────────────────
# Kill the a22 quay-latch swampert (now summon-only) unless already recruited; drop its latch.
# Deckhand Rocco re-latch to the new spot.
execute unless entity @a[tag=zwiggo_joined] run kill @e[type=easy_npc:cobblemon_npc,tag=zwiggo_body]
scoreboard players reset #amb_zwiggo_swampert ci_ambient
execute positioned 410.7 64.0 3501.4 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_zwiggo_man ci_ambient 0
# ── teardown ────────────────────────────────────────────────────────────────────
forceload remove 555 3430 592 3546
forceload remove 545 3470 560 3510
forceload remove 400 3495 412 3504
forceload remove 470 3548 480 3556
