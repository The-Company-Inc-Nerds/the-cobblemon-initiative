# Town breadcrumb (14_shrines Q5) — Mystic Marsh points at the Fairy Shrine. Run AS the
# player from the town hub rumor button (gated not_tag defeated_fairy_shrine_leader).
# Read-only: no state written. Macro-safe: no apostrophes, no percent, no double-quotes
# in the delivered line. Matches Lila's rumor idiom (gray/aqua + book page-turn).
tellraw @s [{"text":"Quietly: ","color":"aqua"},{"text":"The marsh runs deeper than the gym. Below the water there is a light that does not warm - a shrine older than the town, and a priestess who turns away all but one tread. They say she has been waiting. It is south across the deep water, near where the ferry poles run.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9
