# Right of Way — one-shot npcsight registration for the survey detail (runs as server).
# FILL THE UUIDS AT PLACEMENT: replace both %%...%% with the real entity uuids, then this
# file arms the detail the moment badge 1 lands (sequencing: polite desk -> open road ->
# occupied farm). Until the uuids are filled the commands fail harmlessly and the latch
# still sets (the detail simply stays passive - re-run manually after filling).
scoreboard players set #armed rt2 1
# Corridor Assessor - dialog hail, range 10
npcsight add %%ROUTE_SURVEYOR_UUID%% 10 corridor_hail
npcsight mode %%ROUTE_SURVEYOR_UUID%% dialog
# Logistics Escort - pursue spotter, range 8
npcsight add %%ROUTE_ESCORT_UUID%% 8
npcsight mode %%ROUTE_ESCORT_UUID%% pursue
npcsight stoptag %%ROUTE_ESCORT_UUID%% defeated_villain_route_escort
tellraw @a[tag=defeated_takehara_leader] [{"text":"Somewhere on Harvest Road, a survey detail unfolds a wagon of paperwork.","color":"gray","italic":true}]
