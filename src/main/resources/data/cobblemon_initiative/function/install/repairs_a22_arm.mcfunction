# repairs wave a22 — arm: 0.7.0-alpha.4 playtest wave.
# Scope:
#   * Deepcore gym: the 4 floor masters + 2 pit apprentices re-latch to their a22 PASSIVE PVP-flavor
#     bodies (old engage:touch / pursue bodies killed BY TAG — they used ambient_wander and drift off
#     the post — so the new preset takes; the whole-dojo gauntlet then spawns hostile duel bodies).
#   * Gaviota west quay: Zwiggo Man -> Deckhand Rocko re-latch (old sitting fairy-skin body killed);
#     the swampert is now a quay LATCH body (was summon-only) — kill any old summoned body and re-arm
#     its new latch, but ONLY if the player has not already recruited it (zwiggo_joined).
#   * NO repairs needed for: Nurse Marina rename + Fuslie re-skin (uuid bodies refresh in place via
#     update_npc_presets); the new civilian Lucia at the Open podium (fresh latch, spawns on install);
#     the retired fixed orc camps' stray ci_orc bodies (harmless + pin-local, the old spawn logic is gone).
scoreboard players set #repair_a22 ci_ambient 1
# Deepcore gym floor + pit (958-1016 x, 3156-3188 z, y129-142)
forceload add 952 3150 1020 3195
# Gaviota west quay (Zwiggo man 410/3501, swampert 406/3501)
forceload add 400 3494 416 3508
schedule function cobblemon_initiative:install/repairs_a22_apply 3s
