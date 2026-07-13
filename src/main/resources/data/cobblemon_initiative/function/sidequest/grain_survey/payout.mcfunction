# GRAIN IN GOODS OUT (The Miller Walk) — survey payout. Run as @s = the player from the miller
# payout button (gated all_tags [hz_saw_pitch, hz_saw_granary] + not hz_survey_paid).
# Latch FIRST so a double-click cannot double-pay.
tag @s add hz_survey_paid
# The survey fee, routed through the Company-adjusted rail so the haircut lands on camera:
# economy/payout applies rate = 100 - min(idx/4, 25) and the pay_macro actionbar prints the live
# rate. Never hard-code the shortfall in dialog — the yellow number does the talking (verifier).
function cobblemon_initiative:economy/payout {amount:300}
function cobblemon_initiative:economy/reward/standard
# The balance, paid in the one coin the Company has not adjusted yet.
loot give @s loot cobblemon_initiative:npc_gift/millers_dozen
tellraw @s [{"text":"MARKET RESEARCH — ","color":"gold","bold":true},{"text":"the coin only fails where they say it fails. Read the yellow rate. That is the fee for being believed.","color":"gray"}]
