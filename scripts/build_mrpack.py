#!/usr/bin/env python3
"""Build a Modrinth .mrpack for The Cobblemon Initiative / UPM 2.

Reads mrpack/modpack.json (the pack manifest) and resolves every mod / resource
pack / shader / datapack to its Modrinth download URL + hashes (no jar/zip
redistribution), bundles this mod's own jar plus anything staged under
mrpack/{resourcepacks,shaderpacks,datapacks} (and, with --with-map, the UPM 2
world) as overrides, and zips the result to dist/<name>-<version>.mrpack.

`mods` pins each mod to its exact tested build via `filename`; an entry with no
`filename` (e.g. Continuity) resolves by `version` — or the newest build for the
target MC when `version` is null. Content that is not a Fabric mod (resource
packs, shaders, datapacks) is NOT tagged with the "fabric" loader on Modrinth —
resource packs use "minecraft", shaders "iris"/"optifine", datapacks
"datapack" — so the loader filter is relaxed for those categories. Datapacks are
placed into each bundled world's datapacks/ folder, so they require --with-map.
AllTheMons is a combined resource-pack + datapack .zip, so it is listed in BOTH
`resourcepacks` (client models) and `datapacks` (species data) and installs to
both locations.

Run from the repo root (the `build-mrpack` dev-shell command handles that).
"""
import argparse
import glob
import json
import os
import re
import shutil
import subprocess
import tempfile
import urllib.request
import zipfile

MODRINTH_API = "https://api.modrinth.com/v2"
UA = "the-cobblemon-initiative-mrpack-builder/1.0 (+https://github.com/thecompanyinc)"
MRPACK_DIR = "mrpack"
MANIFEST = os.path.join(MRPACK_DIR, "modpack.json")

# Resource packs and shaders are client-side only; declaring it keeps dedicated-server
# installs from pulling client assets. (This pack is single-player, so it is hygiene.)
CLIENT_ONLY = {"client": "required", "server": "unsupported"}


def http_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


class Modrinth:
    """Memoized access to Modrinth version lists — one fetch per project per build."""

    def __init__(self):
        self._cache = {}

    def versions(self, slug):
        if slug not in self._cache:
            self._cache[slug] = http_json(f"{MODRINTH_API}/project/{slug}/version")  # newest-first
        return self._cache[slug]


def choose_version(versions, slug, pin, mc, require_fabric):
    """Pick a version: the pinned version_number if given, else the newest build
    matching `mc` (and the Fabric loader, for mods)."""
    if pin:
        matches = [v for v in versions if v.get("version_number") == pin]
        if not matches:
            avail = ", ".join(sorted({v.get("version_number", "?") for v in versions})[:12])
            raise SystemExit(f"[{slug}] pinned version '{pin}' not found on Modrinth.\n  Available: {avail} ...")
        # Prefer a pinned match that also fits mc + loader; otherwise trust the pin.
        pref = [v for v in matches
                if mc in v.get("game_versions", []) and (not require_fabric or "fabric" in v.get("loaders", []))]
        return (pref or matches)[0]
    cands = [v for v in versions if mc in v.get("game_versions", [])]
    if require_fabric:
        cands = [v for v in cands if "fabric" in v.get("loaders", [])]
    if not cands:
        raise SystemExit(f"[{slug}] no {'Fabric ' if require_fabric else ''}build for MC {mc} on Modrinth.")
    return cands[0]


def primary_file(v, slug):
    files = v.get("files", [])
    f = next((x for x in files if x.get("primary")), files[0] if files else None)
    if not f:
        raise SystemExit(f"[{slug}] version {v.get('version_number')} has no downloadable file.")
    return f


def file_entry(f, path_prefix, env=None):
    entry = {
        "path": path_prefix + f["filename"],
        "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
        "downloads": [f["url"]],
        "fileSize": f["size"],
    }
    if env:
        entry["env"] = env
    return entry


def find_file_by_name(versions, filename):
    """The Modrinth file object with exactly this filename, across all versions."""
    for v in versions:
        for f in v.get("files", []):
            if f.get("filename") == filename:
                return f
    return None


def resolve_mod(mr, e, mc):
    """Resolve a `mods` entry to a mods/ files[] entry: by exact filename if pinned,
    else by version_number (newest Fabric build if null)."""
    versions = mr.versions(e["slug"])
    if e.get("filename"):
        f = find_file_by_name(versions, e["filename"])
        if f is None:
            # The exact tested file is gone from Modrinth (yanked/renamed) — fall back
            # to the newest Fabric build for this MC and flag it.
            v = choose_version(versions, e["slug"], None, mc, require_fabric=True)
            f = primary_file(v, e["slug"])
            print(f"    [warn] {e.get('note', e['slug'])}: '{e['filename']}' not on Modrinth; using {f['filename']}")
        return e.get("version") or "", file_entry(f, "mods/")
    v = choose_version(versions, e["slug"], e.get("version"), mc, require_fabric=True)
    return v.get("version_number"), file_entry(primary_file(v, e["slug"]), "mods/")


def resolve_pack(mr, item, mc, path_prefix, env=None):
    """Resolve a resource pack / shader / (single) datapack entry to a files[] entry.
    Loader filter is relaxed — these are not tagged 'fabric' on Modrinth."""
    versions = mr.versions(item["slug"])
    v = choose_version(versions, item["slug"], item.get("version"), mc, require_fabric=False)
    return v.get("version_number"), primary_file(v, item["slug"])


def read_setting(path, pattern, default=None):
    try:
        with open(path) as fh:
            m = re.search(pattern, fh.read())
            return m.group(1) if m else default
    except OSError:
        return default


def find_mod_jar():
    jars = [
        j for j in glob.glob("build/libs/*.jar")
        if not re.search(r"-(sources|dev|shadow)\.jar$", j)
    ]
    if not jars:
        raise SystemExit("No mod jar in build/libs — run `gradle build` first (or omit --skip-build).")
    return max(jars, key=os.path.getmtime)


def staged_entries(subdir):
    """Non-.gitkeep files/folders staged under mrpack/<subdir>/."""
    return [
        e for e in sorted(glob.glob(os.path.join(MRPACK_DIR, subdir, "*")))
        if os.path.basename(e) != ".gitkeep"
    ]


def copy_into(entry, dest_dir):
    os.makedirs(dest_dir, exist_ok=True)
    dst = os.path.join(dest_dir, os.path.basename(entry))
    if os.path.isdir(entry):
        shutil.copytree(entry, dst)
    else:
        shutil.copy2(entry, dst)


def main():
    ap = argparse.ArgumentParser(description="Build a Modrinth .mrpack for UPM 2.")
    ap.add_argument("--name")
    ap.add_argument("--version")
    ap.add_argument("--with-map", action="store_true",
                    help="bundle world folder(s) from the map dir into overrides/saves/")
    ap.add_argument("--map-dir", default="mrpack/maps")
    ap.add_argument("--out-dir", default="dist")
    ap.add_argument("--skip-build", action="store_true", help="do not run `gradle build` first")
    args = ap.parse_args()

    if not os.path.isfile(MANIFEST):
        raise SystemExit(f"Missing {MANIFEST} — this is the pack manifest (metadata + content lists).")
    with open(MANIFEST) as fh:
        cfg = json.load(fh)

    mc = cfg.get("minecraft") or read_setting("build.gradle.kts", r"minecraft:([0-9][^\"\s]*)", "1.21.1")
    loader_ver = cfg.get("fabricLoader") or read_setting("build.gradle.kts", r"fabric-loader:([0-9][^\"\s]*)", "0.17.2")
    name = args.name or cfg.get("name") or read_setting("settings.gradle.kts", r'rootProject\.name\s*=\s*"([^"]+)"', "modpack")
    slug_name = read_setting("settings.gradle.kts", r'rootProject\.name\s*=\s*"([^"]+)"', "modpack")
    version = args.version or read_setting("build.gradle.kts", r'(?m)^version\s*=\s*"([^"]+)"', "0.0.0")
    summary = cfg.get("summary", name)

    mods = cfg.get("mods") or []
    resourcepacks = cfg.get("resourcepacks") or []
    shaderpacks = cfg.get("shaderpacks") or []
    datapacks = cfg.get("datapacks") or []
    if not mods:
        raise SystemExit(f"{MANIFEST} lists no mods.")

    mr = Modrinth()

    if not args.skip_build:
        print("Building mod jar (gradle build)...")
        subprocess.run(["gradle", "build"], check=True)
    mod_jar = find_mod_jar()
    print(f"Bundling own mod: {os.path.basename(mod_jar)}")

    # Which world folder(s) will be bundled — needed before building files[] so
    # Modrinth datapacks can be pathed into each world's datapacks/.
    worlds = []
    if args.with_map:
        worlds = [d for d in sorted(glob.glob(os.path.join(args.map_dir, "*"))) if os.path.isdir(d)]
        if not worlds:
            print(f"  [warn] --with-map: no world folders in {args.map_dir}/ — skipping map + Modrinth datapacks.")
    world_names = [os.path.basename(w) for w in worlds]

    files = []

    # ── Mods (fabric) ───────────────────────────────────────────────────────────
    print(f"Resolving {len(mods)} mods from Modrinth (fabric / mc {mc}):")
    for e in sorted(mods, key=lambda m: (m.get("note") or m["slug"]).lower()):
        ver, entry = resolve_mod(mr, e, mc)
        files.append(entry)
    print(f"  [OK] {len(mods)} mods resolved")

    # ── Resource packs (client-side; loader is 'minecraft', not 'fabric') ───────
    for rp in resourcepacks:
        ver, f = resolve_pack(mr, rp, mc, "resourcepacks/")
        print(f"  resourcepack {rp['slug']:18s} {ver}")
        files.append(file_entry(f, "resourcepacks/", env=CLIENT_ONLY))

    # ── Shader packs (client-side; loaders are 'iris'/'optifine') ───────────────
    for sp in shaderpacks:
        ver, f = resolve_pack(mr, sp, mc, "shaderpacks/")
        print(f"  shaderpack   {sp['slug']:18s} {ver}")
        files.append(file_entry(f, "shaderpacks/", env=CLIENT_ONLY))

    # ── Datapacks (attach to a world; loaders are 'datapack'/'minecraft') ───────
    if datapacks:
        if world_names:
            for dp in datapacks:
                ver, f = resolve_pack(mr, dp, mc, "datapacks/")
                print(f"  datapack     {dp['slug']:18s} {ver} -> saves/{{{','.join(world_names)}}}/datapacks/")
                for wname in world_names:
                    files.append(file_entry(f, f"saves/{wname}/datapacks/"))
        else:
            print(f"  [warn] {len(datapacks)} datapack(s) configured but no world bundled — "
                  "datapacks attach to a world; re-run with --with-map.")

    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version,
        "name": name,
        "summary": summary,
        "files": files,
        "dependencies": {"minecraft": mc, "fabric-loader": loader_ver},
    }

    os.makedirs(args.out_dir, exist_ok=True)
    out_path = os.path.join(args.out_dir, f"{slug_name}-{version}.mrpack")

    with tempfile.TemporaryDirectory() as tmp:
        overrides = os.path.join(tmp, "overrides")
        with open(os.path.join(tmp, "modrinth.index.json"), "w") as fh:
            json.dump(index, fh, indent=2)

        copy_into(mod_jar, os.path.join(overrides, "mods"))

        # Verbatim passthrough: mrpack/overrides/ -> the pack's overrides/ root, for
        # instance-root files not on Modrinth — e.g. options.txt (keybinds / video /
        # audio settings), a config/ folder, servers.dat. Merges into existing dirs.
        for e in staged_entries("overrides"):
            rel = os.path.basename(e)
            print(f"  override: {rel}")
            dst = os.path.join(overrides, rel)
            if os.path.isdir(e):
                shutil.copytree(e, dst, dirs_exist_ok=True)
            else:
                os.makedirs(overrides, exist_ok=True)
                shutil.copy2(e, dst)

        # Locally-staged packs (things not on Modrinth) -> overrides/<sub>/.
        for sub in ("resourcepacks", "shaderpacks"):
            for e in staged_entries(sub):
                print(f"  staged {sub[:-1]}: {os.path.basename(e)}")
                copy_into(e, os.path.join(overrides, sub))

        # World(s) -> overrides/saves/.
        for w in worlds:
            wname = os.path.basename(w)
            print(f"  world: {wname}")
            shutil.copytree(w, os.path.join(overrides, "saves", wname),
                            symlinks=False, ignore=shutil.ignore_patterns("session.lock"))

        # Locally-staged datapacks -> each bundled world's datapacks/.
        staged_dp = staged_entries("datapacks")
        if staged_dp:
            if world_names:
                for wname in world_names:
                    for e in staged_dp:
                        copy_into(e, os.path.join(overrides, "saves", wname, "datapacks"))
                print(f"  staged datapacks: {len(staged_dp)} -> saves/{{{','.join(world_names)}}}/datapacks/")
            else:
                print(f"  [warn] {len(staged_dp)} staged datapack(s) not bundled — re-run with --with-map.")

        # strict_timestamps=False: world files can carry mtimes outside ZIP's
        # 1980..2107 range (epoch/zero mtimes on region data); clamp instead of crash.
        with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED, strict_timestamps=False) as z:
            for root, _dirs, fnames in os.walk(tmp):
                for fn in fnames:
                    full = os.path.join(root, fn)
                    z.write(full, os.path.relpath(full, tmp))

    size_mb = os.path.getsize(out_path) / 1e6
    print(f"\nWrote {out_path} ({size_mb:.1f} MB) — {len(files)} indexed files + this mod bundled.")
    if not args.with_map:
        print(f"(No map bundled. Use --with-map to include world folders from ./{args.map_dir}/.)")


if __name__ == "__main__":
    main()
