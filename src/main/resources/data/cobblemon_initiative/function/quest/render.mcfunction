# Derive the current objective from existing state (run as @s = player). No new quest state:
# reads memory_fragment (= gyms beaten), defeated_* tags, and fields_liberated.
# Priority ladder is evaluated low -> high so the highest-priority branch wins.
# Displayed rows ride q.* fake players — vanilla 1.21.1 HIDES '#'-prefixed holders from
# the sidebar (PlayerScoreEntry.isHidden), so '#' stays scratch-only (quest_hud). The
# sidebar caps at 15 rows; slot scores rank which lines survive when many are active.

# memory_fragment is only ever SET by badge grants (1..10); an unset score fails every
# `matches` test, which would kill the whole pre-badge ladder — define it as 0 first.
execute unless score @s memory_fragment matches 0.. run scoreboard players set @s memory_fragment 0

# Keep the main sidebar line present (top of list). (The old top "Objective" boss bar
# was removed — showrunner call, 2026-07-04; the sidebar main line carries the story.)
scoreboard players set q.main ci_quest 100

# Count cleared Board members (scratch holder in quest_hud).
scoreboard players set #board quest_hud 0
execute if entity @s[tag=defeated_board_madeline] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_matt] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_micah] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_lauren] run scoreboard players add #board quest_hud 1

# (0) GYM — next gym by badge count (memory_fragment 0..9).
execute if score @s memory_fragment matches ..9 run function cobblemon_initiative:quest/gym_town

# (0b) OPENING CHAIN — Sango Town, pre-badge only. Mom (mom_sent_to_lab) -> starter at the
#      lab (chose_starter) -> Pokedex (got_pokedex) -> Running Shoes (got_running_shoes).
#      Overrides the gym line until the shoes are on.
execute if score @s memory_fragment matches 0 unless entity @s[tag=mom_sent_to_lab] run scoreboard players display name q.main ci_quest [{"text":"▶ Talk to Mom","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=mom_sent_to_lab] unless entity @s[tag=chose_starter] run scoreboard players display name q.main ci_quest [{"text":"▶ Visit Professor Acacia at the lab","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=chose_starter] unless entity @s[tag=got_pokedex] run scoreboard players display name q.main ci_quest [{"text":"▶ Take the Pokedex from Acacia","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=got_pokedex] unless entity @s[tag=got_running_shoes] run scoreboard players display name q.main ci_quest [{"text":"▶ Show Mom your Pokedex","color":"yellow"}]

# (1) HQ RAID — after gym 7 AND 4 liberated fields (the hard gate: starve the monopoly
#     before storming it; DJ refuses the meeting while the fields still feed the Company),
#     until Acting CEO DJ falls (climax outranks gyms 8-10).
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] unless score @s fields_liberated matches 4.. run scoreboard players display name q.main ci_quest [{"text":"▶ Liberate wheat fields, then raid HQ","color":"gold"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] if score @s fields_liberated matches 4.. run scoreboard players display name q.main ci_quest [{"text":"▶ Raid Company HQ  [1590 51 1028]","color":"red"}]

# (2) ROYAL LEAGUE — all 10 badges, DJ down, champion still standing.
#     NOTE: the champion's defeat_tag override is royal_league_champion (NOT the
#     defeated_<id> default) — every consumer must use that latch.
execute if score @s memory_fragment matches 10 if entity @s[tag=defeated_villain_boss] unless entity @s[tag=royal_league_champion] run scoreboard players display name q.main ci_quest [{"text":"▶ Challenge the Royal League","color":"aqua"}]

# (3) THE BOARD — champion down, board not yet fully cleared.
execute if entity @s[tag=royal_league_champion] if score #board quest_hud matches ..3 unless entity @s[tag=defeated_villain_final_boss] run scoreboard players display name q.main ci_quest [{"text":"▶ Hunt the Board of Directors","color":"dark_purple"}]

# (3b) THE FOUNDER — board cleared, the mirror still waits at the top of HQ.
#      (defeated_villain_final_boss is granted by reveal/founder_defeated alongside the
#      canon company_overthrown flag.)
execute if entity @s[tag=royal_league_champion] if score #board quest_hud matches 4.. unless entity @s[tag=defeated_villain_final_boss] run scoreboard players display name q.main ci_quest [{"text":"▶ Face The Founder","color":"dark_red"}]

# (4) DONE — The Founder has fallen.
execute if entity @s[tag=defeated_villain_final_boss] run scoreboard players display name q.main ci_quest [{"text":"▶ Hunt the Ender Dragon","color":"dark_green"}]

# ---- Side objectives (light up as their systems come online) ----
# Opening chain as a tracked mission (slot 81, top of the side list): lights the moment
# Mom's errand is accepted and stages through the chain, on ANY world state — unlike the
# main line above, which only carries the chain pre-badge-1.
scoreboard players reset q.side_opening ci_quest
execute if entity @s[tag=mom_sent_to_lab,tag=!got_running_shoes] run scoreboard players set q.side_opening ci_quest 81
execute if entity @s[tag=mom_sent_to_lab,tag=!chose_starter] run scoreboard players display name q.side_opening ci_quest [{"text":"• Choose a partner at the Sango lab","color":"gray"}]
execute if entity @s[tag=chose_starter,tag=!got_pokedex] run scoreboard players display name q.side_opening ci_quest [{"text":"• Take the Pokedex from Acacia","color":"gray"}]
execute if entity @s[tag=got_pokedex,tag=!got_running_shoes] run scoreboard players display name q.side_opening ci_quest [{"text":"• Show Mom your Pokedex","color":"gray"}]

# Wheat War: shows the fields side line once the war is active AND the reveal has landed
# (heard_wheat_pitch — set by the Hua Zhan traders or the greenhouse catwalk; canon: the
# word never prints before the reveal).
scoreboard players reset q.side_wheat ci_quest
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] run scoreboard players set q.side_wheat ci_quest 80
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] store result storage cobblemon_initiative:quest fields int 1 run scoreboard players get @s fields_liberated
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] run function cobblemon_initiative:quest/set_wheat with storage cobblemon_initiative:quest

# ── beat-2 side objectives (appended by the quest build) ──
# Four Gardens Pilgrimage: Garden seals n/4 side line — lights at the first seal, clears on the blessing (pilgrimage_done latch). Counting + macro render live in sidequest/pilgrimage/hud (mirrors the q.side_wheat block above; q.side_pilgrim rides ci_quest 78, below wheat at 80).
function cobblemon_initiative:sidequest/pilgrimage/hud

# Price Check (Hua Zhan side quest): shows Price checks noted n/3 once Kaito hands out the check (hz_price_check_active), hides after turn-in (hz_prices_done). Scratch counter #prices quest_hud per the render ladder pattern; macro lives in sidequest/price_check/set_prices (quest/ dir is shared).
scoreboard players reset q.side_prices ci_quest
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run scoreboard players set #prices quest_hud 0
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_1] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_2] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_3] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run scoreboard players set q.side_prices ci_quest 74
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] store result storage cobblemon_initiative:quest prices int 1 run scoreboard players get #prices quest_hud
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run function cobblemon_initiative:sidequest/price_check/set_prices with storage cobblemon_initiative:quest

scoreboard players reset q.side_minutes ci_quest
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] run scoreboard players set q.side_minutes ci_quest 77
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] run scoreboard players display name q.side_minutes ci_quest [{"text":"• Deliver the minutes to Lucian in Sango","color":"gray"}]

# Verified Growth (reveal spine): tour pointer until the catwalk reveal lands.
scoreboard players reset q.side_green ci_quest
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players set q.side_green ci_quest 79
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players display name q.side_green ci_quest [{"text":"• Tour the Company greenhouse","color":"gray"}]

# Grain In, Goods Out (the Miller Walk): survey line while active.
scoreboard players reset q.side_survey ci_quest
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players set q.side_survey ci_quest 76
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players display name q.side_survey ci_quest [{"text":"• Survey the grain market for the miller","color":"gray"}]

# ── beat-3 side objectives: the rest of the board (slots 73..58, one per quest) ──
# Same contract as the blocks above: reset the holder unconditionally, then set + name it
# only while the quest is started-and-not-done. All conditions are read-only derivations
# from tags/scores the quests already latch — no new state.
# SKIPPED (no accept latch before completion): Performance Review (hidden gym-1 stealth
# meta; its only player latch, perf_review_resolved, IS the resolution — surfacing it would
# also spoil the ghost run), Out of Office (Genji rod: walk-up string turn-in, only latch is
# genji_rod_done), Work Orders INVENTORY + THE CUTTING (walk-up turn-ins, only latches are
# work_fetch_done / work_mine_done). Grain Survey already rides q.side_survey at 76 above.

# Cascade Ascent (Shou's board at the falls): live-run pointer only — free retries mean no
# accept latch, so the line rides the ci_ascending run tag until the first clear.
scoreboard players reset q.side_ascent ci_quest
execute if entity @s[tag=ci_ascending,tag=!sq_cascade_done] run scoreboard players set q.side_ascent ci_quest 73
execute if entity @s[tag=ci_ascending,tag=!sq_cascade_done] run scoreboard players display name q.side_ascent ci_quest [{"text":"• Base to crest before the clock dies","color":"gray"}]

# Sango Classic (Deka's record quarter): lights on the loaner rod (classic_rod_given) OR a
# live quarter (classic_active — own-rod entrants never take the spare); off at champion.
scoreboard players reset q.side_classic ci_quest
execute if entity @s[tag=classic_rod_given,tag=!sango_classic_champion] run scoreboard players set q.side_classic ci_quest 72
execute if entity @s[tag=classic_active,tag=!sango_classic_champion] run scoreboard players set q.side_classic ci_quest 72
execute if score q.side_classic ci_quest matches 72 run scoreboard players display name q.side_classic ci_quest [{"text":"• Five fish before the bar empties","color":"gray"}]

# Roadside Work Orders — NIGHT SHIFT (the one contract with an accept latch). Staged text:
# cull while on shift, collect once the quota latches; off when Tetsu pays.
scoreboard players reset q.side_shift ci_quest
execute if entity @s[tag=work_night_active,tag=!work_night_paid] run scoreboard players set q.side_shift ci_quest 71
execute if entity @s[tag=work_night_active,tag=!work_night_paid] run scoreboard players display name q.side_shift ci_quest [{"text":"• Cull eight on Blossom Path tonight","color":"gray"}]
execute if entity @s[tag=work_night_done,tag=!work_night_paid] run scoreboard players set q.side_shift ci_quest 71
execute if entity @s[tag=work_night_done,tag=!work_night_paid] run scoreboard players display name q.side_shift ci_quest [{"text":"• Collect night pay from Tetsu","color":"gray"}]

# Natural History (Kenji's museum): brush in hand (sq_museum_brush) until the donation
# case closes (sq_museum_donation_done).
scoreboard players reset q.side_bones ci_quest
execute if entity @s[tag=sq_museum_brush,tag=!sq_museum_donation_done] run scoreboard players set q.side_bones ci_quest 70
execute if entity @s[tag=sq_museum_brush,tag=!sq_museum_donation_done] run scoreboard players display name q.side_bones ci_quest [{"text":"• Six bones complete the exhibit","color":"gray"}]

# Per My Last Memo (Blossom Path checkpoint): eavesdrop leg from the approach warn
# (ckpt_warned), delivery leg once Memo 44-C is in hand (either path), off when Lucian files it.
scoreboard players reset q.side_memo ci_quest
execute if entity @s[tag=ckpt_warned,tag=!memo_heard] run scoreboard players set q.side_memo ci_quest 69
execute if entity @s[tag=ckpt_warned,tag=!memo_heard] run scoreboard players display name q.side_memo ci_quest [{"text":"• Ears on the checkpoint tent","color":"gray"}]
execute if entity @s[tag=memo_heard,tag=!memo_delivered] run scoreboard players set q.side_memo ci_quest 69
execute if entity @s[tag=memo_heard,tag=!memo_delivered] run scoreboard players display name q.side_memo ci_quest [{"text":"• Bring Memo 44-C to Lucian","color":"gray"}]

# No Such Recipient (Marlow's dead letter): carry leg, then the report-back leg — delivered
# or surrendered, both end at Marlow (marlow_thanks_done).
scoreboard players reset q.side_letter ci_quest
execute if entity @s[tag=carrying_dead_letter] run scoreboard players set q.side_letter ci_quest 68
execute if entity @s[tag=carrying_dead_letter] run scoreboard players display name q.side_letter ci_quest [{"text":"• Walk the dead letter to Lucian","color":"gray"}]
execute if entity @s[tag=letter_delivered,tag=!marlow_thanks_done] run scoreboard players set q.side_letter ci_quest 68
execute if entity @s[tag=letter_surrendered,tag=!marlow_thanks_done] run scoreboard players set q.side_letter ci_quest 68
execute unless entity @s[tag=carrying_dead_letter] if score q.side_letter ci_quest matches 68 run scoreboard players display name q.side_letter ci_quest [{"text":"• Tell Marlow how it ended","color":"gray"}]

# The Incomplete File (Lucian, run-long): active from open_file; goes dormant once the
# notices are filed and wakes for the post-HQ refile (mirrors the stage-3 dialog gate).
scoreboard players reset q.side_file ci_quest
execute if entity @s[tag=file_opened,tag=!notices_filed] run scoreboard players set q.side_file ci_quest 67
execute if entity @s[tag=file_opened,tag=!notices_filed] run scoreboard players display name q.side_file ci_quest [{"text":"• Rebuild the record for Lucian","color":"gray"}]
execute if entity @s[tag=notices_filed,tag=defeated_villain_boss,tag=!file_refiled] run scoreboard players set q.side_file ci_quest 67
execute if entity @s[tag=notices_filed,tag=defeated_villain_boss,tag=!file_refiled] run scoreboard players display name q.side_file ci_quest [{"text":"• The file can close — see Lucian","color":"gray"}]

# Off the Record (Lucian's quiet errands): from the first satchel to the debrief.
scoreboard players reset q.side_offrec ci_quest
execute if entity @s[tag=off_record_started,tag=!off_record_complete] run scoreboard players set q.side_offrec ci_quest 66
execute if entity @s[tag=off_record_started,tag=!off_record_complete] run scoreboard players display name q.side_offrec ci_quest [{"text":"• Quiet errands for Lucian","color":"gray"}]

# First Night Watch (Firstfurrow): the lantern note is open from liberation (farm_1_free —
# set by the night_watch tick bridge) until dawn breaks on a held watch (first_watch_done).
scoreboard players reset q.side_watch ci_quest
execute if entity @s[tag=farm_1_free,tag=!first_watch_done] run scoreboard players set q.side_watch ci_quest 65
execute if entity @s[tag=farm_1_free,tag=!first_watch_done] run scoreboard players display name q.side_watch ci_quest [{"text":"• Light the gate lantern at dusk","color":"gray"}]

# Quarterly Sprint (Courier Mio): live-run pointer only — free retries, no accept latch;
# rides ci_sprinting until the first bell (race_won).
scoreboard players reset q.side_sprint ci_quest
execute if entity @s[tag=ci_sprinting,tag=!race_won] run scoreboard players set q.side_sprint ci_quest 64
execute if entity @s[tag=ci_sprinting,tag=!race_won] run scoreboard players display name q.side_sprint ci_quest [{"text":"• Ring the bell at the Takehara arch","color":"gray"}]

# Greenspace 7, Under-Performing (Hua Zhan gym gate): draft leg from the approach note —
# eavesdrop, tray, or post-defeat all land yield_report_taken — then the Lucian filing leg.
scoreboard players reset q.side_audit ci_quest
execute if entity @s[tag=audit_warned,tag=!yield_report_taken] run scoreboard players set q.side_audit ci_quest 63
execute if entity @s[tag=audit_warned,tag=!yield_report_taken] run scoreboard players display name q.side_audit ci_quest [{"text":"• The gym gate audit — get the draft","color":"gray"}]
execute if entity @s[tag=yield_report_taken,tag=!scrub_report_filed] run scoreboard players set q.side_audit ci_quest 63
execute if entity @s[tag=yield_report_taken,tag=!scrub_report_filed] run scoreboard players display name q.side_audit ci_quest [{"text":"• File the yield report with Lucian","color":"gray"}]

# Sting Operation (Beekeeper Tomo): the seal walk, from the first confirm (sting_seal_1)
# to the payout (sting_reward_paid).
scoreboard players reset q.side_sting ci_quest
execute if entity @s[tag=sting_seal_1,tag=!sting_reward_paid] run scoreboard players set q.side_sting ci_quest 62
execute if entity @s[tag=sting_seal_1,tag=!sting_reward_paid] run scoreboard players display name q.side_sting ci_quest [{"text":"• Walk the four seals with Tomo","color":"gray"}]

# Notice of Non-Compliance (Mei's moth prints): from the glue objection (sq_posters_started)
# to the pasted-up payout (sq_posters_done).
scoreboard players reset q.side_posters ci_quest
execute if entity @s[tag=sq_posters_started,tag=!sq_posters_done] run scoreboard players set q.side_posters ci_quest 61
execute if entity @s[tag=sq_posters_started,tag=!sq_posters_done] run scoreboard players display name q.side_posters ci_quest [{"text":"• Paste three moth prints unseen","color":"gray"}]

# Head Count (Ume's census): from the accepted pitch (census_accepted) to the receipt
# (census_paid — the payee line still reads UNVERIFIED).
scoreboard players reset q.side_census ci_quest
execute if entity @s[tag=census_accepted,tag=!census_paid] run scoreboard players set q.side_census ci_quest 60
execute if entity @s[tag=census_accepted,tag=!census_paid] run scoreboard players display name q.side_census ci_quest [{"text":"• Log a verified capture for Ume","color":"gray"}]

# Tenants of Record (the Deng camp): from farm_1 liberation (farm_1_free bridge latch)
# until the homecoming pays out at Old Deng (homecoming_paid).
scoreboard players reset q.side_deng ci_quest
execute if entity @s[tag=farm_1_free,tag=!homecoming_paid] run scoreboard players set q.side_deng ci_quest 59
execute if entity @s[tag=farm_1_free,tag=!homecoming_paid] run scoreboard players display name q.side_deng ci_quest [{"text":"• Walk the Deng family home","color":"gray"}]

# Right of Way (Harvest Road survey detail): the ambush itself has no accept latch (sight-
# triggered by design); only the manifest leg is trackable — wagon paper in hand until Lucian pays.
scoreboard players reset q.side_manifest ci_quest
execute if entity @s[tag=took_route_manifest,tag=!manifest_paid] run scoreboard players set q.side_manifest ci_quest 58
execute if entity @s[tag=took_route_manifest,tag=!manifest_paid] run scoreboard players display name q.side_manifest ci_quest [{"text":"• Bring the route manifest to Lucian","color":"gray"}]
