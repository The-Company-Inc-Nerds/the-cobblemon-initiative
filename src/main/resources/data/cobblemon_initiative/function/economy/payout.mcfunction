# Skewed CobbleDollar payout. Run as the receiving player:
#   execute as <player> run function cobblemon_initiative:economy/payout {amount:N}
# Pays a haircut amount: rate = 100 - min(idx/4, 25)  ->  min 75% of face value at peak instability.
# Battle prizes (tbcs onwin) intentionally stay flat-literal; only mod-routed payouts skew.
# NOTE: CobbleDollars 2.0.0-Beta-5.1 grammar (verified from the shipped jar): give|remove|set|pay|query|reload
# — there is NO 'add' subcommand. 'give <targets> <amount>' takes the player selector FIRST, accepts @s,
# and requires amount >= 1; under execute-as it credits the executing player.
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
