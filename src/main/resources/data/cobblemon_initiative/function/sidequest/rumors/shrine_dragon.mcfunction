# Town breadcrumb (14_shrines Q5) — Ryujin Keep points at the Dragon Shrine. Run AS the
# player from the town hub rumor button (gated not_tag defeated_dragon_shrine_leader).
# Carries the Doubles warning in-line: Draconis and the hydra fight in pairs. Read-only,
# macro-safe: no apostrophes, no percent, no double-quotes in the delivered line.
tellraw @s [{"text":"Quietly: ","color":"aqua"},{"text":"Past the keep, the oldest fire still coils in a shrine no map marks. A hydra guards it, and a High Priest who says the wyrm remembers a tread from before the towns. Bring hardened scales, and a party built for two-on-two - the keeper fights in pairs, and so does the hydra.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9
