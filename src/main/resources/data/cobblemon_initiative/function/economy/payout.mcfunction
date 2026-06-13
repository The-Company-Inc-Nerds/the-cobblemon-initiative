# Skewed CobbleDollar payout. Run as the receiving player:
#   execute as <player> run function cobblemon_initiative:economy/payout {amount:N}
# Pays a haircut amount: rate = 100 - min(idx/4, 25)  ->  min 75% of face value at peak instability.
# Battle prizes (tbcs onwin) intentionally stay flat-literal; only mod-routed payouts skew.
# NOTE: 'cobbledollars add @s' inside execute-as is unverified in-repo — SMOKE-TEST before P4 relies on it.
$scoreboard players set #raw cd_calc $(amount)
scoreboard players operation #cut cd_calc = #idx cd_instability
scoreboard players operation #cut cd_calc /= #four cd_const
execute if score #cut cd_calc matches 26.. run scoreboard players set #cut cd_calc 25
scoreboard players set #rate cd_calc 100
scoreboard players operation #rate cd_calc -= #cut cd_calc
scoreboard players operation #paid cd_calc = #raw cd_calc
scoreboard players operation #paid cd_calc *= #rate cd_calc
scoreboard players operation #paid cd_calc /= #hundred cd_const
execute store result storage cobblemon_initiative:economy raw int 1 run scoreboard players get #raw cd_calc
execute store result storage cobblemon_initiative:economy rate int 1 run scoreboard players get #rate cd_calc
execute store result storage cobblemon_initiative:economy paid int 1 run scoreboard players get #paid cd_calc
function cobblemon_initiative:economy/pay_macro with storage cobblemon_initiative:economy
