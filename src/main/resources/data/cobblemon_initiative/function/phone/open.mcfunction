# PokéPhone open (macro). Re-attempts the caller's Easy NPC dialog at $(label) until the CLIENT has
# tracked the freshly-spawned body (the open succeeds) or the small try budget is spent.
# ROOT CAUSE (bytecode + live log, Easy NPC 6.25.0): the server open is synchronous+correct; the
# break is client-side — the client rebuilds the menu from LivingEntityManager.npcEntityMap
# (populated on ClientEntityEvents ENTITY_LOAD). A per-call import_new body may not be tracked yet
# when the packet lands → DialogScreen.<init> (DialogScreen.java:89) does getEasyNPC().getLivingEntity()
# on a null entity → NPE → silent no-open. A fixed 5t/20t defer can lose that race; this retry can't.
# Budget kept SMALL on purpose: the caller spawns at the player's feet (always in range), so tracking
# lands in ~1-2t and the dialog opens on the first attempt; re-opening restarts the typewriter reveal,
# so we cap the tries to bound any restart to the call's opening instant. $(tag)=body tag, $(label)=label.
scoreboard players remove #phone_open_tries ci_dawn 1
$execute as @a[limit=1] at @s run easy_npc dialog open @e[type=easy_npc:humanoid,tag=$(tag),limit=1,sort=nearest] @s $(label)
$execute if entity @e[type=easy_npc:humanoid,tag=$(tag),limit=1] if score #phone_open_tries ci_dawn matches 1.. run schedule function cobblemon_initiative:phone/open_tick 2t
