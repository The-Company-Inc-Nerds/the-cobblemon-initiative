# One-shot chat beat on first approach to the checkpoint. Run as the player.
tag @s add ckpt_warned
tellraw @s [{"text":"A checkpoint ahead. ","color":"yellow"},{"text":"Verification is voluntary. Compliance is appreciated.","color":"gray","italic":true}]
