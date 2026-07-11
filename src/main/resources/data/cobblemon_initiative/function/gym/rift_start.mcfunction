# Opens the Ryujin rift-dragon fight. Called from Leader Ryujin's rift dialog button
# (`function` is on the Easy NPC ExecAsUser allowlist; bare function calls are
# execute-as wrapped by the compiler, so @s here = the challenger, and the function
# source carries the player entity that /riftdragon start binds to).
#
# ORDER MATTERS: summon the dragon FIRST, then play the reveal cutscene — a skipped
# cutscene can never lose the fight, and the camera catches the dragon already
# emerging from the tear. The cutscene deliberately has no ambientWeather: the rift
# manager owns the storm sky and restores it on its own teardown.
riftdragon start
cutscene play rift_intro
