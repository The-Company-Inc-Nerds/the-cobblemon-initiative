# Paid nurse healing. Run as the player (a healer NPC's heal button — all four nurses
# route here, so the fee is tuned in one place).
# FEE RIDES THE INSTABILITY INDEX (quest-flow review B3, 2026-07-06):
#   fee = 100 + 2 x #idx cd_instability
# 100 at a stable index, 116 after gym 1, ~212 at the act-2 peak — and visible relief
# when liberations claw the index back. NOT a random price: cd_instability is the ONLY
# sanctioned price driver (randomness invariants, ENGINE_FINDINGS §3). The live fee is
# printed on the receipt and on the decline line; heal buttons say "posted rate".
# Compute -> storage -> macro (heal_paid_fee) carries $(fee) into the pay probe.
scoreboard players set #fee cd_calc 100
scoreboard players set #fee_var cd_calc 0
execute if score #idx cd_instability matches 0.. run scoreboard players operation #fee_var cd_calc = #idx cd_instability
scoreboard players operation #fee_var cd_calc *= #two cd_const
scoreboard players operation #fee cd_calc += #fee_var cd_calc
execute store result storage cobblemon_initiative:economy fee int 1 run scoreboard players get #fee cd_calc
function cobblemon_initiative:economy/heal_paid_fee with storage cobblemon_initiative:economy
