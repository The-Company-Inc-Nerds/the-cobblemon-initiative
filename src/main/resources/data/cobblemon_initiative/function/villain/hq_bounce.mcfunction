# Company HQ ground floor — ESCORTED OUT. Run as/at a lobby-box player who is not
# raid-eligible (villain/hq_tick a). Cooldown FIRST (AutoInstall rule: latch before the
# effect — a crash mid-escort must not re-fire every tick), then the card, then the walk:
# tp to the street side of the door line, facing the guards who just declined to know you.
scoreboard players set @s ci_hq_kick_cd 200
tellraw @s [{"text":"Two hands in pressed suits close on your shoulders. ","color":"gray","italic":true},{"text":"You are walked, politely and without appeal, back out the door you came in.","color":"gray"}]
title @s actionbar [{"text":"ESCORTED OUT","color":"red","bold":true},{"text":" — the Company thanks you for your interest.","color":"gray"}]
playsound minecraft:entity.villager.no player @s ~ ~ ~ 1 0.8
tp @s 1619.5 92 1120.5 facing 1619.5 92 1116.5
execute at @s run playsound minecraft:block.wooden_door.close player @s ~ ~ ~ 1 0.9
