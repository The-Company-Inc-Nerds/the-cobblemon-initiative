# Wager purse sweetener (review B6, 2026-07-06). Run AS the winning player from a
# wager's onwin list (`execute as @1 run function …` — @1 = winner-side player,
# ENGINE_FINDINGS §2 TBCS token order). DESIGN CALL, noted per the Tier-B spec: the
# review floated ROLLED STAKES, but stakes/decline fees are committed amounts — the
# randomness invariants (ENGINE_FINDINGS §3) forbid rolling them, and the 13b bow-out
# fees are compile-baked literals. So the STAKES STAY FIXED and only the WIN side
# rolls: +25..100 CD on top of the advertised purse. Bonus-only, always announced.
# Flat CD on purpose (battle prizes stay flat in onwin — §3): the base purse and this
# rider are one prize. All four wager NPCs are one-time (defeat-tag gated) — no loop.
execute store result score #sweet cd_calc run random value 1..4
execute if score #sweet cd_calc matches 1 run cobbledollars give @s 25
execute if score #sweet cd_calc matches 1 run tellraw @s [{"text":"The purse runs heavy — ","color":"gray"},{"text":"+25 CD","color":"gold"},{"text":" over the posted terms.","color":"gray"}]
execute if score #sweet cd_calc matches 2 run cobbledollars give @s 50
execute if score #sweet cd_calc matches 2 run tellraw @s [{"text":"The purse runs heavy — ","color":"gray"},{"text":"+50 CD","color":"gold"},{"text":" over the posted terms.","color":"gray"}]
execute if score #sweet cd_calc matches 3 run cobbledollars give @s 75
execute if score #sweet cd_calc matches 3 run tellraw @s [{"text":"The purse runs heavy — ","color":"gray"},{"text":"+75 CD","color":"gold"},{"text":" over the posted terms.","color":"gray"}]
execute if score #sweet cd_calc matches 4 run cobbledollars give @s 100
execute if score #sweet cd_calc matches 4 run tellraw @s [{"text":"The purse runs heavy — ","color":"gray"},{"text":"+100 CD","color":"gold"},{"text":" over the posted terms.","color":"gray"}]
