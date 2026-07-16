# Town breadcrumb (14_shrines Q5) — Nifl Town points at the Ice Shrine. Run AS the player
# from the town hub rumor button (gated not_tag defeated_ice_shrine_leader). Read-only,
# macro-safe: no apostrophes, no percent, no double-quotes in the delivered line.
tellraw @s [{"text":"Quietly: ","color":"aqua"},{"text":"West of Nifl the ice runs a path that cracks under the unworthy and throws them back to the start. Glacius keeps it. He does not move, they say, not once in a lifetime of watching the door. Cross the frozen path before the frost takes you, and he will hear you out.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9
