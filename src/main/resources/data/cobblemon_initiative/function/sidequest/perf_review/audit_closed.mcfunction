# Seen path: badge earned with at least one sighting on record. Run as the player.
# Closes the audit; the sweep payout (600 CD + 4x exp_candy_xs) is the gym guide button
# gated on all four defeated_takehara_trainer_N tags (dialog/sq_perf_review_guide.json).
tag @s add perf_review_resolved
tellraw @s [{"text":"Audit closed. Sightings on record: ","color":"gray"},{"score":{"name":"@s","objective":"gym1_seen"},"color":"red"},{"text":". A full ladder clear can still be compensated — ask the gym guide about the Verification Bonus.","color":"gray"}]
