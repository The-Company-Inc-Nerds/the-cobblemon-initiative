# Fight path: both checkpoint agents beaten (they despawn on loss). Run as the player.
# One-time: the memo_heard tag gates the caller.
loot give @s loot cobblemon_initiative:npc_gift/memo_44c
tag @s add memo_heard
tag @s remove memo_loiter
tellraw @s [{"text":"You go through the abandoned checkpoint paperwork and find ","color":"gray"},{"text":"Memo 44-C","color":"gold"},{"text":". Lucian the archivist will want this.","color":"gray"}]
