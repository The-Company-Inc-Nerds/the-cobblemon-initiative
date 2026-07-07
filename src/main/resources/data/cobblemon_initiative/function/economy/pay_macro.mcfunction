# Macro (run as @s=player): pay the computed CobbleDollars and show the verified rate.
# Args: paid, rate, raw.
# TONE RULE (showrunner 2026-07-06): the DEFAULT rail is UNBRANDED — civilians pay for
# almost every quest, and stamping the villain's name on their money made the whole map
# read as Company payroll. "Verified" alone carries the slogan's echo. The branded
# receipt lives in pay_macro_company and fires ONLY where taking Company money is the
# point (census sign fork, courier sell fork, Invitational purse, Adjusted Retail).
$cobbledollars give @s $(paid)
$title @s actionbar [{"text":"Verified Rate ","color":"gold"},{"text":"$(rate)%","color":"yellow"},{"text":"   ","color":"gray"},{"text":"$(raw)","color":"dark_gray"},{"text":" → ","color":"gray"},{"text":"$(paid) ","color":"green"},{"text":"CD","color":"dark_green"}]
