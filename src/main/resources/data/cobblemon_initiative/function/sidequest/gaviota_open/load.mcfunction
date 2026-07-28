# Gaviota Open (alpha.26 audit rulings) — objectives + bossbar setup. The salt-water
# derby the port quartet always promised (Enzo/Gianna/Rosa/Marco champion entries gate
# on gaviota_open_champion — this tree finally grants it). Clone of the proven Sango
# Classic pattern (sidequest/derby/), collapsed from the roadmap 3-round design into
# the record round Marlin's dialog canonises: TEN proofs of catch in 300 seconds.
# SALT SPECIES (jar-verified 1.7.3 spawn_pool_world, all common in #cobblemon:is_ocean;
# chewtle REJECTED — freshwater-only pools):
#   finneon  -> proof item minecraft:cod       (guaranteed species drop)
#   magikarp -> proof item minecraft:salmon    (guaranteed species drop; the common)
#   qwilfish -> proof item minecraft:pufferfish (guaranteed species drop)
#   tentacool-> proof item minecraft:ink_sac   (guaranteed species drop; also vanilla junk loot)
# The scale counts the PROOF ITEMS (store-result clear, entry-snapshot anti-exploit) —
# mechanically true to Cobblemon: KO the wild salt shoals for drops, or rod the harbour
# (vanilla ocean fishing yields the same items). Both play styles are legal at the scale.
# Registered in data/minecraft/tags/function/load.json (a26 ruling registers this clone
# directly — unlike derby/load, which predates that call). No tick entry needed: the
# countdown rides the schedule loop (second.mcfunction), Sango cadence.
# Idempotent and relog-safe: objectives add no-ops when present, #init guards the bossbar,
# and a round that was live at shutdown re-arms its schedule loop here.
scoreboard objectives add ci_open dummy
scoreboard objectives add ci_open_cod dummy
scoreboard objectives add ci_open_salmon dummy
scoreboard objectives add ci_open_puffer dummy
scoreboard objectives add ci_open_ink dummy
scoreboard objectives add ci_open_total dummy
scoreboard objectives add ci_open_rem dummy
scoreboard objectives add ci_open_take dummy
scoreboard objectives add ci_open_base_cod dummy
scoreboard objectives add ci_open_base_salmon dummy
scoreboard objectives add ci_open_base_puffer dummy
scoreboard objectives add ci_open_base_ink dummy
scoreboard objectives add ci_open_win dummy
scoreboard objectives add ci_open_bonus dummy
execute unless score #init ci_open matches 1 run bossbar add cobblemon_initiative:gaviota_open [{"text":"THE GAVIOTA OPEN","color":"aqua","bold":true}]
scoreboard players set #init ci_open 1
bossbar set cobblemon_initiative:gaviota_open color blue
bossbar set cobblemon_initiative:gaviota_open style notched_10
bossbar set cobblemon_initiative:gaviota_open max 300
execute unless score #on ci_open matches -2147483648..2147483647 run scoreboard players set #on ci_open 0
# Resume the countdown if a round was live when the server stopped.
execute if score #on ci_open matches 1 run schedule function cobblemon_initiative:sidequest/gaviota_open/second 1s
