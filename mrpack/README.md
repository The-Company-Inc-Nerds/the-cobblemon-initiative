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

Always bundled automatically: this mod's own jar → `overrides/mods/`.

To add another staging subfolder, mirror the `.gitignore` pattern
(`/mrpack/<sub>/*` + `!/mrpack/<sub>/.gitkeep`).
