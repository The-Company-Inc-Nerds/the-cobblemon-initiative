# Town breadcrumb (14_shrines Q5) — Scorchspire points at the Fire Shrine. Run AS the
# player from the town hub rumor button (gated not_tag defeated_fire_shrine_leader).
# The Fire shrine is post-league: a champion gets the full breadcrumb; anyone earlier
# gets a not-yet teaser. Read-only, macro-safe: no apostrophes, no percent, no
# double-quotes in the delivered lines.
execute unless entity @s[tag=royal_league_champion] run tellraw @s [{"text":"Quietly: ","color":"aqua"},{"text":"Beyond the last flame there is a caldera the league does not test you against - a fire-shrine and a priest, Ignis, who only opens to a champion. Earn the crown at the Royal League first. Until then the ash-priests will not even look at you.","color":"gray"}]
execute if entity @s[tag=royal_league_champion] run tellraw @s [{"text":"Quietly: ","color":"aqua"},{"text":"You wear the crown, so the caldera will want its measure of you now. Beyond the last flame, up the shrine road, Ignis tends a fire older than the first ledger. Run his burning path before the heat runs you, then take his measure.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9
