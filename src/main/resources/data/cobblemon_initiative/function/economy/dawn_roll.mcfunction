# Dawn roll (review B6) — fired once per Minecraft day by economy/dawn. Every roll is
# announced (stream-visible); all variance is wares/flavor, never price or punishment
# (randomness invariants, ENGINE_FINDINGS §3).
#
# 1) EAST MARKET STREET WARE ROLL: #ware_day 1 = orchard day, 2 = hill day. Prices are
#    FLAT canon (unverified stalls never swing with cd_instability) — only WHICH wares
#    are stocked rotates: berry stand fine fruit (Leppa/Lum), the mint drawers
#    (forward/steady), the apricorn colors (warm/cool). One announce line, market voice.
execute store result score #ware_day hz_market run random value 1..2
execute if score #ware_day hz_market matches 1 run tellraw @a [{"text":"EAST MARKET STREET — ","color":"gold"},{"text":"orchard day: Leppa on the berry stand, the forward drawers open, warm colors on the cart.","color":"gray"}]
execute if score #ware_day hz_market matches 2 run tellraw @a [{"text":"EAST MARKET STREET — ","color":"gold"},{"text":"hill day: Lum on the berry stand, the steady drawers open, cool colors on the cart.","color":"gray"}]
#
# 2) COMPANY MORNING MEMO: ONE dawn bulletin line (hard cap — screen-noise rule),
#    drawn from the authored registers/economy.json pools by instability band:
#    idx ..15 = propaganda (stable, glossy) | 16..55 = reassurance (slipping, nervous)
#    | 56.. OR post-HQ = corrupted (exposed, glitching; the register calls it Post-HQ,
#    so the defeated_villain_boss tag forces the pool even after hq_stabilize claws
#    the index back down). Lines are baked here verbatim — registers are compile-time
#    source, functions are the runtime (band_tags precedent).
scoreboard players set #memo_pool cd_calc 1
execute if score #idx cd_instability matches 16.. run scoreboard players set #memo_pool cd_calc 2
execute if score #idx cd_instability matches 56.. run scoreboard players set #memo_pool cd_calc 3
execute if entity @a[tag=defeated_villain_boss] run scoreboard players set #memo_pool cd_calc 3
execute store result score #memo cd_calc run random value 1..3
execute if score #memo_pool cd_calc matches 1 if score #memo cd_calc matches 1 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"The Company, Inc. reminds you: verified trust, verified value.","color":"gray"}]
execute if score #memo_pool cd_calc matches 1 if score #memo cd_calc matches 2 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"Sleep easy. Every CobbleDollar is backed, audited, and watched over by people who care.","color":"gray"}]
execute if score #memo_pool cd_calc matches 1 if score #memo cd_calc matches 3 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"Ten years of stability and counting. The Company keeps the ledgers honest so you do not have to.","color":"gray"}]
execute if score #memo_pool cd_calc matches 2 if score #memo cd_calc matches 1 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"Prices are simply adjusting. That is normal. That is healthy. Please do not hoard.","color":"gray"}]
execute if score #memo_pool cd_calc matches 2 if score #memo cd_calc matches 2 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"The CobbleDollar is fine. The CobbleDollar has always been fine. Who told you otherwise.","color":"gray"}]
execute if score #memo_pool cd_calc matches 2 if score #memo cd_calc matches 3 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"A temporary recalibration of value. The Company is on top of it. The Company is always on top of it.","color":"gray"}]
execute if score #memo_pool cd_calc matches 3 if score #memo cd_calc matches 1 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"Verified tru§kvalue verified§ktrust we told them the founder retired.","color":"gray"}]
execute if score #memo_pool cd_calc matches 3 if score #memo cd_calc matches 2 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"There was never a founder. There was never anyone. Move along and trade your grain.","color":"gray"}]
execute if score #memo_pool cd_calc matches 3 if score #memo cd_calc matches 3 run tellraw @a [{"text":"COMPANY MORNING MEMO — ","color":"gold","bold":true},{"text":"The vaults were always empty. You only just noticed.","color":"gray"}]
