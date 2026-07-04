# Right of Way — post-badge-1 arming. Register in #minecraft:tick (orchestrator wires the
# tag). One-shot: the moment any player holds the Takehara badge, the survey detail's
# npcsight registrations fire once and the road ambush goes live. Cheap: a single score
# check per tick once armed.
execute if score #armed rt2 matches 0 if entity @a[tag=defeated_takehara_leader] run function cobblemon_initiative:sidequest/right_of_way/arm
