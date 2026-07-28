# Orc encampments (pins P2-P9) — load-time latch init. Eight raider camps on the
# wilderness ridges between Deepcore and the eastern passes, built on the night_watch
# vanilla-mob pattern (armored vindicator/husk summons, NOT Easy NPC melee bodies — the
# duel_melee goal set is unverified on orc navs and is a known crash class).
# Latch semantics per camp on ci_ambient (placement-latch idiom, #-holders hidden from
# the sidebar): 0 = armed (camp not yet raised), 1 = live (mobs summoned, absence poll
# running), 2 = cleared forever (one-shot ceremony + spoils fired). Objective add is
# idempotent (placements_init does the same); init-if-unset never clobbers a live/cleared
# camp on relog — same shape as the hand-authored Victor path latch in ambient/init.
scoreboard objectives add ci_ambient dummy
execute unless score #orc_camp_p2 ci_ambient matches 0.. run scoreboard players set #orc_camp_p2 ci_ambient 0
execute unless score #orc_camp_p3 ci_ambient matches 0.. run scoreboard players set #orc_camp_p3 ci_ambient 0
execute unless score #orc_camp_p4 ci_ambient matches 0.. run scoreboard players set #orc_camp_p4 ci_ambient 0
execute unless score #orc_camp_p5 ci_ambient matches 0.. run scoreboard players set #orc_camp_p5 ci_ambient 0
execute unless score #orc_camp_p6 ci_ambient matches 0.. run scoreboard players set #orc_camp_p6 ci_ambient 0
execute unless score #orc_camp_p7 ci_ambient matches 0.. run scoreboard players set #orc_camp_p7 ci_ambient 0
execute unless score #orc_camp_p8 ci_ambient matches 0.. run scoreboard players set #orc_camp_p8 ci_ambient 0
execute unless score #orc_camp_p9 ci_ambient matches 0.. run scoreboard players set #orc_camp_p9 ci_ambient 0
