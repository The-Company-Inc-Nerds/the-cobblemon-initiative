# Zone Mapper

A browser tool for drawing `install.json` zones (towns, routes, shrines, villain
sites, the Royal League, landmarks) on top of an offline whole-map render of the
world, and exporting them in the exact schema `InstallZone.java` expects.

It draws over a **[uNmINeD](https://unmined.net) web export**, so you can lay out
every zone in one view without flying around in-game or reading F3 coordinates
one corner at a time.

## Why this exists

Zones are currently authored by hand in
`src/main/resources/data/cobblemon_initiative/install.json`, with polygon
`vertices` drawn in this editor (the in-game `zone-trace` tool is retired — deleted 2026-07-11). That's slow
for laying out a whole map. This tool lets you *draw* the zones on the actual
terrain and get correct block coordinates out the other end.

## How coordinates stay correct

uNmINeD's web export is an **OpenLayers** app. This tool is a faithful port of the
map construction in the export's own `unmined.openlayers.js`, so tiles render
identically, and — critically — **every drawn point is converted to Minecraft
`(X, Z)` through uNmINeD's own registered `VIEW → DATA` transform.** We do no
coordinate math of our own. That transform is a single Y-flip; the DATA coordinate
equals Minecraft `(X, Z)` with the origin at world `0,0`. All render parameters
(`imageFormat`, `minZoom`, `maxZoom`, region bounds) are read from
`UnminedMapProperties` at runtime — nothing is hardcoded, so it adapts to any
world/export.

## Prerequisites

- **uNmINeD CLI** — **bundled in the dev shell** (`shell.nix` packages the prebuilt
  `unmined-cli`, patched to run on NixOS), so no manual install. To use a different
  build, point the launcher at it with `--unmined /path/to/unmined-cli` (or the
  `UNMINED` env var). Note the GUI build can't render headlessly — the CLI is what's
  packaged. uNmINeD reads the world's region files read-only.
- Python 3 — already in the dev shell (`shell.nix`), used to serve the folder.
- No internet needed: OpenLayers 6.9.0 (UMD full build) is **vendored** in
  `scripts/zone-mapper/vendor/` and copied in beside the editor by the launcher, so
  the page runs fully offline.

## Quick start

From the dev shell, one command renders the map and opens the editor:

```bash
zone-mapper
```

It auto-detects the world staged in `mrpack/maps/<world>/`, renders it with uNmINeD
into **`dev/zone-map/`** (gitignored — renders are never committed), copies the
editor in, serves it, and opens your browser at
<http://localhost:8099/zone-editor.html>.

- The render is **refreshed on every run** so the map is always current. The first run
  is a full render (can take a while); after that it's incremental (uNmINeD only re-does
  changed chunks — fast). Pass `--no-render` to skip and just serve the existing tiles.
  (This is a change from the old "render once, then only on `--rerender`" behaviour that
  made stale maps confusing; `--rerender` is still accepted as a no-op.)
- Render a different save: `zone-mapper --world "/path/to/save"`.
- Re-open an existing render without re-rendering: `zone-mapper <export-dir> --no-render`.
- Options: `-o/--out <dir>` (default `dev/zone-map`), `-p/--port <n>` (default 8099),
  `--dimension <d>` (default overworld), `--no-render`, `--no-open`. See `zone-mapper --help`.

**Do not pass a zoom-in render** (the launcher renders at default zoom, which keeps
coordinates block-exact — see Caveats). A local server is used because some browsers
restrict local tile loads over `file://`.

### The `dev/zone-map/` folder

The render output and the copied `zone-editor.html` live in the gitignored
`dev/zone-map/`. Delete it anytime to force a clean re-render; nothing there is
tracked. The tool's source of truth stays in `scripts/zone-mapper/` and is copied
in fresh on every launch.

## Draw

- **Box / Polygon / Route** → exported as `vertices` (`[{x,z}, …]`). The mod derives
  `centerX`/`centerZ`/`radius` from the polygon centroid + max vertex distance; the
  editor previews those derived values so they match runtime. Use this for
  irregular town outlines and route corridors — same shape the retired `zone-trace` tool produced.
- **Circle** → exported as `centerX/centerY/centerZ` + `radius`, no vertices. Use
  for simple round zones (most gym towns, shrines, the HQ, the Royal League).
- **Edit** → move/select a zone and reshape its vertices; fill in name, subtitle,
  type, colour, `centerY`, dimension, and the `hostileOnly`/`cylindrical`/`announce`
  flags. Colour defaults follow the zone type but are editable.
- **Delete** → click a zone to remove it.
- Live block coordinates show bottom-left as you move the mouse.

## Export into the mod

Click **Copy** (or **Download**) — you get the `zones` array. Paste it as the value
of `"zones"` in
`src/main/resources/data/cobblemon_initiative/install.json`, then:

```bash
gradle build          # bakes install.json into the jar
# in-game:
/cobblemon-initiative install run
```

`install run` registers each zone as a `SafeZone` (mob-spawn suppression +
announcements) and draws it on JourneyMap via Map Frontiers.

## Auto-loaded current zones + NPC overlay

On launch, `zone-mapper` stages two things beside the editor so you open straight
onto the **current** data — no copy/paste:

- **`install.json`** is copied in, and the editor **auto-imports it on load** (same
  path as the Import button; a blue banner confirms the zone count). Edit the zones,
  then **Copy** the export back into
  `src/main/resources/data/cobblemon_initiative/install.json`. If you'd already drawn
  something, the auto-import no-ops so it never clobbers your work.
- **`npc-overlay.js`** is regenerated (`scripts/generate_npc_overlay`) and drawn as
  labeled dots (toggle in the panel): green = wired/authored cast, gray = builder
  flavor, orange = planned. Positions come from the builders' CSV (uuid bodies) layered
  with the **live latch-placed cast** read straight from `dialog-src/characters/`, so
  recently-added NPCs show up. **Caveat:** a uuid body moved by a later
  `repairs_a*_apply` tp still shows at its *CSV* spot (`scripts/npc_roster` is the
  repairs-aware source if exact live body coords ever matter for a zone edge).

These, the terrain render, and the zones are all refreshed every launch, so the
whole view always reflects the source tree (`--no-render` skips just the terrain
re-render if you want a faster, serve-only launch).

## Editing existing zones

You can also **Import** manually to load an `install.json` (or a bare zones array) —
useful for a file that isn't the staged one. Every zone becomes an editable shape on
the map — polygons from their `vertices`, circles from their center+radius. This is
also the fastest **calibration check**: your known zones (Sango Town at `0/0`, The
Company HQ at `1590/1028`, Royal League at `3528/2773`) should land exactly on the
right spots. If they do, the export is aligned for that render.

## Caveats (verified against uNmINeD 1.18 & 1.20.1 exports)

- **Coordinate exactness is verified for default renders (`maxZoom == 0`).** If the
  export was made with `--zoomin`, the tool shows a warning banner; verify one
  exported coordinate against F3 in-game before trusting a batch.
- **Unexplored/ungenerated regions have no tiles** and show blank (404s in the
  console are harmless). uNmINeD renders whatever is saved on disk — chunks that
  were never generated won't appear.
- **The `zones` array is per dimension.** The `dimension` field defaults to
  `minecraft:overworld`; render/import a different dimension's export if needed.
- **Y is not captured from the map** (a top-down render has no useful Y). `centerY`
  defaults to 64 and is editable per zone; it only matters for non-cylindrical
  (full-sphere) zones.

## Files

| File | Role |
|------|------|
| `zone-editor.html` | The whole tool — self-contained OpenLayers page. |
| `README.md` | This file. |
