# Paid nurse healing. Run as the player (a healer NPC's heal button — every Center nurse
# routes here, so the fee is tuned in one place).
# FEE RIDES BADGES + THE INSTABILITY INDEX, TIMES THE MODMENU MULTIPLIER (showrunner
# 2026-08-03, retuned same day to "it needs to hurt every time you have to pay it"):
#   fee = (200 + 100 x badges (@s memory_fragment) + 2 x #idx cd_instability)
#         x #cfg_fee_mult / 100
# At the default 100% multiplier: 200 pre-badge-1, ~300 after gym 1, ~850-900 after
# Kalahar, ~1250+ in the endgame — with visible relief when liberations claw the index
# back. #cfg_fee_mult is the ModMenu "Fee Multiplier (%)" (Economy & Services tab, Recurring Fees),
# mirrored into cd_const by UtilityFeeManager.tick every ~10s (config->scoreboard bridge);
# the unless-guard below defaults it to 100 if the mirror has not run yet. NOT a random
# price: every driver is deterministic (randomness invariants, ENGINE_FINDINGS §3). The
# live fee is printed on the receipt and the decline line; heal buttons say "posted rate",
# never a hard number.
# Compute -> storage -> macro (heal_paid_fee) carries $(fee) into the pay probe.
scoreboard players set #fee cd_calc 200
scoreboard players set #fee_var cd_calc 0
execute if score @s memory_fragment matches 0.. run scoreboard players operation #fee_var cd_calc = @s memory_fragment
scoreboard players operation #fee_var cd_calc *= #hundred cd_const
scoreboard players operation #fee cd_calc += #fee_var cd_calc
scoreboard players set #fee_var cd_calc 0
execute if score #idx cd_instability matches 0.. run scoreboard players operation #fee_var cd_calc = #idx cd_instability
scoreboard players operation #fee_var cd_calc *= #two cd_const
scoreboard players operation #fee cd_calc += #fee_var cd_calc
execute unless score #cfg_fee_mult cd_const matches 1.. run scoreboard players set #cfg_fee_mult cd_const 100
scoreboard players operation #fee cd_calc *= #cfg_fee_mult cd_const
scoreboard players operation #fee cd_calc /= #hundred cd_const
execute store result storage cobblemon_initiative:economy fee int 1 run scoreboard players get #fee cd_calc
function cobblemon_initiative:economy/heal_paid_fee with storage cobblemon_initiative:economy
