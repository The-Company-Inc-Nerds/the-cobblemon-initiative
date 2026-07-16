# Heritage Acquisitions (Ryujin SQ2) - the peaceful cite-back path. Run AS the player
# (dialog cite button, gated on ryujin_charter_read). Cite the charter at the envoy: raised
# in oath, not in coin. Sets ryujin_heritage_settled fail-soft, pays 800 CD (smaller than the
# 1200 battle prize - the peaceful route pays less) + a standard training gift. The envoy body
# persists (despawn_on_win false on the character); the settled dialog entry then delivers his
# withdrawn line on this and every route (OQ2 ruling: left standing, humiliated).
tag @s add ryujin_heritage_settled
function cobblemon_initiative:economy/payout {amount:800}
function cobblemon_initiative:economy/reward/standard
title @s title [{"text":"WITHDRAWN PENDING CLARIFICATION","color":"gold"}]
title @s subtitle [{"text":"There was no one left to clarify it.","color":"gray"}]
