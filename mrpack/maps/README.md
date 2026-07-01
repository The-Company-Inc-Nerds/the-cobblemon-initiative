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
