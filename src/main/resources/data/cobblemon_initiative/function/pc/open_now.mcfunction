# Trampoline scheduled 3t after pc/open: the nurse / Acacia dialog has closed, so open the
# player's Cobblemon PC now (on a clean screen). Single-player: @a[limit=1] is the player.
# `cobblemon-initiative pc` (CobblemonInitiativeCommands.openPc) links the PC + sends the
# client the OpenPCPacket.
execute as @a[limit=1] run cobblemon-initiative pc
