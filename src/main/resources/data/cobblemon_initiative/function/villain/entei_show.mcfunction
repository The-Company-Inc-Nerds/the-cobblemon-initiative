# The Entei reunion presentation — run as/at the player, once (tag cleared first).
# Corporate-memo framing on top, the real sentiment underneath; the player still does
# not know WHY the beast answers to them.
tag @s remove ci_entei_returned
title @s times 10 80 20
title @s subtitle {"text":"Original holder: verified.","color":"gray","italic":true}
title @s title {"text":"ASSET REASSIGNED","color":"gold","bold":true}
playsound cobblemon:pokemon.entei.cry master @s ~ ~ ~ 0.9 1.0
tellraw @s [{"text":"[The Company, Inc.] ","color":"gold","bold":true},{"text":"The shiny Entei falls in step beside you. It never stopped waiting.","color":"gray","italic":true}]
# The gift lands LAST (moved from entei_return, alpha.8): givemon opens the nickname
# prompt, and here the title/cry have already played — the naming ritual closes the
# beat instead of fighting it. @s is the player (entei_title re-targets via the tag);
# givemon resolves the player from the command source, so this works under execute-as.
# IVs roll naturally; the fight copy's perfect 31s are DJ's grooming, not a contract.
cobblemon-initiative givemon entei level=64 shiny=true nature=adamant
