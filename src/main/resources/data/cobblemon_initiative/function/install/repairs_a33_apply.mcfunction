# repairs wave a33 — apply: kill every loaded stray PokéPhone caller body (the invisible Easy NPC
# hosts of the retired dialog delivery). One-shot: the caller content and the phone/tick orphan
# sweep are both gone in a20, so this is the last cleaner these bodies will ever see.
kill @e[type=easy_npc:humanoid,tag=ci_phone_caller]

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 1210 2502
