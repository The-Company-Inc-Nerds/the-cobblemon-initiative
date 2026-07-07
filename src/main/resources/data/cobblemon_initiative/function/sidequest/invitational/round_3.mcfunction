# Shorefront Invitational — the final falls (Harbourmaster). Run as the player via the
# bracket onwin (execute as @1 run …). Fires on title defenses too — the ladder resets
# weekly and a defended title deserves the same card.
title @s times 5 60 15
title @s subtitle [{"text":"A champion, pending verification","color":"gray"}]
title @s title [{"text":"BRACKET COMPLETE","color":"gold","bold":true}]
playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 1 1
