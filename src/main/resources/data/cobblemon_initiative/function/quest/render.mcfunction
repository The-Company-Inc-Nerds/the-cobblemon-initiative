# Derive the current objective from existing state (run as @s = player). No new quest state:
# reads memory_fragment (= gyms beaten), defeated_* tags, and fields_liberated.
# Priority ladder is evaluated low -> high so the highest-priority branch wins.
# Displayed rows ride q.* fake players — vanilla 1.21.1 HIDES '#'-prefixed holders from
# the sidebar (PlayerScoreEntry.isHidden), so '#' stays scratch-only (quest_hud). The
# sidebar caps at 15 rows; slot scores rank which lines survive when many are active.

# memory_fragment is only ever SET by badge grants (1..10); an unset score fails every
# `matches` test, which would kill the whole pre-badge ladder — define it as 0 first.
execute unless score @s memory_fragment matches 0.. run scoreboard players set @s memory_fragment 0
# ci_papers_held is recomputed every tick by sidequest/personnel_file/papers_tick; zero-init
# here too so the filing-day suppression tests below never fail on a fresh player.
execute unless score @s ci_papers_held matches 0.. run scoreboard players set @s ci_papers_held 0
# fields_liberated is only ever SET by liberation/free_field_apply; an unset score makes
# the Java quest-tracker's score check FAIL, mis-resolving the tracked main waypoint at the
# Act-2 pivot (it fell through to the Ryujin gym while the sidebar said "Liberate wheat
# fields"). Zero-init so the HQ-raid stages evaluate correctly for a player with 0 fields.
execute unless score @s fields_liberated matches 0.. run scoreboard players set @s fields_liberated 0

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

# (1) HQ RAID — after gym 7 AND 6 of the 10 liberated fields (the hard gate, showrunner
#     ruling 2026-07-13: starve the monopoly before storming it; DJ refuses the meeting
#     while the fields still feed the Company), until Acting CEO DJ falls (climax outranks
#     gyms 8-10). The raid DESCENDS to DJ at the basement bottom [1590 51 1028].
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] unless score @s fields_liberated matches 6.. run scoreboard players display name q.main ci_quest [{"text":"▶ Liberate wheat fields, then raid HQ","color":"gold"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] if score @s fields_liberated matches 6.. run scoreboard players display name q.main ci_quest [{"text":"▶ Raid Company HQ  [1590 51 1028]","color":"red"}]

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
execute if entity @s[tag=defeated_villain_final_boss] run scoreboard players display name q.main ci_quest [{"text":"▶ Beyond the map — the world is yours","color":"dark_green"}]

# ---- Side objectives (light up as their systems come online) ----
# Opening chain as a tracked mission (slot 81, top of the side list): lights the moment
# Mom's errand is accepted and stages through the chain, on ANY world state — unlike the
# main line above, which only carries the chain pre-badge-1.
scoreboard players reset q.side_opening ci_quest
execute if entity @s[tag=mom_sent_to_lab,tag=!got_running_shoes] run scoreboard players set q.side_opening ci_quest 81
execute if entity @s[tag=mom_sent_to_lab,tag=!chose_starter] run scoreboard players display name q.side_opening ci_quest [{"text":"• Choose a partner at the Sango lab","color":"gray"}]
execute if entity @s[tag=chose_starter,tag=!got_pokedex] run scoreboard players display name q.side_opening ci_quest [{"text":"• Take the Pokedex from Acacia","color":"gray"}]
execute if entity @s[tag=got_pokedex,tag=!got_running_shoes] run scoreboard players display name q.side_opening ci_quest [{"text":"• Show Mom your Pokedex","color":"gray"}]

# Dex-unlock partners: fill the Pokedex (15 / 30 CAUGHT) then talk to Prof. Acacia to
# unlock the next starter, then claim it from the lab stand-in. dex_gte_15/30 are band tags.
scoreboard players reset q.side_partner ci_quest
execute if entity @s[tag=chose_starter,tag=dex_gte_15,tag=!second_starter_claimed] run scoreboard players set q.side_partner ci_quest 79
execute if entity @s[tag=chose_starter,tag=dex_gte_15,tag=!second_starter_unlocked] run scoreboard players display name q.side_partner ci_quest [{"text":"• A second partner awaits — see Prof. Acacia","color":"gray"}]
execute if entity @s[tag=second_starter_unlocked,tag=!second_starter_claimed] run scoreboard players display name q.side_partner ci_quest [{"text":"• Claim your second partner at the lab","color":"gray"}]
execute if entity @s[tag=second_starter_claimed,tag=dex_gte_30,tag=!third_starter_claimed] run scoreboard players set q.side_partner ci_quest 79
execute if entity @s[tag=second_starter_claimed,tag=dex_gte_30,tag=!third_starter_unlocked] run scoreboard players display name q.side_partner ci_quest [{"text":"• A third partner awaits — see Prof. Acacia","color":"gray"}]
execute if entity @s[tag=third_starter_unlocked,tag=!third_starter_claimed] run scoreboard players display name q.side_partner ci_quest [{"text":"• Claim your third partner at the lab","color":"gray"}]

# Wheat War: shows the fields side line once the war is active AND the reveal has landed
# (heard_wheat_pitch — set by the Hua Zhan traders or the greenhouse catwalk; canon: the
# word never prints before the reveal).
scoreboard players reset q.side_wheat ci_quest
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] run scoreboard players set q.side_wheat ci_quest 80
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] store result storage cobblemon_initiative:quest fields int 1 run scoreboard players get @s fields_liberated
# 10 farms exist but the goal is any 6 — clamp the HUD so it never overflows past 6/6.
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] if score @s fields_liberated matches 6.. run data modify storage cobblemon_initiative:quest fields set value 6
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
# Discovery stage: warned but not yet heard — points at the top-floor door (round 13).
execute if entity @s[tag=hz_office_warned,tag=!hz_minutes_heard] run scoreboard players set q.side_minutes ci_quest 77
execute if entity @s[tag=hz_office_warned,tag=!hz_minutes_heard] run scoreboard players display name q.side_minutes ci_quest [{"text":"• Ears on the top-floor door","color":"gray"}]
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] if score @s ci_papers_held matches ..1 run scoreboard players set q.side_minutes ci_quest 77
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] if score @s ci_papers_held matches ..1 run scoreboard players display name q.side_minutes ci_quest [{"text":"• Deliver the minutes to Lucian in Sango","color":"gray"}]

# Verified Growth (reveal spine): tour pointer until the catwalk reveal lands.
scoreboard players reset q.side_green ci_quest
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players set q.side_green ci_quest 79
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players display name q.side_green ci_quest [{"text":"• Tour the Verified Growth greenhouse","color":"gray"}]

# Grain In, Goods Out (the Miller Walk): survey line while active; flips to the report
# leg once both survey halves are logged (hz_saw_pitch + hz_saw_granary — the grain_survey
# tick loggers, `Noted (n/3)` pattern).
scoreboard players reset q.side_survey ci_quest
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players set q.side_survey ci_quest 76
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players display name q.side_survey ci_quest [{"text":"• Survey the grain market for the miller","color":"gray"}]
execute if entity @s[tag=hz_survey_active,tag=hz_saw_pitch,tag=hz_saw_granary,tag=!hz_survey_paid] run scoreboard players display name q.side_survey ci_quest [{"text":"• Report the survey to Guo the Miller","color":"gray"}]

# ── beat-3 side objectives: the rest of the board (slots 73..58, one per quest) ──
# Same contract as the blocks above: reset the holder unconditionally, then set + name it
# only while the quest is started-and-not-done. All conditions are read-only derivations
# from tags/scores the quests already latch — no new state.
# SKIPPED (no accept latch before completion): Out of Office (Genji rod: walk-up string
# turn-in, only latch is genji_rod_done), Work Orders INVENTORY + THE CUTTING (walk-up
# turn-ins, only latches are work_fetch_done / work_mine_done). Grain Survey already rides
# q.side_survey at 76 above.  (Performance Review gym-1 stealth meta REMOVED 2026-07-07.)

# Cascade Ascent (Shou's board at the falls): live-run pointer only — free retries mean no
# accept latch, so the line rides the ci_ascending run tag until the first clear.
scoreboard players reset q.side_ascent ci_quest
execute if entity @s[tag=ci_ascending,tag=!sq_cascade_done] run scoreboard players set q.side_ascent ci_quest 73
execute if entity @s[tag=ci_ascending,tag=!sq_cascade_done] run scoreboard players display name q.side_ascent ci_quest [{"text":"• Base to crest before the clock dies","color":"gray"}]

# Sango Classic (Deka's record quarter): lights only while a quarter is LIVE (classic_active);
# off at champion. NOT on classic_rod_given — that latch is permanent, so lighting on it made
# the derby read as "started" forever after taking the loaner rod (showrunner 2026-07-07).
scoreboard players reset q.side_classic ci_quest
execute if entity @s[tag=classic_active,tag=!sango_classic_champion] run scoreboard players set q.side_classic ci_quest 72
execute if score q.side_classic ci_quest matches 72 run scoreboard players display name q.side_classic ci_quest [{"text":"• Three fish before the bar empties","color":"gray"}]

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
execute if entity @s[tag=memo_heard,tag=!memo_delivered] if score @s ci_papers_held matches ..1 run scoreboard players set q.side_memo ci_quest 69
execute if entity @s[tag=memo_heard,tag=!memo_delivered] if score @s ci_papers_held matches ..1 run scoreboard players display name q.side_memo ci_quest [{"text":"• Bring Memo 44-C to Lucian","color":"gray"}]

# No Such Recipient (Marlow's dead letter): carry leg, then the report-back leg — delivered
# or surrendered, both end at Marlow (marlow_thanks_done).
scoreboard players reset q.side_letter ci_quest
execute if entity @s[tag=carrying_dead_letter] if score @s ci_papers_held matches ..1 run scoreboard players set q.side_letter ci_quest 68
execute if entity @s[tag=carrying_dead_letter] if score @s ci_papers_held matches ..1 run scoreboard players display name q.side_letter ci_quest [{"text":"• Walk the dead letter to Lucian","color":"gray"}]
execute if entity @s[tag=letter_delivered,tag=!marlow_thanks_done] run scoreboard players set q.side_letter ci_quest 68
execute if entity @s[tag=letter_surrendered,tag=!marlow_thanks_done] run scoreboard players set q.side_letter ci_quest 68
execute unless entity @s[tag=carrying_dead_letter] if score q.side_letter ci_quest matches 68 run scoreboard players display name q.side_letter ci_quest [{"text":"• Tell Marlow how it ended","color":"gray"}]

# The Incomplete File (Lucian, run-long): active from open_file; goes dormant once the
# notices are filed and wakes for the post-HQ refile (mirrors the stage-3 dialog gate).
# A seller (sold_docs) killed the quest at the courier cart — the rebuild line is gated
# off and replaced by a permanent dark-red tombstone (the record belongs to the Company).
# During the badge-1-to-3 dark window (docs filed, notices not yet briefed) the line
# reads as a wait state instead of a stale errand.
scoreboard players reset q.side_file ci_quest
execute if entity @s[tag=file_opened,tag=!notices_filed,tag=!sold_docs] run scoreboard players set q.side_file ci_quest 67
execute if entity @s[tag=file_opened,tag=!notices_filed,tag=!sold_docs] run scoreboard players display name q.side_file ci_quest [{"text":"• Rebuild the record for Lucian","color":"gray"}]
execute if entity @s[tag=docs_filed,tag=!notices_filed,tag=!sold_docs] if score @s memory_fragment matches ..2 run scoreboard players display name q.side_file ci_quest [{"text":"• Lucian waits on a third badge","color":"gray"}]
execute if entity @s[tag=sold_docs] run scoreboard players set q.side_file ci_quest 67
execute if entity @s[tag=sold_docs] run scoreboard players display name q.side_file ci_quest [{"text":"• The record is Company property now","color":"dark_red"}]
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
execute if entity @s[tag=yield_report_taken,tag=!scrub_report_filed] if score @s ci_papers_held matches ..1 run scoreboard players set q.side_audit ci_quest 63
execute if entity @s[tag=yield_report_taken,tag=!scrub_report_filed] if score @s ci_papers_held matches ..1 run scoreboard players display name q.side_audit ci_quest [{"text":"• File the yield report with Lucian","color":"gray"}]

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
execute if entity @s[tag=farm_1_free,tag=!homecoming_paid,tag=!homecoming_walking] run scoreboard players set q.side_deng ci_quest 59
execute if entity @s[tag=farm_1_free,tag=!homecoming_paid,tag=!homecoming_walking] run scoreboard players display name q.side_deng ci_quest [{"text":"• Walk the Deng family home","color":"gray"}]
execute if entity @s[tag=homecoming_walking,tag=!homecoming_paid] run scoreboard players set q.side_deng ci_quest 59
execute if entity @s[tag=homecoming_walking,tag=!homecoming_paid] run scoreboard players display name q.side_deng ci_quest [{"text":"• Lead the Dengs to the Firstfurrow gate","color":"gray"}]

# The Lane Looks After Its Own (Oma's delivery loop, round 13d — now trackable): deliver
# leg while any door is unvisited, then the return leg once all three are done.
scoreboard players reset q.side_lane ci_quest
execute if entity @s[tag=lane_started,tag=!lane_done,tag=!delivered_1] run scoreboard players set q.side_lane ci_quest 78
execute if entity @s[tag=lane_started,tag=!lane_done,tag=!delivered_2] run scoreboard players set q.side_lane ci_quest 78
execute if entity @s[tag=lane_started,tag=!lane_done,tag=!delivered_3] run scoreboard players set q.side_lane ci_quest 78
execute if score q.side_lane ci_quest matches 78 run scoreboard players display name q.side_lane ci_quest [{"text":"• Take Oma's baskets down the lane","color":"gray"}]
execute if entity @s[tag=delivered_1,tag=delivered_2,tag=delivered_3,tag=!lane_done] run scoreboard players set q.side_lane ci_quest 78
execute if entity @s[tag=delivered_1,tag=delivered_2,tag=delivered_3,tag=!lane_done] run scoreboard players display name q.side_lane ci_quest [{"text":"• Bring the care package back to Oma","color":"gray"}]

# Right of Way (Harvest Road survey detail): the ambush itself has no accept latch (sight-
# triggered by design); only the manifest leg is trackable — wagon paper in hand until Lucian pays.
scoreboard players reset q.side_manifest ci_quest
execute if entity @s[tag=took_route_manifest,tag=!manifest_paid] if score @s ci_papers_held matches ..1 run scoreboard players set q.side_manifest ci_quest 58
execute if entity @s[tag=took_route_manifest,tag=!manifest_paid] if score @s ci_papers_held matches ..1 run scoreboard players display name q.side_manifest ci_quest [{"text":"• Bring the route manifest to Lucian","color":"gray"}]

# Preferred Provider (Dr. Asha's clinic restock): from the accepted list (clinic_supply_started)
# until the bundle lands (clinic_stocked). The daily rx that follows is a walk-up, untracked.
scoreboard players reset q.side_clinic ci_quest
execute if entity @s[tag=clinic_supply_started,tag=!clinic_stocked] run scoreboard players set q.side_clinic ci_quest 57
execute if entity @s[tag=clinic_supply_started,tag=!clinic_stocked] run scoreboard players display name q.side_clinic ci_quest [{"text":"• Clinic list: 8 oran, 4 pecha, 2 cheri","color":"gray"}]

# FILING DAY aggregate (slot 75 — the formerly-vacant hole between survey 76 and prices 74):
# when the player is carrying TWO OR MORE unfiled Company papers (ci_papers_held, recomputed
# by sidequest/personnel_file/papers_tick), the five individual "…to Lucian" deliver lines
# above suppress themselves (their `if score ..1` guards) and this one line takes their
# place — one trip, one ritual, one desk. Macro render mirrors quest/set_wheat.
scoreboard players reset q.side_papers ci_quest
execute if score @s ci_papers_held matches 2.. run scoreboard players set q.side_papers ci_quest 75
execute if score @s ci_papers_held matches 2.. store result storage cobblemon_initiative:quest papers int 1 run scoreboard players get @s ci_papers_held
execute if score @s ci_papers_held matches 2.. run function cobblemon_initiative:quest/set_papers with storage cobblemon_initiative:quest

# ── Battle Frontier (post-Royal-League set-piece, endgame band 82-85; sits just under the
# main line at 100). Four holders that never overlap by more than two at once: the sign
# line hides after registration; the hall counter hides once all seven fall and the door
# line takes over; the plaque line is a one-shot lore pointer. Same reset-then-set contract
# as the blocks above — all read-only derivations from tags the frontier subsystem latches.
#
# Sign the ledger (82): a champion who has not yet signed. Retargets to the Registrar.
scoreboard players reset q.side_frontier ci_quest
execute if entity @s[tag=royal_league_champion] unless entity @s[tag=frontier_registered] run scoreboard players set q.side_frontier ci_quest 82
execute if entity @s[tag=royal_league_champion] unless entity @s[tag=frontier_registered] run scoreboard players display name q.side_frontier ci_quest [{"text":"• Sign the Frontier ledger","color":"aqua"}]

# Count the seven facility brains cleared (scratch #halls — feeds both the N/7 macro and the
# door line below). frontier_all_cleared is the CAVE (Selene) tag and is counted separately.
scoreboard players set #halls quest_hud 0
execute if entity @s[tag=frontier_tower_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_factory_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_castle_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_arcade_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_port_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_pyramid_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_market_cleared] run scoreboard players add #halls quest_hud 1

# The Seven Halls (83): registered, fewer than seven cleared. Dynamic $(halls)/7 macro; the
# per-hall waypoint retarget lives in quest_targets q.side_frontier_hall.
scoreboard players reset q.side_frontier_hall ci_quest
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_all_cleared] if score #halls quest_hud matches ..6 run scoreboard players set q.side_frontier_hall ci_quest 83
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_all_cleared] if score #halls quest_hud matches ..6 store result storage cobblemon_initiative:quest halls int 1 run scoreboard players get #halls quest_hud
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_all_cleared] if score #halls quest_hud matches ..6 run function cobblemon_initiative:quest/set_frontier with storage cobblemon_initiative:quest

# The Warden (84): all seven cleared, the Deep Dark door open, Selene not yet beaten.
scoreboard players reset q.side_frontier_done ci_quest
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_all_cleared] if score #halls quest_hud matches 7.. run scoreboard players set q.side_frontier_done ci_quest 84
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_all_cleared] if score #halls quest_hud matches 7.. run scoreboard players display name q.side_frontier_done ci_quest [{"text":"• The Deep Dark door is open - face the Warden","color":"dark_aqua"}]

# The Last Honest Signature (85): registered, plaque not yet read. One-shot lore pointer.
scoreboard players reset q.side_frontier_plaque ci_quest
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_plaque_read] run scoreboard players set q.side_frontier_plaque ci_quest 85
execute if entity @s[tag=frontier_registered] unless entity @s[tag=frontier_plaque_read] run scoreboard players display name q.side_frontier_plaque ci_quest [{"text":"• Read the founding plaque","color":"gray"}]

# ── Mystic Marsh (gym 3) side quests (slots 54-56) ──
# Wisps in the Reeds (Marigold's charm fetch): lights after the rumor hub (met_mm_nurse), off on turn-in.
scoreboard players reset q.side_wisps ci_quest
execute if entity @s[tag=met_mm_nurse,tag=!mm_charms_done] run scoreboard players set q.side_wisps ci_quest 56
execute if entity @s[tag=met_mm_nurse,tag=!mm_charms_done] run scoreboard players display name q.side_wisps ci_quest [{"text":"• Bring 8 string to the charm-weaver","color":"gray"}]

# Verified Weather (Osric's exchange board): only once the money feels wrong (cd_instability >= 16), off on witness.
scoreboard players reset q.side_verified ci_quest
execute unless entity @s[tag=mm_board_done] if score #idx cd_instability matches 16.. run scoreboard players set q.side_verified ci_quest 55
execute unless entity @s[tag=mm_board_done] if score #idx cd_instability matches 16.. run scoreboard players display name q.side_verified ci_quest [{"text":"• Witness the exchange board at the marsh kiosk","color":"gray"}]

# The Mirebloom Paddy (frees farm_2): lights after the rumor hub points south, off on liberation (field_2_liberated).
scoreboard players reset q.side_mirebloom ci_quest
execute if entity @s[tag=met_mm_nurse,tag=!field_2_liberated] run scoreboard players set q.side_mirebloom ci_quest 54
execute if entity @s[tag=met_mm_nurse,tag=!field_2_liberated] run scoreboard players display name q.side_mirebloom ci_quest [{"text":"• Free the Mirebloom Paddies from the Company","color":"gray"}]


# ═══════════ Town quest packs (gyms 4-7) ═══════════
# ===== Deepcore City (gym 4, Fighting) =====
# ── Deepcore City (gym 4) side quests (slots 50-53) ──
# Deep Restructuring (Kang's reserve-fraud fetch, slot 53): lights after the rumor hub
# (met_deepcore_nurse), off on turn-in (deepcore_restructure_done). Waypoint retargets across
# stages via quest_targets q.side_reserve (giver -> ledger board -> back to Kang -> optional wager).
scoreboard players reset q.side_reserve ci_quest
execute if entity @s[tag=met_deepcore_nurse,tag=!deepcore_restructure_done] run scoreboard players set q.side_reserve ci_quest 53
execute if entity @s[tag=met_deepcore_nurse,tag=!deepcore_restructure_done] run scoreboard players display name q.side_reserve ci_quest [{"text":"• Look into the re-verified ore ledger for Foreman Kang","color":"gold"}]

# The Iron Ladder (Old Dun's no-heal gauntlet, slot 52): lights once a Bruno student is beaten
# (defeated_deepcore_trainer_2), off on iron_ladder_cleared.
scoreboard players reset q.side_ladder ci_quest
execute if entity @s[tag=defeated_deepcore_trainer_2,tag=!iron_ladder_cleared] run scoreboard players set q.side_ladder ci_quest 52
execute if entity @s[tag=defeated_deepcore_trainer_2,tag=!iron_ladder_cleared] run scoreboard players display name q.side_ladder ci_quest [{"text":"• Climb Old Dun's Iron Ladder - three fights, no heals","color":"gold"}]

# The Deep Office (scrubbing-artifact set-piece, slot 51): appears once the restructure quest is
# underway (a reason to be down the shaft), off once the portrait is seen (seen_deep_office).
scoreboard players reset q.side_office ci_quest
execute if entity @s[tag=deepcore_restructure_started,tag=!seen_deep_office] run scoreboard players set q.side_office ci_quest 51
execute if entity @s[tag=deepcore_restructure_started,tag=!seen_deep_office] run scoreboard players display name q.side_office ci_quest [{"text":"• Something is sealed deep in the played-out shaft","color":"dark_gray"}]

# The Better Rate (mandated wheat-trader recognition beat, slot 50): only once the pitch is heard
# (heard_wheat_pitch, shipped), off if the trader turns hostile or is beaten.
scoreboard players reset q.side_rate ci_quest
execute if entity @s[tag=heard_wheat_pitch,tag=!wheat_trader_hostile,tag=!defeated_deepcore_wheat_trader] run scoreboard players set q.side_rate ci_quest 50
execute if entity @s[tag=heard_wheat_pitch,tag=!wheat_trader_hostile,tag=!defeated_deepcore_wheat_trader] run scoreboard players display name q.side_rate ci_quest [{"text":"• Weigh the grain buyer at the Deepcore commissary","color":"gray"}]

# ===== Gaviota Port (gym 5, Water) =====

# -- Gaviota Port (gym 5) side quests (slots 44-46) --
# Mending the Deep Nets (Rui fetch->wager): lights after the rumor hub (met_gaviota_nurse), stages by bosun_net_done, off on the wager win.
scoreboard players reset q.side_rui ci_quest
execute if entity @s[tag=met_gaviota_nurse,tag=!bosun_net_done] run scoreboard players set q.side_rui ci_quest 44
execute if entity @s[tag=met_gaviota_nurse,tag=!bosun_net_done] run scoreboard players display name q.side_rui ci_quest [{"text":"• Bring 8 string to Netmender Rui","color":"gray"}]
execute if entity @s[tag=bosun_net_done,tag=!defeated_sq_rui_wager] run scoreboard players set q.side_rui ci_quest 44
execute if entity @s[tag=bosun_net_done,tag=!defeated_sq_rui_wager] run scoreboard players display name q.side_rui ci_quest [{"text":"• Take Bosun Rui up on his wager","color":"gray"}]

# The Tide Market (Odessa black-market fetch): lights after the rumor hub, stages by odessa_crate_started, off on recovery.
scoreboard players reset q.side_odessa ci_quest
execute if entity @s[tag=met_gaviota_nurse,tag=!odessa_crate_started,tag=!odessa_crate_recovered] run scoreboard players set q.side_odessa ci_quest 45
execute if entity @s[tag=met_gaviota_nurse,tag=!odessa_crate_started,tag=!odessa_crate_recovered] run scoreboard players display name q.side_odessa ci_quest [{"text":"• Find the fence under the boardwalk","color":"gray"}]
execute if entity @s[tag=odessa_crate_started,tag=!odessa_crate_recovered] run scoreboard players set q.side_odessa ci_quest 45
execute if entity @s[tag=odessa_crate_started,tag=!odessa_crate_recovered] run scoreboard players display name q.side_odessa ci_quest [{"text":"• Recover the seized crate off the customs float","color":"gray"}]

# Adjusted Freight (Kaito manifest audit): lights on gaviota_manifest_check_active, off on gaviota_manifests_filed. Dynamic $(manifests)/3; flips to the report leg at 3/3. Mirrors the q.side_prices block.
scoreboard players reset q.side_freight ci_quest
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] run scoreboard players set #manifests quest_hud 0
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] if entity @s[tag=gaviota_manifest_1] run scoreboard players add #manifests quest_hud 1
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] if entity @s[tag=gaviota_manifest_2] run scoreboard players add #manifests quest_hud 1
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] if entity @s[tag=gaviota_manifest_3] run scoreboard players add #manifests quest_hud 1
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] run scoreboard players set q.side_freight ci_quest 46
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] store result storage cobblemon_initiative:quest manifests int 1 run scoreboard players get #manifests quest_hud
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] if score #manifests quest_hud matches ..2 run function cobblemon_initiative:sidequest/manifest/set_manifests with storage cobblemon_initiative:quest
execute if entity @s[tag=gaviota_manifest_check_active] unless entity @s[tag=gaviota_manifests_filed] if score #manifests quest_hud matches 3.. run scoreboard players display name q.side_freight ci_quest [{"text":"• Report the manifest shortfalls to Kaito","color":"gray"}]

# ===== Kalahar Reach =====
# ── Kalahar Reach (gym 6) side quests (slots 42-43) ──
# The Reach Remembers (Ossa's boundary stones): opens on her accept (boundary_stones_active),
# retargets stone-1 dune line -> the guarded stone-3 road -> back to Ossa to file; off when filed.
scoreboard players reset q.side_kalahar_stones ci_quest
execute if entity @s[tag=boundary_stones_active,tag=!boundary_stones_done] run scoreboard players set q.side_kalahar_stones ci_quest 43
execute if entity @s[tag=boundary_stones_active,tag=!boundary_stones_done] run scoreboard players display name q.side_kalahar_stones ci_quest [{"text":"• Unearth the survey stones along the dune line","color":"gray"}]
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=!seal_stone_3,tag=!boundary_stones_done] run scoreboard players display name q.side_kalahar_stones ci_quest [{"text":"• Read the guarded stone on the Old Caravan Road","color":"gray"}]
execute if entity @s[tag=seal_stone_1,tag=seal_stone_2,tag=seal_stone_3,tag=!boundary_stones_done] run scoreboard players display name q.side_kalahar_stones ci_quest [{"text":"• File the counter-claim with Warden Ossa","color":"gray"}]

# Dry Season (Marisol's wells): opens on her accept (dry_season_active), retargets the Oasis
# pump crew -> the manifold once both are down -> back to Marisol to report; off when restored.
scoreboard players reset q.side_kalahar_water ci_quest
execute if entity @s[tag=dry_season_active,tag=!dry_season_done] run scoreboard players set q.side_kalahar_water ci_quest 42
execute if entity @s[tag=dry_season_active,tag=!dry_season_done] run scoreboard players display name q.side_kalahar_water ci_quest [{"text":"• Drive off the Company pump crew at the Oasis","color":"gray"}]
execute if entity @s[tag=dry_season_active,tag=defeated_sq_pump_officer,tag=defeated_sq_pump_foreman,tag=!oasis_pump_off,tag=!dry_season_done] run scoreboard players display name q.side_kalahar_water ci_quest [{"text":"• Shut the pump manifold at the Oasis","color":"gray"}]
execute if entity @s[tag=oasis_pump_off,tag=!dry_season_done] run scoreboard players display name q.side_kalahar_water ci_quest [{"text":"• Report the shut pump to Well-Keeper Marisol","color":"gray"}]

# ===== Cyber City (gym 7, Electric) =====
# ── Cyber City (gym 7) side quests (slots 34-37) ──
# The Door Downtown (Ohmond, HQ on-ramp): two mutually-exclusive stages, post-Volt only. Stage 2
# (hear the math) until hq_pointer_done; stage 1 (starve the fields) after, until the keycard lands.
scoreboard players reset q.side_door ci_quest
execute if entity @s[tag=defeated_cyber_leader,tag=!hq_pointer_done] run scoreboard players set q.side_door ci_quest 37
execute if entity @s[tag=defeated_cyber_leader,tag=!hq_pointer_done] run scoreboard players display name q.side_door ci_quest [{"text":"• Hear the door math from Ohmond","color":"gray"}]
execute if entity @s[tag=hq_pointer_done,tag=!hq_keycard] run scoreboard players set q.side_door ci_quest 37
execute if entity @s[tag=hq_pointer_done,tag=!hq_keycard] run scoreboard players display name q.side_door ci_quest [{"text":"• Liberate the last fields, then find the tower door","color":"gold"}]

# Off the Records (Maren whistleblower): post-Volt; from the accept latch (ci_file_active) to the file recovery (ci_file_done).
scoreboard players reset q.side_offrecords ci_quest
execute if entity @s[tag=ci_file_active,tag=!ci_file_done] run scoreboard players set q.side_offrecords ci_quest 36
execute if entity @s[tag=ci_file_active,tag=!ci_file_done] run scoreboard players display name q.side_offrecords ci_quest [{"text":"• Recover 3 file pages from the archive drops","color":"gray"}]

# Signal Integrity (Rell surveillance): from the accept latch (ci_signal_active) to the last board (ci_signal_done).
scoreboard players reset q.side_signal ci_quest
execute if entity @s[tag=ci_signal_active,tag=!ci_signal_done] run scoreboard players set q.side_signal ci_quest 35
execute if entity @s[tag=ci_signal_active,tag=!ci_signal_done] run scoreboard players display name q.side_signal ci_quest [{"text":"• Scrub 3 glitching billboards downtown","color":"gray"}]

# Exchange Rate (teller data quest): from the accept latch (ci_reserves_active) to the count turn-in (ci_reserves_done).
scoreboard players reset q.side_exchange ci_quest
execute if entity @s[tag=ci_reserves_active,tag=!ci_reserves_done] run scoreboard players set q.side_exchange ci_quest 34
execute if entity @s[tag=ci_reserves_active,tag=!ci_reserves_done] run scoreboard players display name q.side_exchange ci_quest [{"text":"• Re-verify 3 reserve tags downtown","color":"gray"}]

# ═══════════ Town quest packs (gyms 8-10) ═══════════
# ===== Ryujin Keep (gym 8, Dragon) (generated from quest_targets) =====
scoreboard players reset q.side_oath ci_quest
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_oath_told] run scoreboard players set q.side_oath ci_quest 28
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_oath_told] run scoreboard players display name q.side_oath ci_quest [{"text":"• Hear the First Oath from Skywatcher Rei","color":"gray"}]
scoreboard players reset q.side_mail ci_quest
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_mail_done] run scoreboard players set q.side_mail ci_quest 29
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_mail_done] run scoreboard players display name q.side_mail ci_quest [{"text":"• Bring Tetsu 8 dragon scales","color":"gray"}]
execute if entity @s[tag=ryujin_mail_done,tag=!defeated_sq_ryujin_tetsu_wager,tag=!declined_sq_ryujin_tetsu_wager] run scoreboard players set q.side_mail ci_quest 29
execute if entity @s[tag=ryujin_mail_done,tag=!defeated_sq_ryujin_tetsu_wager,tag=!declined_sq_ryujin_tetsu_wager] run scoreboard players display name q.side_mail ci_quest [{"text":"• Take Tetsu wager, or part friends","color":"gray"}]
scoreboard players reset q.side_heritage ci_quest
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_charter_read,tag=!ryujin_heritage_settled,tag=!declined_ryujin_heritage_envoy] run scoreboard players set q.side_heritage ci_quest 30
execute if entity @s[tag=defeated_villain_boss,tag=!ryujin_charter_read,tag=!ryujin_heritage_settled,tag=!declined_ryujin_heritage_envoy] run scoreboard players display name q.side_heritage ci_quest [{"text":"• Read the Sovereign Charter at the lectern","color":"gray"}]
execute if entity @s[tag=ryujin_charter_read,tag=!ryujin_heritage_settled,tag=!declined_ryujin_heritage_envoy] run scoreboard players set q.side_heritage ci_quest 30
execute if entity @s[tag=ryujin_charter_read,tag=!ryujin_heritage_settled,tag=!declined_ryujin_heritage_envoy] run scoreboard players display name q.side_heritage ci_quest [{"text":"• Send the Heritage envoy packing","color":"gray"}]
scoreboard players reset q.side_clerk8 ci_quest
execute if entity @s[tag=ryujin_defector_met,tag=!ryujin_ledger_taken] run scoreboard players set q.side_clerk8 ci_quest 31
execute if entity @s[tag=ryujin_defector_met,tag=!ryujin_ledger_taken] run scoreboard players display name q.side_clerk8 ci_quest [{"text":"• Take the ledger page from the clerk","color":"gray"}]

# ===== Nifl Town (gym 9, Ice) (generated from quest_targets) =====
scoreboard players reset q.side_archive ci_quest
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_core_1,tag=!nifl_archive_read] run scoreboard players set q.side_archive ci_quest 24
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_core_1,tag=!nifl_archive_read] run scoreboard players display name q.side_archive ci_quest [{"text":"• Ask Auditor Corvin about the vault","color":"gray"}]
execute if entity @s[tag=nifl_core_1,tag=!nifl_core_2] run scoreboard players set q.side_archive ci_quest 24
execute if entity @s[tag=nifl_core_1,tag=!nifl_core_2] run scoreboard players display name q.side_archive ci_quest [{"text":"• Thaw the ledger cores with Corvin","color":"gray"}]
execute if entity @s[tag=nifl_core_2,tag=!nifl_archive_read] run scoreboard players set q.side_archive ci_quest 24
execute if entity @s[tag=nifl_core_2,tag=!nifl_archive_read] run scoreboard players display name q.side_archive ci_quest [{"text":"• Get past Halden to the last core","color":"gray"}]
scoreboard players reset q.side_frostgate ci_quest
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_frostgate_clear] run scoreboard players set q.side_frostgate ci_quest 23
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_frostgate_clear] run scoreboard players display name q.side_frostgate ci_quest [{"text":"• Settle the Frostgate with Warrant Officer Dain","color":"gray"}]
scoreboard players reset q.side_lanterns ci_quest
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_lantern_1,tag=!nifl_lanterns_done] run scoreboard players set q.side_lanterns ci_quest 22
execute if entity @s[tag=defeated_ryujin_leader,tag=!nifl_lantern_1,tag=!nifl_lanterns_done] run scoreboard players display name q.side_lanterns ci_quest [{"text":"• Ask Keeper Vetra about the dark lanterns","color":"gray"}]
execute if entity @s[tag=nifl_lantern_1,tag=!nifl_lanterns_done] run scoreboard players set q.side_lanterns ci_quest 22
execute if entity @s[tag=nifl_lantern_1,tag=!nifl_lanterns_done] run scoreboard players display name q.side_lanterns ci_quest [{"text":"• Relight the lake lanterns with Vetra","color":"gray"}]

# ===== Scorchspire (generated from quest_targets) =====
scoreboard players reset q.side_forgeorder ci_quest
execute if entity @s[tag=met_scorchspire_healer,tag=!forge_order_1] run scoreboard players set q.side_forgeorder ci_quest 16
execute if entity @s[tag=met_scorchspire_healer,tag=!forge_order_1] run scoreboard players display name q.side_forgeorder ci_quest [{"text":"• The forge runs a dead Company order - see Forgemaster Sena","color":"gray"}]
execute if entity @s[tag=forge_order_1,tag=!forge_order_2] run scoreboard players set q.side_forgeorder ci_quest 16
execute if entity @s[tag=forge_order_1,tag=!forge_order_2] run scoreboard players display name q.side_forgeorder ci_quest [{"text":"• Burn the requisition ledgers with Sena","color":"gray"}]
execute if entity @s[tag=forge_order_2,tag=!forge_order_agent_clear,tag=!declined_sq_recovery_agent] run scoreboard players set q.side_forgeorder ci_quest 16
execute if entity @s[tag=forge_order_2,tag=!forge_order_agent_clear,tag=!declined_sq_recovery_agent] run scoreboard players display name q.side_forgeorder ci_quest [{"text":"• Settle the Asset Recovery collector by the quench","color":"gray"}]
execute if entity @s[tag=declined_sq_recovery_agent,tag=forge_order_2,tag=!forge_order_done,tag=!forge_order_agent_clear] run scoreboard players set q.side_forgeorder ci_quest 16
execute if entity @s[tag=declined_sq_recovery_agent,tag=forge_order_2,tag=!forge_order_done,tag=!forge_order_agent_clear] run scoreboard players display name q.side_forgeorder ci_quest [{"text":"• Return to Sena and burn the blank charter","color":"gray"}]
execute if entity @s[tag=forge_order_agent_clear,tag=!forge_order_done] run scoreboard players set q.side_forgeorder ci_quest 16
execute if entity @s[tag=forge_order_agent_clear,tag=!forge_order_done] run scoreboard players display name q.side_forgeorder ci_quest [{"text":"• Return to Sena and burn the blank charter","color":"gray"}]
scoreboard players reset q.side_tempering ci_quest
execute if entity @s[tag=met_scorchspire_healer,tag=!temper_blade_done] run scoreboard players set q.side_tempering ci_quest 17
execute if entity @s[tag=met_scorchspire_healer,tag=!temper_blade_done] run scoreboard players display name q.side_tempering ci_quest [{"text":"• Bring Bladesmith Hollis 8 iron ingots to temper a keepsake","color":"gray"}]
execute if entity @s[tag=temper_blade_done,tag=!defeated_sq_temper_hollis] run scoreboard players set q.side_tempering ci_quest 17
execute if entity @s[tag=temper_blade_done,tag=!defeated_sq_temper_hollis] run scoreboard players display name q.side_tempering ci_quest [{"text":"• Test the tempered edge against Hollis (optional wager)","color":"gray"}]
scoreboard players reset q.side_thehand ci_quest
execute if entity @s[tag=met_scorchspire_healer,tag=!the_hand_started] run scoreboard players set q.side_thehand ci_quest 18
execute if entity @s[tag=met_scorchspire_healer,tag=!the_hand_started] run scoreboard players display name q.side_thehand ci_quest [{"text":"• Old Marren remembers the hand that signed the plates - hear him out","color":"gray"}]
execute if entity @s[tag=the_hand_started,tag=!the_hand_plate] run scoreboard players set q.side_thehand ci_quest 18
execute if entity @s[tag=the_hand_started,tag=!the_hand_plate] run scoreboard players display name q.side_thehand ci_quest [{"text":"• Dig the half-slagged door plate from the forge slag heap","color":"gray"}]
execute if entity @s[tag=the_hand_plate,tag=!the_hand_done] run scoreboard players set q.side_thehand ci_quest 18
execute if entity @s[tag=the_hand_plate,tag=!the_hand_done] run scoreboard players display name q.side_thehand ci_quest [{"text":"• Set the door plate on the Marren anvil","color":"gray"}]
scoreboard players reset q.side_retirement ci_quest
execute if entity @s[tag=met_scorchspire_healer,tag=!severance_met] run scoreboard players set q.side_retirement ci_quest 19
execute if entity @s[tag=met_scorchspire_healer,tag=!severance_met] run scoreboard players display name q.side_retirement ci_quest [{"text":"• A Company clerk is hiding on the shrine road - find her","color":"gray"}]
execute if entity @s[tag=severance_met,tag=!asset_recovery_clear] run scoreboard players set q.side_retirement ci_quest 19
execute if entity @s[tag=severance_met,tag=!asset_recovery_clear] run scoreboard players display name q.side_retirement ci_quest [{"text":"• Beat the Asset Recovery tag-team on the shrine road","color":"gray"}]
execute if entity @s[tag=asset_recovery_clear,tag=!retirement_memo_taken] run scoreboard players set q.side_retirement ci_quest 19
execute if entity @s[tag=asset_recovery_clear,tag=!retirement_memo_taken] run scoreboard players display name q.side_retirement ci_quest [{"text":"• Take the memo trail from Severance","color":"gray"}]


# ═══════════ Nobles + Shrines endgame cluster ═══════════
# ===== 14_shrines =====
scoreboard players set #shrines quest_hud 0
execute if entity @s[tag=defeated_fairy_shrine_leader] run scoreboard players add #shrines quest_hud 1
execute if entity @s[tag=defeated_ground_shrine_leader] run scoreboard players add #shrines quest_hud 1
execute if entity @s[tag=defeated_dragon_shrine_leader] run scoreboard players add #shrines quest_hud 1
execute if entity @s[tag=defeated_ice_shrine_leader] run scoreboard players add #shrines quest_hud 1
execute if entity @s[tag=defeated_fire_shrine_leader] run scoreboard players add #shrines quest_hud 1
scoreboard players reset q.side_shrines_capstone ci_quest
execute if score #shrines quest_hud matches 1.. unless entity @s[tag=five_keepers_paid] run scoreboard players set q.side_shrines_capstone ci_quest 93
execute if score #shrines quest_hud matches 1.. unless entity @s[tag=five_keepers_paid] run scoreboard players display name q.side_shrines_capstone ci_quest [{"text":"• Five Keepers - clear all five elemental shrines","color":"aqua"}]
execute if entity @s[tag=defeated_fairy_shrine_leader,tag=defeated_ground_shrine_leader,tag=defeated_dragon_shrine_leader,tag=defeated_ice_shrine_leader,tag=defeated_fire_shrine_leader,tag=!five_keepers_paid] run scoreboard players set q.side_shrines_capstone ci_quest 93
execute if entity @s[tag=defeated_fairy_shrine_leader,tag=defeated_ground_shrine_leader,tag=defeated_dragon_shrine_leader,tag=defeated_ice_shrine_leader,tag=defeated_fire_shrine_leader,tag=!five_keepers_paid] run scoreboard players display name q.side_shrines_capstone ci_quest [{"text":"• Five Keepers - claim the crystals from the Last Pilgrim","color":"aqua"}]
execute if entity @s[tag=five_keepers_paid] run scoreboard players set q.side_shrines_capstone ci_quest 93
execute if entity @s[tag=five_keepers_paid] run scoreboard players display name q.side_shrines_capstone ci_quest [{"text":"• Five Keepers - the crystals answer to one hand","color":"dark_aqua"}]
scoreboard players reset q.side_shrine_fairy ci_quest
execute if entity @s[tag=defeated_mystic_leader,tag=!defeated_fairy_shrine_leader] run scoreboard players set q.side_shrine_fairy ci_quest 94
execute if entity @s[tag=defeated_mystic_leader,tag=!defeated_fairy_shrine_leader] run scoreboard players display name q.side_shrine_fairy ci_quest [{"text":"• Climb the Fairy Shrine cultist ladder","color":"gray"}]
execute if entity @s[tag=defeated_mystic_leader,tag=defeated_fairy_shrine_cultist_2,tag=!defeated_fairy_shrine_leader] run scoreboard players set q.side_shrine_fairy ci_quest 94
execute if entity @s[tag=defeated_mystic_leader,tag=defeated_fairy_shrine_cultist_2,tag=!defeated_fairy_shrine_leader] run scoreboard players display name q.side_shrine_fairy ci_quest [{"text":"• Face High Priestess Aurora","color":"light_purple"}]
execute if entity @s[tag=defeated_fairy_shrine_leader] run scoreboard players set q.side_shrine_fairy ci_quest 94
execute if entity @s[tag=defeated_fairy_shrine_leader] run scoreboard players display name q.side_shrine_fairy ci_quest [{"text":"• Fairy Shrine cleared - the crystal raises Xerneas","color":"dark_aqua"}]
scoreboard players reset q.side_shrine_ground ci_quest
execute if entity @s[tag=defeated_kalahar_leader,tag=!defeated_ground_shrine_leader] run scoreboard players set q.side_shrine_ground ci_quest 95
execute if entity @s[tag=defeated_kalahar_leader,tag=!defeated_ground_shrine_leader] run scoreboard players display name q.side_shrine_ground ci_quest [{"text":"• Descend the Ground Shrine cultist ladder","color":"gray"}]
execute if entity @s[tag=defeated_kalahar_leader,tag=defeated_ground_shrine_cultist_2,tag=!defeated_ground_shrine_leader] run scoreboard players set q.side_shrine_ground ci_quest 95
execute if entity @s[tag=defeated_kalahar_leader,tag=defeated_ground_shrine_cultist_2,tag=!defeated_ground_shrine_leader] run scoreboard players display name q.side_shrine_ground ci_quest [{"text":"• Face High Priest Terran - fights in pairs","color":"gold"}]
execute if entity @s[tag=defeated_ground_shrine_leader] run scoreboard players set q.side_shrine_ground ci_quest 95
execute if entity @s[tag=defeated_ground_shrine_leader] run scoreboard players display name q.side_shrine_ground ci_quest [{"text":"• Ground Shrine cleared - the crystal raises Groudon","color":"dark_aqua"}]
scoreboard players reset q.side_shrine_dragon ci_quest
execute if entity @s[tag=defeated_ryujin_leader,tag=!defeated_dragon_shrine_leader] run scoreboard players set q.side_shrine_dragon ci_quest 96
execute if entity @s[tag=defeated_ryujin_leader,tag=!defeated_dragon_shrine_leader] run scoreboard players display name q.side_shrine_dragon ci_quest [{"text":"• Climb the Dragon Shrine cultist ladder","color":"gray"}]
execute if entity @s[tag=defeated_ryujin_leader,tag=defeated_dragon_shrine_cultist_2,tag=!defeated_dragon_shrine_leader] run scoreboard players set q.side_shrine_dragon ci_quest 96
execute if entity @s[tag=defeated_ryujin_leader,tag=defeated_dragon_shrine_cultist_2,tag=!defeated_dragon_shrine_leader] run scoreboard players display name q.side_shrine_dragon ci_quest [{"text":"• Face High Priest Draconis - fights in pairs","color":"gold"}]
execute if entity @s[tag=defeated_dragon_shrine_leader] run scoreboard players set q.side_shrine_dragon ci_quest 96
execute if entity @s[tag=defeated_dragon_shrine_leader] run scoreboard players display name q.side_shrine_dragon ci_quest [{"text":"• Dragon Shrine cleared - the crystal raises Rayquaza","color":"dark_aqua"}]
scoreboard players reset q.side_shrine_ice ci_quest
execute if entity @s[tag=defeated_nifl_leader,tag=!defeated_ice_shrine_leader] run scoreboard players set q.side_shrine_ice ci_quest 97
execute if entity @s[tag=defeated_nifl_leader,tag=!defeated_ice_shrine_leader] run scoreboard players display name q.side_shrine_ice ci_quest [{"text":"• Cross the Ice Shrine cultist ladder","color":"gray"}]
execute if entity @s[tag=defeated_nifl_leader,tag=defeated_ice_shrine_cultist_2,tag=!ice_shrine_trial_clear,tag=!defeated_ice_shrine_leader] run scoreboard players set q.side_shrine_ice ci_quest 97
execute if entity @s[tag=defeated_nifl_leader,tag=defeated_ice_shrine_cultist_2,tag=!ice_shrine_trial_clear,tag=!defeated_ice_shrine_leader] run scoreboard players display name q.side_shrine_ice ci_quest [{"text":"• Cross the frozen path, then face Glacius","color":"aqua"}]
execute if entity @s[tag=defeated_nifl_leader,tag=ice_shrine_trial_clear,tag=!defeated_ice_shrine_leader] run scoreboard players set q.side_shrine_ice ci_quest 97
execute if entity @s[tag=defeated_nifl_leader,tag=ice_shrine_trial_clear,tag=!defeated_ice_shrine_leader] run scoreboard players display name q.side_shrine_ice ci_quest [{"text":"• Face High Priest Glacius","color":"aqua"}]
execute if entity @s[tag=defeated_ice_shrine_leader] run scoreboard players set q.side_shrine_ice ci_quest 97
execute if entity @s[tag=defeated_ice_shrine_leader] run scoreboard players display name q.side_shrine_ice ci_quest [{"text":"• Ice Shrine cleared - the crystal raises Articuno","color":"dark_aqua"}]
scoreboard players reset q.side_shrine_fire ci_quest
execute if entity @s[tag=royal_league_champion,tag=!defeated_fire_shrine_leader] run scoreboard players set q.side_shrine_fire ci_quest 98
execute if entity @s[tag=royal_league_champion,tag=!defeated_fire_shrine_leader] run scoreboard players display name q.side_shrine_fire ci_quest [{"text":"• Climb the Fire Shrine cultist ladder","color":"gray"}]
execute if entity @s[tag=royal_league_champion,tag=defeated_fire_shrine_cultist_2,tag=!fire_shrine_trial_clear,tag=!defeated_fire_shrine_leader] run scoreboard players set q.side_shrine_fire ci_quest 98
execute if entity @s[tag=royal_league_champion,tag=defeated_fire_shrine_cultist_2,tag=!fire_shrine_trial_clear,tag=!defeated_fire_shrine_leader] run scoreboard players display name q.side_shrine_fire ci_quest [{"text":"• Run the burning path, then face Ignis","color":"gold"}]
execute if entity @s[tag=royal_league_champion,tag=fire_shrine_trial_clear,tag=!defeated_fire_shrine_leader] run scoreboard players set q.side_shrine_fire ci_quest 98
execute if entity @s[tag=royal_league_champion,tag=fire_shrine_trial_clear,tag=!defeated_fire_shrine_leader] run scoreboard players display name q.side_shrine_fire ci_quest [{"text":"• Face High Priest Ignis","color":"gold"}]
execute if entity @s[tag=defeated_fire_shrine_leader] run scoreboard players set q.side_shrine_fire ci_quest 98
execute if entity @s[tag=defeated_fire_shrine_leader] run scoreboard players display name q.side_shrine_fire ci_quest [{"text":"• Fire Shrine cleared - the crystal raises Moltres","color":"dark_aqua"}]

# ===== 13_nobles_gating (NOBLE half) =====
# === Noble encounters (endgame set-pieces, slots 86-92; above the town side quests, below the main line at 100). Append to function/quest/render.mcfunction. defeated_noble_<id> is the noble engine storyFlag SCOREBOARD (created lazily on first subdual) - zero-init each objective then gate the done-state on the SCORE, never a tag (a defeated_noble_<id> tag would be dead). ===
scoreboard objectives add defeated_noble_mew dummy
scoreboard objectives add defeated_noble_kyogre dummy
scoreboard objectives add defeated_noble_zapdos dummy
scoreboard objectives add defeated_noble_rayquaza dummy
scoreboard objectives add defeated_noble_articuno dummy
scoreboard objectives add defeated_noble_groudon dummy
scoreboard objectives add defeated_noble_moltres dummy
execute unless score @s defeated_noble_mew matches 0.. run scoreboard players set @s defeated_noble_mew 0
execute unless score @s defeated_noble_kyogre matches 0.. run scoreboard players set @s defeated_noble_kyogre 0
execute unless score @s defeated_noble_zapdos matches 0.. run scoreboard players set @s defeated_noble_zapdos 0
execute unless score @s defeated_noble_rayquaza matches 0.. run scoreboard players set @s defeated_noble_rayquaza 0
execute unless score @s defeated_noble_articuno matches 0.. run scoreboard players set @s defeated_noble_articuno 0
execute unless score @s defeated_noble_groudon matches 0.. run scoreboard players set @s defeated_noble_groudon 0
execute unless score @s defeated_noble_moltres matches 0.. run scoreboard players set @s defeated_noble_moltres 0
# A Giggle in the Grass (Mew chase, 86): mid band (badges 3-6), off once subdued.
scoreboard players reset q.side_wisp ci_quest
execute if score @s memory_fragment matches 3.. unless score @s defeated_noble_mew matches 1.. run scoreboard players set q.side_wisp ci_quest 86
execute if score @s memory_fragment matches 3.. unless score @s defeated_noble_mew matches 1.. run scoreboard players display name q.side_wisp ci_quest [{"text":"• Chase the wisp in the Safari Zone","color":"light_purple"}]
# Under the Storm (Kyogre buoy, 87): post-gym-5, off once subdued.
scoreboard players reset q.side_deep ci_quest
execute if score @s memory_fragment matches 5.. unless score @s defeated_noble_kyogre matches 1.. run scoreboard players set q.side_deep ci_quest 87
execute if score @s memory_fragment matches 5.. unless score @s defeated_noble_kyogre matches 1.. run scoreboard players display name q.side_deep ci_quest [{"text":"• Ring the warning buoy off Gaviota","color":"aqua"}]
# The Defense of Cyber City (Zapdos prompt, 88): post-gym-7 (badges_gte_7), off once subdued.
scoreboard players reset q.side_grid ci_quest
execute if entity @s[tag=badges_gte_7] unless score @s defeated_noble_zapdos matches 1.. run scoreboard players set q.side_grid ci_quest 88
execute if entity @s[tag=badges_gte_7] unless score @s defeated_noble_zapdos matches 1.. run scoreboard players display name q.side_grid ci_quest [{"text":"• Help Grid Warden Cass hold the grid","color":"yellow"}]
# What Falls From the Sky (Rayquaza double-gate, 89): gym 8 AND Dragon Shrine cleared -> the altar; gym 8 without the shrine -> points at the shrine prereq first. Off once subdued.
scoreboard players reset q.side_sky ci_quest
execute if score @s memory_fragment matches 8.. unless score @s defeated_noble_rayquaza matches 1.. run scoreboard players set q.side_sky ci_quest 89
execute if score @s memory_fragment matches 8.. if entity @s[tag=!defeated_dragon_shrine_leader] unless score @s defeated_noble_rayquaza matches 1.. run scoreboard players display name q.side_sky ci_quest [{"text":"• Clear the Dragon Shrine to unseal the sky-altar","color":"gray"}]
execute if score @s memory_fragment matches 8.. if entity @s[tag=defeated_dragon_shrine_leader] unless score @s defeated_noble_rayquaza matches 1.. run scoreboard players display name q.side_sky ci_quest [{"text":"• Raise your hand at the Ryujin sky-altar","color":"green"}]
# Winter Takes Wing (Articuno dovetail, 90): Ice Shrine cleared, off once subdued.
scoreboard players reset q.side_gale ci_quest
execute if entity @s[tag=defeated_ice_shrine_leader] unless score @s defeated_noble_articuno matches 1.. run scoreboard players set q.side_gale ci_quest 90
execute if entity @s[tag=defeated_ice_shrine_leader] unless score @s defeated_noble_articuno matches 1.. run scoreboard players display name q.side_gale ci_quest [{"text":"• Call the frozen gale at the Ice Shrine","color":"aqua"}]
# The Mountain Holds Its Breath (Groudon monument, 91): strict post-gym-10 (badges_gte_10), off once subdued.
scoreboard players reset q.side_mountain ci_quest
execute if entity @s[tag=badges_gte_10] unless score @s defeated_noble_groudon matches 1.. run scoreboard players set q.side_mountain ci_quest 91
execute if entity @s[tag=badges_gte_10] unless score @s defeated_noble_groudon matches 1.. run scoreboard players display name q.side_mountain ci_quest [{"text":"• Strike the warding stone on the crater rim","color":"red"}]
# Rebirth in Ember (Moltres dovetail, 92): Fire Shrine cleared (post-league), off once subdued.
scoreboard players reset q.side_ember ci_quest
execute if entity @s[tag=defeated_fire_shrine_leader] unless score @s defeated_noble_moltres matches 1.. run scoreboard players set q.side_ember ci_quest 92
execute if entity @s[tag=defeated_fire_shrine_leader] unless score @s defeated_noble_moltres matches 1.. run scoreboard players display name q.side_ember ci_quest [{"text":"• Call the reborn flame at the Fire Shrine","color":"gold"}]