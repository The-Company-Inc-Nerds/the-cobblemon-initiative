# ADJUSTED RETAIL — turn-in guard. Run as the player from Kaito's turnin button.
# One-shot: everything real happens in payout_apply, gated on not-yet-done.
execute unless entity @s[tag=hz_prices_done] run function cobblemon_initiative:sidequest/price_check/payout_apply
