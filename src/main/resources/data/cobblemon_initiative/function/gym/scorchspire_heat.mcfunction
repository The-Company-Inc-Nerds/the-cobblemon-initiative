# Scorchspire "Banked Coals" — Spire Heat gauge (fire punishes turtling). NEEDS
# #minecraft:tick registration (tags/function/tick.json — orchestrator adds the entry)
# placed AFTER gym/scorchspire_tower and BEFORE dialog/band_tags so the compiler-derived
# heat band tag reads this tick's value. Objectives scorchspire_heat + scorchspire_away
# are added in gym/load (orchestrator adds those lines too).
#
# Design (per-player; single-player production):
#   +1 heat per second while the challenger stands inside the arena box and is not mid
#   battle. Battle pause: NO general in-battle signal exists (verified — the Java mod
#   writes none, and in_trainer_battle is only set on engage:touch forced fights), so the
#   four warden battle buttons set scorchspire_forging themselves (as_player command
#   action, the starter_totodile pattern) and clear it in their battle onwin/onlose
#   branches — the engage:touch choreography, hand-wired. Heat clamps 0..120. Each warden
#   defeated VENTS -30 heat (their battle on_win). At Vulcan's door the banked dialog
#   entry (priority 18, band tag scorchspire_heat_gte_60) forces the full-strength fight
#   over ready_weakened (17) — low heat plus all four wardens keeps the weakened Vulcan.
#   Cooling: 10 consecutive seconds outside the box (the scorchspire_away countdown,
#   ~200 ticks) resets heat to 0 — the spire only counts coals while you stand in it;
#   re-entry starts a fresh climb. Badge holders (defeated_scorchspire_leader) run cold:
#   no gain, heat pinned to 0, no bossbar.
#
# This file is only the 1-second divider (fake-player clock on the heat objective); all
# real work happens in gym/scorchspire_heat_second, once every 20 ticks.
scoreboard players add #heat_clock scorchspire_heat 1
execute if score #heat_clock scorchspire_heat matches 20.. run function cobblemon_initiative:gym/scorchspire_heat_second
