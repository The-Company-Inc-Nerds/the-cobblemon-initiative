# Ambient-life / NPC placement latches. The per-NPC zero-inits are GENERATED from
# character `placement` fields -> ambient/placements_init (scripts/content_compile).
function cobblemon_initiative:ambient/placements_init
# HAND-AUTHORED latch (not a `placement` field, so the generator omits it): Victor's PATH
# latch, armed by sango/victor_descend when he leaves the tower. Zero-init here so ARRIVE's
# `matches 0` gate is robust independent of descend ordering; init-if-unset never clobbers a
# consumed (=1) latch on reload.
execute unless score #amb_victor_path ci_ambient matches 0.. run scoreboard players set #amb_victor_path ci_ambient 0
