# repairs wave a38 — apply: place the ONE remaining static Kalahar prop (chunk forceloaded by the
# arm). REWORKED to the repeatable-hunt design: the found treasure chests are now buried at
# hunt-START (sidequest/kalahar_hunt/*), NOT here. Only the flavor deposit prop stays static.

# ── Warden Ossa's WARDEN'S DEPOSIT — the named deposit prop at the records post ──
# Empty iron-box flavor chest beside Ossa (2050/129/4085). The real turn-in is Ossa's dialog
# (sidequest/warden_ossa/deposit) probing the player's carried Warden's Cache item; this chest is
# just the physical filing point / set dressing.
setblock 2051 129 4085 minecraft:chest[facing=west]{CustomName:'{"text":"The Warden Deposit","color":"gold"}'}

forceload remove 2051 4085 2051 4085
