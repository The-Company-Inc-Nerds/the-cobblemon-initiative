# Acting CEO DJ takes the floor — penthouse battle staging (playtest 2026-08-05 P7-P9).
# Run AS the challenger: a bare as_player function call, the FIRST action on the battle
# button in acting_ceo_dj mono_3 — build_button lowers authored actions in order, and
# battle_actions appends attach + tbcs battle AFTER them, so both tps land before the
# battle opens. battle.stage_pos is consumed ONLY by the intro_scene engage path (DJ has
# no intro scene), so the challenger tp lives here, not in the compiler.
# DJ steps from his desk (P7 [1634.7 197 1106.5]) to the window line (P8) facing the
# challenger; the challenger is staged across the office at P9 facing him.
tp @e[type=easy_npc:humanoid,tag=ci_acting_dj,limit=1] 1633.5 197.0 1101.5 facing 1633.4 197.0 1111.5
tp @s 1633.4 197.0 1111.5 facing 1633.5 197.0 1101.5
execute at @s run playsound minecraft:block.beacon.deactivate player @s ~ ~ ~ 1 0.7
