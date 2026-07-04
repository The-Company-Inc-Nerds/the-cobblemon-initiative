# Right of Way — arming latch. Register in #minecraft:load (orchestrator wires the tag).
# rt2 #armed: 0 = survey detail not yet npcsight-registered, 1 = armed. The detail must
# only ambush AFTER badge 1 (sight pursue has no start-gate), so registration happens at
# runtime via the tick below instead of dialog/register_sight.
scoreboard objectives add rt2 dummy
execute unless score #armed rt2 matches -2147483648..2147483647 run scoreboard players set #armed rt2 0
