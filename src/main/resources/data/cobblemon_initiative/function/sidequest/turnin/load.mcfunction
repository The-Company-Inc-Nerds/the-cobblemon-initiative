# Scratch objective for server-side item-count hand-ins (turnin/*). Easy NPC's has_item
# dialog condition (HAS_ITEM_IN_INVENTORY) does NOT gate in this build, so the hand-in
# buttons validate the count here instead: `clear @s <item> 0` is a dry run that returns
# the count WITHOUT removing anything; only a sufficient count clears + rewards.
scoreboard objectives add ci_item dummy
