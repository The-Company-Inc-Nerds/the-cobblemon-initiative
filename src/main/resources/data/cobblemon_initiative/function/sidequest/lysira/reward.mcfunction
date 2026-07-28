# The Lamplighters Round (Mystic SQ Q2 / N26, Lysira Dewfen) — 8 glowstone dust refills
# the round. Run AS the player from the claim dialog button (claim entry gated
# lysira_round_in + not lysira_round_done). Single-fire: guarded once with unless
# lysira_round_done, tag latches before any grant (nifl/lanterns_reward pattern).
# Pays 300 via the skewed payout; keepsake 2 soul lanterns (wisp-glass — same lineage as
# the kid-loaned lantern in mm_will_o_wisp_child). The tellraw beat is the FAIRY-SHRINE
# BREADCRUMB: the LAST lamp on the round looks over the south dock, and the waters go
# DOWN there — sylthra_veilsting carries the matching drowned-stair hint at the ferry.
execute if entity @s[tag=lysira_round_done] run return 0
tag @s add lysira_round_done
function cobblemon_initiative:economy/payout {amount:300}
give @s minecraft:soul_lantern 2
tellraw @s [{"text":"The round is lit. ","color":"gold"},{"text":"Lysira walks you to the last lamp on the round — it hangs looking over the south dock. The waters go down there, she says. Lanterns only ever mind what goes down.","color":"gray"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.7 0.8
