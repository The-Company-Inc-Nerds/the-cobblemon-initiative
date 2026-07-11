# Field-liberation latches. Run once on datapack load (wired via minecraft:load tag).
# field_freed: per-field one-way latch (holder = field id, e.g. "hua_zhan_1"). Set to 1 the first
#   time a field is liberated so free_field is idempotent. Add fails silently if it already exists.
scoreboard objectives add field_freed dummy
# Ceremony name map: field id -> the display name the LIBERATED title card prints
# (free_field_apply resolves names.<field>; a missing entry falls back to THE PARCEL).
# WIRING RULE: every new field wired into liberation/free_field MUST add its dispatch-board
# name here (hz_greenhouse_archivist reads the canon list). All 10 farms (install.json FARM
# zones, gated field_freed/farm_1..farm_10) are seeded. The wheat-war goal is ANY 6 of the 10.
data merge storage cobblemon_initiative:liberation {names:{farm_1:"FIRSTFURROW",farm_2:"MIREBLOOM PADDIES",farm_3:"WESTWIND FIELDS",farm_4:"DRYROW STEADING",farm_5:"CROSSROADS GRANARY",farm_6:"FENCELINE ACRES",farm_7:"COLDFURROW FARM",farm_8:"FROSTFALLOW FARM",farm_9:"HIGHFIELD ESTATE",farm_10:"ASHLOAM FIELDS"}}
