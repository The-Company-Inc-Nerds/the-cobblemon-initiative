# Dalia Sefet's date-palm rescue (Kalahar SQ, playtest 2026-08-06 N16) — the player brought
# 2x Mystic Water for her dying date palms. Run AS the player from the claim dialog button
# (claim entry gated dalia_fetch_in + not dalia_fetch_done). ONE-SHOT: guarded once with
# `unless dalia_fetch_done`, tag latches before any grant (lysira/reward + nifl/lanterns_reward
# pattern). Pays 250 via the skewed payout, plus a MINOR training pack (candy scales with era)
# — one-time completion payout, NOT a repeatable/daily loop (hardcore no-farm rule).
execute if entity @s[tag=dalia_fetch_done] run return 0
tag @s add dalia_fetch_done
function cobblemon_initiative:economy/payout {amount:250}
function cobblemon_initiative:economy/reward/minor
tellraw @s [{"text":"The date palms drink. ","color":"gold"},{"text":"Dalia pours the mystic water slow around each dry crown and, for the first time in a season, the roots take it. She presses a fold of coin into your hand and does not say thank you — she does not have to.","color":"gray"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8
execute at @s run particle minecraft:falling_water ~ ~1 ~ 0.6 0.4 0.6 0.0 24
