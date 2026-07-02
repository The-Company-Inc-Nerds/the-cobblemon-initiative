# Liberate one occupied wheat field — the player's direct lever against the wheat monopoly.
# Call from a field-guard trainer's command reward (run as @s = the player):
#   execute as {player} run function cobblemon_initiative:liberation/free_field {field:"hua_zhan_1"}
# Idempotent per field via the field_freed latch (relog-safe world data): re-liberating does nothing.
$execute unless score $(field) field_freed matches 1 run function cobblemon_initiative:liberation/free_field_apply {field:"$(field)"}
