# Stand Down at the Frostgate (Nifl SQ2) - paid-decline wrapper. Run AS the player from the decline
# dialog button. Wraps the compiler-generated route/decline_nifl_warrant_officer (pay-probe 140 CD,
# fail-soft: paid sets declined_nifl_warrant_officer + receipt; broke fires the opt-in DOUBLES battle).
# Per spec the paid decline is one of three paths that CLEAR the gate, so this wrapper sets
# nifl_frostgate_clear once the player is declined (the auto-gen fn cannot set an arbitrary tag).
# Synchronous: the tag set by the decline fn is observable in the same command chain.
function cobblemon_initiative:route/decline_nifl_warrant_officer
execute if entity @s[tag=declined_nifl_warrant_officer] run tag @s add nifl_frostgate_clear
