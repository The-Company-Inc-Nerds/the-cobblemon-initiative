# PokéPhone deferred open (macro). Opens the caller's Easy NPC dialog at $(label) now that the
# body from phone/deliver has finished initialising. Single-player: @a[limit=1] is the player;
# `at @s` anchors sort=nearest so we grab the caller spawned at their feet. $(tag)=body tag.
$execute as @a[limit=1] at @s run easy_npc dialog open @e[type=easy_npc:humanoid,tag=$(tag),limit=1,sort=nearest] @s $(label)
