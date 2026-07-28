# Faelara Moonwhisper — "show me the glow and the shadow" (a21 wave, Bryn-pattern
# flavor errand, deliberately NO register slot). partyhas tags synchronously, so this
# same function branches on the probe tags right after the checks. A single dual-type
# (Morgrem/Impidimp) satisfies BOTH probes at once — ACCEPTED, on-theme (her own Hex
# is the glow and the shadow in one skin). One-shot via faelara_pair_done; replays
# route to the done dialog band.
tag @s remove fae_fairy_ok
tag @s remove fae_dark_ok
execute unless entity @s[tag=faelara_pair_done] run cobblemon-initiative partyhas fairy fae_fairy_ok
execute unless entity @s[tag=faelara_pair_done] run cobblemon-initiative partyhas dark fae_dark_ok
execute if entity @s[tag=fae_fairy_ok] if entity @s[tag=fae_dark_ok] run function cobblemon_initiative:sidequest/faelara/show_pair_success
execute unless entity @s[tag=faelara_pair_done] unless entity @s[tag=fae_fairy_ok] run title @s actionbar [{"text":"Faelara shakes her head. ","color":"gray"},{"text":"No glow among them yet.","color":"light_purple"}]
execute unless entity @s[tag=faelara_pair_done] if entity @s[tag=fae_fairy_ok] unless entity @s[tag=fae_dark_ok] run title @s actionbar [{"text":"Faelara shakes her head. ","color":"gray"},{"text":"The glow is there. The shadow is not.","color":"light_purple"}]
tag @s remove fae_fairy_ok
tag @s remove fae_dark_ok
