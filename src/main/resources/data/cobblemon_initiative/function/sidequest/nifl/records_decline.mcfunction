# Cold Storage (Nifl SQ1) - paid-decline wrapper. Run AS the player from the decline dialog button.
# Wraps the compiler-generated route/decline_nifl_records_officer (pay-probe 120 CD, fail-soft: paid
# sets declined_nifl_records_officer + receipt; broke fires the opt-in battle). Per spec the paid
# decline must ALSO open the archive gate so the portrait is a real skip, not a dead end - the
# auto-gen fn cannot set an arbitrary tag, so this wrapper adds nifl_archive_open once the player is
# declined. Synchronous: the tag set by the decline fn is observable in the same command chain.
function cobblemon_initiative:route/decline_nifl_records_officer
execute if entity @s[tag=declined_nifl_records_officer] run tag @s add nifl_archive_open
