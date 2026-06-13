# Hardcore Runbook — fresh-world setup

This mod targets a **hardcore + Nuzlocke** single-player run. When a run ends (death),
the world is deleted and a new world is started. Most mod state lives in **world data**
(scoreboards, player tags, `level.dat`) and is correctly lost with the world — but a few
things live **outside** the world and leak across runs. This runbook covers the per-world
reset.

## ⚠️ The config leak (must reset every new world)

`NuzlockeConfig` persists to **`config/cobblemon-initiative.json`** (relative to the game
directory, see `NuzlockeConfig.CONFIG_FILE`). This file **survives world deletion**, so a
previous run's `safeZones` array (and any other config) carries into a fresh world.

Worse: `/cobblemon-initiative install run` **skips zones by name** — if the config already
contains the town/zone definitions from a dead run, `install run` silently no-ops and you
may believe install "did nothing."

**On every fresh hardcore world, before playing:**

1. Delete (or empty the `safeZones` array in) `config/cobblemon-initiative.json`.
   - Safe minimal reset: set `"safeZones": []` and leave the other settings.
2. Run `/cobblemon-initiative install run` — it will now re-apply all zones cleanly.
3. Confirm hardcore is on (see below) and relog once.

> Never use runtime `/safezone add` to bake **per-run** state (e.g. a liberated wheat
> field becoming safe). Runtime-added zones get written into this leaky global config.
> Gate world-dependent safety on a **world-data scoreboard** instead.

## Hardcore flag

Hardcore is a `level.dat` flag (not a difficulty). It's flipped by the install run via mixin
accessors and **requires a client relog** to take effect. Verify the heart UI / no-respawn
behavior after relog.

## Datapack function tags

Activated features hang off `data/minecraft/tags/function/{load,tick}.json`:

- `load.json` → `cobblemon_initiative:memory/init` (creates the `memory_fragment` objective).
  If this is empty, **Memory Fragments silently never fire**.
- `tick.json` → (populated when the Wheat-Trader / economy pollers are placed in-world).

After any datapack change, a `/reload` (or world reload) re-runs `#minecraft:load`.

## Smoke test after setup

- Defeat a gym leader → a styled "[Memory]" fragment title fires **once**.
- Relog → the fragment does **not** re-fire (one-way latch via the defeated-trainer set).
- Outside a safe zone, on a Pokémon faint, a dark-red Dark Urge whisper may appear (12%,
  5-min cooldown); inside a town/shrine safe zone it must **not** appear.
