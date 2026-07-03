# The Incomplete File — Lucian opens a Personnel File on the nameless stranger and issues
# the run-long errand. Called by the sq_personnel_file open_file button (run as the player).
# The book is the run death ledger the streamer hand-writes; poke balls are Standard
# Registration Equipment; 300 CD is a records-fee refund the player never paid (the ledger
# must balance). One-shot; gated on not_tag file_opened.
give @s minecraft:writable_book[minecraft:custom_name='{"color":"gold","italic":false,"text":"Personnel File — FOUND"}',minecraft:lore=['{"color":"dark_gray","italic":true,"text":"Name: [pending]. History: [pending]."}','{"color":"dark_gray","italic":true,"text":"Section: Partners, Deceased. One page per loss. Remove none."}']] 1
give @s cobblemon:poke_ball 3
tag @s add file_opened
function cobblemon_initiative:economy/payout {amount:300}
