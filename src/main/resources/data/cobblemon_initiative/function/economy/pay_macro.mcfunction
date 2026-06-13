# Macro (run as @s=player): pay the computed CobbleDollars and show the verified rate.
# Args: paid, rate, raw.
$cobbledollars add @s $(paid)
$title @s actionbar [{"text":"Company Verified Rate ","color":"gold"},{"text":"$(rate)%","color":"yellow"},{"text":"   ","color":"gray"},{"text":"$(raw)","color":"dark_gray"},{"text":" → ","color":"gray"},{"text":"$(paid) ","color":"green"},{"text":"CD","color":"dark_green"}]
