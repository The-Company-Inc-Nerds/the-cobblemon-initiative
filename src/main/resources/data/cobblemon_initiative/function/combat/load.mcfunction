# combat/load — objectives for the combat tick family (alpha.26 flee cooldown).
# Register in #minecraft:load (data/minecraft/tags/function/load.json wires the tag;
# this file must NOT edit function tags itself — same rule as sidequest/minutes/load).
# ci_flee_cd: per-player flee-cooldown TICKS. NuzlockeInit.startFleeCooldown arms it to
# 6000 (5 min — a3 playtest ruling) when the player flees a TRAINER battle and also creates the objective
# defensively; this covers a fresh world before any flee so combat/flee_cooldown's
# selectors resolve from tick one.
scoreboard objectives add ci_flee_cd dummy
