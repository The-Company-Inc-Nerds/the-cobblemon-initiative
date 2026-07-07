# Field-liberation latches. Run once on datapack load (wired via minecraft:load tag).
# field_freed: per-field one-way latch (holder = field id, e.g. "hua_zhan_1"). Set to 1 the first
#   time a field is liberated so free_field is idempotent. Add fails silently if it already exists.
scoreboard objectives add field_freed dummy
# Ceremony name map: field id -> the display name the LIBERATED title card prints
# (free_field_apply resolves names.<field>; a missing entry falls back to THE PARCEL).
# WIRING RULE: every new field wired into liberation/free_field MUST add its dispatch-board
# name here (Firstfurrow / Fenceline Acres / Mirebloom Paddies / Westwind Fields / Dryrow
# Steading / Crossroads Granary / … — hz_greenhouse_archivist reads the canon list).
data merge storage cobblemon_initiative:liberation {names:{farm_1:"FIRSTFURROW"}}
