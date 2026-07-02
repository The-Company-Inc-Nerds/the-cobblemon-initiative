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
`vertices` traced via the in-game `zone-trace` tool and copy-pasted in. That's slow
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

- **uNmINeD** (CLI or GUI) — https://unmined.net
- A copy of the world save you want to map (works on the live save; uNmINeD reads
  region files read-only).
- Python 3 (or any static file server) to serve the export folder.
- Internet on first open (OpenLayers 6.9.0 loads from a CDN). For offline use, drop
  `ol.js` and `ol.css` next to `zone-editor.html` and repoint the tags in its
  `<head>`.

## Workflow

### 1. Render the world with uNmINeD

```bash
unmined-cli web render \
  --world "/path/to/saves/UPM2" \
  --output "/path/to/upm2-map"
```

- `--output` should be an empty folder the first time.
- Re-running the same command later re-renders only changed regions (incremental).
- **Do not pass `--zoomin`** if you want verified block-exact coordinates (see
  Caveats). Default zoom is fine for planning.
- `--dimension overworld` is the default; pass it explicitly for other dimensions.

Run `unmined-cli web render --help` to confirm flags for your installed build —
uNmINeD's CLI options beyond `--world`/`--output` are under-documented and vary by
version.

### 2. Drop the tool into the export

```bash
cp scripts/zone-mapper/zone-editor.html scripts/zone-mapper/README.md /path/to/upm2-map/
```

`zone-editor.html` sits next to uNmINeD's `index.html`. uNmINeD overwrites its own
generated files on re-render but leaves arbitrarily-named files alone, so the tool
survives re-renders. (The source of truth is still the copy in this repo — copy it
in fresh whenever it changes.)

### 3. Serve and open

```bash
cd /path/to/upm2-map
python3 -m http.server 8099
```

Open <http://localhost:8099/zone-editor.html>. A local server is recommended;
opening via `file://` usually works too but some browsers restrict local tile
loads.

### 4. Draw

- **Box / Polygon / Route** → exported as `vertices` (`[{x,z}, …]`). The mod derives
  `centerX`/`centerZ`/`radius` from the polygon centroid + max vertex distance; the
  editor previews those derived values so they match runtime. Use this for
  irregular town outlines and route corridors — same shape `zone-trace` produces.
- **Circle** → exported as `centerX/centerY/centerZ` + `radius`, no vertices. Use
  for simple round zones (most gym towns, shrines, the HQ, the Royal League).
- **Edit** → move/select a zone and reshape its vertices; fill in name, subtitle,
  type, colour, `centerY`, dimension, and the `hostileOnly`/`cylindrical`/`announce`
  flags. Colour defaults follow the zone type but are editable.
- **Delete** → click a zone to remove it.
- Live block coordinates show bottom-left as you move the mouse.

### 5. Export into the mod

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

## Editing existing zones

Use **Import** to load your current `install.json` (or a bare zones array). Every
zone becomes an editable shape on the map — polygons from their `vertices`, circles
from their center+radius. This is also the fastest **calibration check**: your known
zones (Sango Town at `0/0`, The Company HQ at `1590/1028`, Royal League at
`3528/2773`) should land exactly on the right spots. If they do, the export is
aligned for that render.

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
