# PokéPhone deferred open — trampoline scheduled 5t after phone/deliver spawns the caller
# (a scheduled function cannot carry `with storage`, so this thin wrapper re-attaches the
# stashed {tag,label} and calls the macro that actually opens the call dialog).
function cobblemon_initiative:phone/open with storage cobblemon_initiative:phone open
