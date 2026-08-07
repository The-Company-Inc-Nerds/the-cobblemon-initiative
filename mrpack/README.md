# mrpack/ — Modrinth modpack build inputs (tracked skeleton)

`build-mrpack` reads this folder to assemble `dist/<name>-<version>.mrpack`.
The structure plus `modpack.json` are **tracked**; the packs and world you drop
into the subfolders are **not** (only the folders are kept, via `.gitkeep` /
READMEs). Every mod / resource pack / shader / datapack resolves to a Modrinth
download link + hashes — nothing here redistributes a jar or zip.

## `modpack.json` *(tracked)* — the pack manifest

- **metadata** — `name`, `summary`, `minecraft`, `fabricLoader`.
- **`mods`** — the full mod list. Each entry pins its exact tested build via
  `filename` (resolved to a Modrinth download link + hashes); an entry with no
  `filename` (e.g. Continuity) resolves by `version`, or by the newest Fabric
  build for the MC if `version` is null. `note` is the human name.
- **`resourcepacks`** / **`shaderpacks`** / **`datapacks`** — `{slug, version}`
  lists pulled from Modrinth. Their Modrinth loader is `minecraft` / `iris` /
  `datapack` (**not** `fabric`), so the builder relaxes the loader filter for
  them. Resource packs → `resourcepacks/`, shaders → `shaderpacks/`, datapacks →
  each bundled world's `datapacks/` (so datapacks require `--with-map`).
  *AllTheMons* is a combined resource-pack + datapack `.zip`, so it is listed in
  **both** `resourcepacks` (client models) and `datapacks` (species data) and
  installs to both locations.

## Staging subfolders (untracked contents)

Drop files here for content **not** on Modrinth; it bundles into the pack as
overrides:

- **`resourcepacks/`** → `overrides/resourcepacks/`
- **`shaderpacks/`** → `overrides/shaderpacks/`
- **`datapacks/`** → each bundled world's `datapacks/` (needs `--with-map`)
- **`maps/`** → world staging (see its README); drop a world folder in and it
  bundles into `overrides/saves/` with `--with-map`.
- **`overrides/`** → copied verbatim into the pack's `overrides/` root, for
  instance-root files: **`options.txt`** (keybinds / video / audio settings),
  a `config/` folder, `servers.dat`, etc. Unlike the folders above, this one's
  contents are **tracked** (small, curated pack config) — drop
  `mrpack/overrides/options.txt` here and it ships as `overrides/options.txt`.
  **Instance logo:** the `.mrpack` format has no icon field, but dropping a square
  PNG named exactly `mrpack/overrides/icon.png` ships it as `overrides/icon.png`,
  which Prism Launcher 10.0.0+ auto-applies as the instance icon on local import
  (older launchers ignore it harmlessly; for Modrinth browser/App installs, set the
  icon on the Modrinth project page instead).

Always bundled automatically: this mod's own jar → `overrides/mods/`.

To add another staging subfolder, mirror the `.gitignore` pattern
(`/mrpack/<sub>/*` + `!/mrpack/<sub>/.gitkeep`).

## Recommended launcher JVM args (set per-instance, NOT shipped in the pack)

The `.mrpack` format can't carry JVM args, so set these in your launcher's instance
Java settings after import — **Prism**: Instance → Edit → Settings → Java → check
"Override Java arguments"; **Modrinth App**: profile → Options → Java arguments.

Distant Horizons recommends the low-pause **generational ZGC** collector: DH keeps a
large heap plus off-heap LOD buffers, and the default G1 collector's pauses show up as
frametime stutter — exactly what a stream can't have. On Java 21 (MC 1.21.1's runtime):

    -XX:+UseZGC -XX:+ZGenerational -Xms8G -Xmx8G -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem

- `-XX:+UseZGC -XX:+ZGenerational` — generational ZGC (opt-in on Java 21). If you ever
  run this on Java 23+, DROP `+ZGenerational` (generational is the default there and the
  flag is removed/deprecated).
- `-Xms8G -Xmx8G` — fixed 8 GB heap (equal Xms/Xmx avoids resize pauses; pairs with
  AlwaysPreTouch). Bump to 10–12 GB if you have RAM headroom, but DH's LOD data is mostly
  off-heap / in VRAM, so a giant heap isn't needed just for the 512-chunk view distance —
  leave plenty of system RAM free for the OS and the game's direct/off-heap memory.
- `-XX:+AlwaysPreTouch` — commits + pre-faults the heap at launch for steady frametimes
  (no lazy page-fault stutter mid-stream); costs a slightly slower boot.
- `-XX:+PerfDisableSharedMem` — avoids JVM perf-data file I/O stalls.

Do **NOT** add `-XX:+DisableExplicitGC` (it ships in Aikar's flags): Distant Horizons
calls explicit GC to free native LOD buffers, so disabling it can leak native memory.
