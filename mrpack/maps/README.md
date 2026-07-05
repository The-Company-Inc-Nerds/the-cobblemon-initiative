# mrpack/maps/ — local map staging (untracked)

Drop the world save folder(s) here — e.g. `mrpack/maps/UPM2/` (a folder that
contains `level.dat`). Everything in this directory is gitignored **except this
README**, so maps are never committed.

Used by:

- **`run-client`** — symlinks each world folder here into `run/saves/`, so the
  Fabric dev client boots straight into the real map. The link is live: changes
  you make while testing persist to the staged world. Copy a world out first if
  you want a throwaway test instance.
- **`build-mrpack --with-map`** — bundles the world(s) here into the `.mrpack`
  under `overrides/saves/`. Modrinth-sourced datapacks (see the `datapacks` list
  in `mrpack/modpack.json` — e.g. the AllTheMons species data) are also placed
  into each bundled world's `datapacks/` folder, so `--with-map` is required for
  them.

## What lives where in a world (block data vs NPCs vs user data)

| Folder / file | Holds | On a map update |
|---|---|---|
| `region/`, `DIM*/region/` | **Block/terrain data** (chunks) — what builders fix | take the new export's |
| `poi/`, `DIM*/poi/` | POI index derived from blocks | take new (regenerates on load anyway) |
| `entities/`, `DIM*/entities/` | Live entities incl. **placed NPCs** (zlib-compressed per chunk) | travels with the export |
| `easy_npc/` (411 `*.npc.nbt`) | **Easy NPC's own NPC persistence** — dialog, skin, and `Pos` per NPC | travels with the export |
| `level.dat` | World metadata + **host Player** (builders play on the save → their inventory/pos/party) | **rebaked; `Data.Player` REMOVED** (fresh host at world spawn) |
| `serverconfig/` | Per-world mod configs (rctmod cap/series) | keep / rebaked by the mod at runtime |
| `data/` | World saved data (scoreboard, mod persistence, rctmod stat.dat, map frontiers) | keep (mostly keyed by player identity → harmless; mod re-inits) |
| `playerdata/ stats/ advancements/ cobblemon*playerdata/ pokedex/ pokemon/ session.lock` | **Per-player state incl. Cobblemon PC/party captures** | **stripped at build** (players start fresh) |

## Updating the bundled map (builders send a new full export)

You do **not** need to redo your level.dat edits or preserve NPC positions by hand:

- **level.dat is fully reproducible, and the builders' host-player is wiped.**
  `build-mrpack` bakes gamerules + difficulty + hardcore into the world *copy* from
  `install.json`, and REMOVES `Data.Player` — because the builders send saves they've
  played/tested on, so that Player carries their inventory, position, party, and the
  map's baked speed. The fresh host then spawns clean at the world spawn (Sango
  2615/109/2843) in survival. Your earlier hand-edits were exactly those install
  settings, so a fresh export gets them re-applied automatically. Want a *new* level.dat
  setting to persist? Add it to `install.json` (ask Claude to extend the bake) — never
  hand-edit the staged map, or it's lost on the next swap.
- **NPCs travel with the export** (`entities/` + `easy_npc/`); the mod applies preset
  content — dialog/skin/battle wiring — at *runtime* (`update_npc_presets` + the refresh
  manager), not baked into the map, so map swaps never touch it.
- **User data is stripped at build** (the table above), so a builder's playthrough
  state never ships.

**If the builders' export CONTAINS the NPCs** (they place them, per the dev CSV):
just replace `mrpack/maps/The Cobblemon Initiative/` wholesale and rebuild — done.

**If the builders send TERRAIN ONLY** (NPCs missing from their export) and you want to
keep the current NPCs: copy only the block data across —
`cp -r <new-export>/{region,poi,DIM-1,DIM1} "The Cobblemon Initiative"/` — keeping the
current `entities/`, `easy_npc/`, `data/`, `serverconfig/`. (Ask Claude to add a
`build-mrpack --blocks-from <dir>` helper if this becomes routine.)

## Faster rebuild+install: dep cache

`build-mrpack --cache` downloads every dependency jar once into `mrpack/cache/`
(content-addressed, gitignored) and **bundles** them into the pack, so installing a
freshly built pack re-downloads nothing but our own mod. The first `--cache` build
populates the cache; later builds reuse it (only changed deps re-download).
