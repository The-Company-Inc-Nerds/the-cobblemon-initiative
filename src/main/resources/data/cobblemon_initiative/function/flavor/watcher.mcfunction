# The shadow-self watcher — fired ONCE, the first time the player is truly on the road (after
# the first badge, Takehara Falls). Registered in #minecraft:tick. A bare tag filter is cheap and
# the one-shot watcher_seen_1 tag makes the inner call dead forever after the first play — so this
# is effectively free once seen. The scene names nothing (Rule 7); the player just glimpses a copy
# of themselves watching, then it is gone. Independent of the phone toggle.
execute as @a[tag=defeated_takehara_leader] unless entity @s[tag=watcher_seen_1] run function cobblemon_initiative:flavor/watcher_fire
