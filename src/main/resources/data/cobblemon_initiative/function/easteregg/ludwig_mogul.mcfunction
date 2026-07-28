# Ludwig the roof Jigglypuff — MOGUL MOVES, one time. Run as @s = the player from the
# mogul button (entry gated not_tag ludwig_mogul_done). Latch FIRST so nothing double-pays.
tag @s add ludwig_mogul_done
function cobblemon_initiative:economy/payout {amount:200}
playsound minecraft:block.note_block.bell player @s ~ ~ ~ 0.8 1.2
tellraw @s [{"text":"MOGUL MOVES — ","color":"gold","bold":true},{"text":"the roof executive approves a one-time discretionary disbursement. Do not ask which budget line.","color":"gray"}]
