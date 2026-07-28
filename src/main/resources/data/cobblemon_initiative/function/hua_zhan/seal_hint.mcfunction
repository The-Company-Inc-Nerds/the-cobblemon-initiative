# Four Wardens pilgrimage — post-battle seal reminder. Run AS the winning player from
# each statue's battle on_win (execute as @1 run …, the hz_analyst on_win precedent).
# The alpha.25 playtest showed players walking away after the win without knowing the
# seal needs a SECOND interaction with the statue (the "seal" dialog entry, gated on
# defeat). One actionbar line + chime closes that gap without touching the seal gating.
title @s actionbar [{"text":"The warden yields. ","color":"green"},{"text":"Speak to the statue once more to press its seal.","color":"yellow"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.8 1.4
