# repairs wave a28 — arm: 0.7.0-alpha.11 Gaviota gym follow-up (playtest corrections).
# Scope: Jr. Apprentice + Apprentice reverted from EYESIGHT (a27) back to talk-to-battle (now gated
# behind gaviota_drained — battleable only once the arena is fully drained), so their a27 eyesight
# bodies must die + re-latch from the recompiled talk-to-battle presets at the same ring spots; plus
# Tally Clerk Bram (gaviota_manifest_c) moved from the deep pier to 436.5/64/3491.5. Needs its own
# #repair_a28 guard because worlds that installed the a27 pack already have #repair_a27 set.
scoreboard players set #repair_a28 ci_ambient 1
forceload add 555 3640 625 3655
schedule function cobblemon_initiative:install/repairs_a28_apply 3s
