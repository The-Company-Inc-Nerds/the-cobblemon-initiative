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
import hashlib
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

INSTALL_JSON = "src/main/resources/data/cobblemon_initiative/install.json"
DIFFICULTY_BYTE = {"peaceful": 0, "easy": 1, "normal": 2, "hard": 3}


def bake_install_into_level_dat(level_dat: str, level_name: str = None) -> None:
    """Pre-apply install.json's world-side settings to a bundled world COPY:
    gamerules (all NBT strings), difficulty, and the hardcore bit. Mirrors what
    `/cobblemon-initiative install run` does live; failures warn, never abort.
    `level_name` (from settings.json mapName) sets the in-game world display name."""
    try:
        import sys as _sys
        _sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from nbt_read import (read_nbt_file, TAG_COMPOUND, TAG_STRING, TAG_BYTE,
                              TAG_INT, TAG_FLOAT, TAG_DOUBLE, TAG_LIST)
        from nbt_write import write_nbt_file

        with open(INSTALL_JSON) as fh:
            install = json.load(fh)
        gamerules = dict(install.get("gamerules") or {})
        difficulty = gamerules.pop("_difficulty", None)
        hardcore = bool(install.get("hardcore"))

        root_name, tree = read_nbt_file(level_dat)
        data = tree[1].get("Data")
        if not data or data[0] != TAG_COMPOUND:
            print(f"    [warn] {level_dat}: no Data compound — skipping install bake.")
            return
        dtag = data[1]

        rules = dtag.get("GameRules")
        if not rules or rules[0] != TAG_COMPOUND:
            rules = (TAG_COMPOUND, {})
            dtag["GameRules"] = rules
        for rule, value in gamerules.items():
            rules[1][rule] = (TAG_STRING, str(value))

        if difficulty in DIFFICULTY_BYTE:
            dtag["Difficulty"] = (TAG_BYTE, DIFFICULTY_BYTE[difficulty])
        if hardcore:
            dtag["hardcore"] = (TAG_BYTE, 1)
            dtag["Difficulty"] = (TAG_BYTE, DIFFICULTY_BYTE["hard"])

        # Reset the host player. Builders send full saves they've PLAYED/TESTED on, so
        # Data.Player carries THEIR inventory, position, XP, effects (incl. the map's
        # old baked infinite speed — this replacement permanently supersedes that
        # strip), and even a party — and with playerdata/ empty the singleplayer host
        # INHERITS it. But DELETING the tag is also wrong: vanilla 1.21.1 ignores
        # SpawnY for a brand-new player (ServerPlayer's constructor calls
        # adjustSpawnLocation → PlayerRespawnLogic.getOverworldRespawnPos, which scans
        # DOWN from the MOTION_BLOCKING heightmap top of the spawn column), so a
        # tag-less host lands on the spawn house ROOF (y=122 instead of 109). Instead
        # REPLACE it with a minimal sanitized host tag built from scratch at this
        # level.dat's own SpawnX/Y/Z — PlayerList.load() then places the host exactly
        # there. Deliberately NO UUID key (Entity.load would adopt it) and no
        # Inventory/XP/effects/attributes/food — vanilla readers default all of those.
        # Pos MUST be 3 doubles (an empty/short list silently reads as 0,0,0).
        spawn = [dtag.get(k) for k in ("SpawnX", "SpawnY", "SpawnZ")]
        if all(t and t[0] == TAG_INT for t in spawn):
            sx, sy, sz = (float(t[1]) for t in spawn)
            angle = dtag.get("SpawnAngle")
            yaw = float(angle[1]) if angle else 0.0
            dv = dtag.get("DataVersion", (TAG_INT, 3955))[1]
            dtag["Player"] = (TAG_COMPOUND, {
                "Pos": (TAG_LIST, (TAG_DOUBLE, [sx + 0.5, sy, sz + 0.5])),
                "Rotation": (TAG_LIST, (TAG_FLOAT, [yaw, 0.0])),
                "Motion": (TAG_LIST, (TAG_DOUBLE, [0.0, 0.0, 0.0])),
                "Dimension": (TAG_STRING, "minecraft:overworld"),
                "playerGameType": (TAG_INT, 0),
                "DataVersion": (TAG_INT, dv),
            })
            print(f"    replaced Data.Player with sanitized host tag at spawn "
                  f"({sx + 0.5}, {sy}, {sz + 0.5}, yaw {yaw})")
        elif "Player" in dtag:
            # No baked spawn to anchor to — fall back to the old bare strip so the
            # builder's inventory/effects still never ship.
            del dtag["Player"]
            print(f"    [warn] {level_dat}: no Spawn X/Y/Z — removed Data.Player only "
                  "(host will take the vanilla heightmap spawn).")
        # Also force the world default gametype to survival.
        dtag["GameType"] = (TAG_INT, 0)

        if level_name:
            dtag["LevelName"] = (TAG_STRING, level_name)
            print(f"    set in-game world name → {level_name!r}")

        write_nbt_file(level_dat, root_name, tree, gzipped=True)
        print(f"    baked install: {len(gamerules)} gamerule(s)"
              f"{', difficulty=' + difficulty if difficulty else ''}"
              f"{', hardcore' if hardcore else ''} -> level.dat")
    except Exception as e:
        print(f"    [warn] install bake failed for {level_dat}: {e} — "
              f"run '/cobblemon-initiative install run' in-game instead.")


def strip_rctmod_trainers(datapack_zip: str) -> None:
    """UPM 2's bundled world datapack (datapacks/data.zip) ships 76 stale rctmod
    trainer JSONs (data/rctmod/trainers/*) that predate this pack — every world load
    logs ~83 'Model validation failure' warns and pollutes the trainer registry (our
    real trainers ship in the mod jar). Rewrite the bundled COPY without them; the
    staged source zip (and data.zip.backup) stay untouched. Failures warn, never abort."""
    try:
        with zipfile.ZipFile(datapack_zip) as zin:
            infos = zin.infolist()
            keep = [i for i in infos if not i.filename.startswith("data/rctmod/trainers/")]
            if len(keep) == len(infos):
                return
            payload = [(i, zin.read(i)) for i in keep]
        tmp = datapack_zip + ".tmp"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED, strict_timestamps=False) as zout:
            for info, data in payload:
                zout.writestr(info, data)
        os.replace(tmp, datapack_zip)
        print(f"    stripped {len(infos) - len(keep)} stale rctmod trainer entries "
              f"from {os.path.basename(datapack_zip)}")
    except Exception as e:
        print(f"    [warn] rctmod trainer strip failed for {datapack_zip}: {e}")


MANIFEST = os.path.join(MRPACK_DIR, "modpack.json")
# Human-facing pack settings (optional; overrides modpack.json / gradle / the map folder
# name). Keeps the display config out of the mods manifest. CLI flags still win over it.
SETTINGS_JSON = os.path.join(MRPACK_DIR, "settings.json")
# --cache: content-addressed local store of downloaded mod/pack jars (gitignored).
# Reused across builds so a rebuild+reinstall never re-downloads the unchanged deps.
CACHE_DIR = os.path.join(MRPACK_DIR, "cache")

# Per-player / session state that must NOT ship (players start fresh). Terrain (region),
# entities (incl. builder-placed NPCs), easy_npc/, poi/, data/ all stay. Applied when
# copying a bundled world so a builder's full-map export can be dropped in wholesale.
WORLD_USERDATA_STRIP = (
    "session.lock", "playerdata", "stats", "advancements",
    "cobblemonplayerdata", "cobbledollarsplayerdata", "pokedex",
    "pokemon",  # Cobblemon per-player PC + party storage (pcstore / playerpartystore)
)

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


def file_entry(f, path_prefix, env=None, suffix=""):
    # suffix=".disabled": Fabric Loader only loads *.jar files, and Prism / the Modrinth
    # App both show mods/<name>.jar.disabled as a disabled mod with a one-click enable
    # toggle — the mechanism for shipping a mod off-by-default (e.g. JEI).
    entry = {
        "path": path_prefix + f["filename"] + suffix,
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
    else by version_number (newest Fabric build if null). Entries with "disabled": true
    ship as mods/<name>.jar.disabled (off by default; one-click enable in the launcher)."""
    versions = mr.versions(e["slug"])
    suffix = ".disabled" if e.get("disabled") else ""
    if e.get("filename"):
        f = find_file_by_name(versions, e["filename"])
        if f is None:
            # The exact tested file is gone from Modrinth (yanked/renamed) — fall back
            # to the newest Fabric build for this MC and flag it.
            v = choose_version(versions, e["slug"], None, mc, require_fabric=True)
            f = primary_file(v, e["slug"])
            print(f"    [warn] {e.get('note', e['slug'])}: '{e['filename']}' not on Modrinth; using {f['filename']}")
        return e.get("version") or "", file_entry(f, "mods/", suffix=suffix)
    v = choose_version(versions, e["slug"], e.get("version"), mc, require_fabric=True)
    return v.get("version_number"), file_entry(primary_file(v, e["slug"]), "mods/", suffix=suffix)


def resolve_pack(mr, item, mc, path_prefix, env=None):
    """Resolve a resource pack / shader / (single) datapack entry to a files[] entry.
    Loader filter is relaxed — these are not tagged 'fabric' on Modrinth."""
    versions = mr.versions(item["slug"])
    v = choose_version(versions, item["slug"], item.get("version"), mc, require_fabric=False)
    return v.get("version_number"), primary_file(v, item["slug"])


def load_pack_settings():
    """Optional mrpack/settings.json — human-facing pack config (packName, packSummary,
    packVersion, packSlug, mapName, minecraft, fabricLoader). All keys optional; absent
    file → {}. Ignored keys starting with '_' (comments)."""
    try:
        with open(SETTINGS_JSON) as fh:
            return {k: v for k, v in json.load(fh).items() if not k.startswith("_")}
    except FileNotFoundError:
        return {}
    except Exception as e:
        print(f"  [warn] {SETTINGS_JSON}: {e} — ignoring.")
        return {}


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


def _sha1(path):
    h = hashlib.sha1()
    with open(path, "rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def cache_fetch(url, sha1, size):
    """Return a local path to the file with this sha1, downloading into CACHE_DIR
    (content-addressed) only if it isn't already cached + intact. Reused across builds."""
    os.makedirs(CACHE_DIR, exist_ok=True)
    cached = os.path.join(CACHE_DIR, sha1)
    if os.path.isfile(cached) and _sha1(cached) == sha1:
        return cached
    tmp = cached + ".part"
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=120) as r, open(tmp, "wb") as out:
        shutil.copyfileobj(r, out)
    got = _sha1(tmp)
    if got != sha1:
        os.remove(tmp)
        raise SystemExit(f"[cache] sha1 mismatch for {url}\n  expected {sha1}\n  got      {got}")
    os.replace(tmp, cached)
    return cached


def write_autoinstall_marker(config_dir):
    """Write the PACK-ONLY auto-install marker into a config/ dir. Its presence makes
    the mod run `/cobblemon-initiative install run` once per fresh world shortly after
    the first join. Shared with scripts/dev_sync so the dev run dir behaves like the
    installed pack. Returns the marker path."""
    marker = os.path.join(config_dir, "cobblemon-initiative-autoinstall.json")
    os.makedirs(config_dir, exist_ok=True)
    with open(marker, "w") as fh:
        json.dump({
            "enabled": True,
            "_comment": "Shipped by build_mrpack. The Cobblemon Initiative auto-runs "
                        "'/cobblemon-initiative install run' once per fresh world when this "
                        "file exists (world latch: data/cobblemon_initiative_autoinstall.json). "
                        "Delete this file or set enabled=false to go back to manual installs.",
        }, fh, indent=2)
    return marker


def bundle_cached_files(entries, overrides):
    """--cache: pull every manifest file entry into CACHE_DIR and copy it into the pack's
    overrides/<path>, so the installed pack is self-contained (launcher downloads nothing
    but our own jar). Returns count of bytes served from cache vs freshly downloaded."""
    from_cache = downloaded = 0
    for e in entries:
        sha1 = e["hashes"]["sha1"]
        already = os.path.isfile(os.path.join(CACHE_DIR, sha1))
        src = cache_fetch(e["downloads"][0], sha1, e.get("fileSize", 0))
        dst = os.path.join(overrides, *e["path"].split("/"))
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copy2(src, dst)
        if already:
            from_cache += 1
        else:
            downloaded += 1
    return from_cache, downloaded


def main():
    ap = argparse.ArgumentParser(description="Build a Modrinth .mrpack for UPM 2.")
    ap.add_argument("--name")
    ap.add_argument("--version")
    ap.add_argument("--with-map", action="store_true",
                    help="bundle world folder(s) from the map dir into overrides/saves/")
    ap.add_argument("--map-dir", default="mrpack/maps")
    ap.add_argument("--out-dir", default="dist")
    ap.add_argument("--skip-build", action="store_true", help="do not run `gradle build` first")
    ap.add_argument("--cache", action="store_true",
                    help="download every dep jar into mrpack/cache/ (content-addressed, "
                         "reused across builds) and BUNDLE them into overrides/mods etc. — "
                         "the installed pack downloads nothing but our own jar. First build "
                         "populates the cache; later builds reuse it.")
    args = ap.parse_args()

    if not os.path.isfile(MANIFEST):
        raise SystemExit(f"Missing {MANIFEST} — this is the pack manifest (metadata + content lists).")
    with open(MANIFEST) as fh:
        cfg = json.load(fh)

    # Precedence: CLI flag > mrpack/settings.json > modpack.json > gradle files > default.
    st = load_pack_settings()
    mc = st.get("minecraft") or cfg.get("minecraft") or read_setting("build.gradle.kts", r"minecraft:([0-9][^\"\s]*)", "1.21.1")
    loader_ver = st.get("fabricLoader") or cfg.get("fabricLoader") or read_setting("build.gradle.kts", r"fabric-loader:([0-9][^\"\s]*)", "0.17.2")
    name = args.name or st.get("packName") or cfg.get("name") or read_setting("settings.gradle.kts", r'rootProject\.name\s*=\s*"([^"]+)"', "modpack")
    slug_name = st.get("packSlug") or read_setting("settings.gradle.kts", r'rootProject\.name\s*=\s*"([^"]+)"', "modpack")
    version = args.version or st.get("packVersion") or read_setting("build.gradle.kts", r'(?m)^version\s*=\s*"([^"]+)"', "0.0.0")
    summary = st.get("packSummary") or cfg.get("summary") or name
    map_name = st.get("mapName")  # optional: renames the bundled world's folder + in-game name
    if st:
        print(f"  settings.json → name={name!r}, version={version}"
              + (f", mapName={map_name!r}" if map_name else ""))

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
    # Dest folder name per bundled world — settings.mapName renames it (folder + LevelName)
    # when exactly one world is bundled; otherwise the source folder name is kept.
    world_names = [os.path.basename(w) for w in worlds]
    if map_name and len(worlds) == 1:
        world_names = [map_name]
    elif map_name and len(worlds) > 1:
        print(f"  [warn] mapName set but {len(worlds)} worlds bundled — keeping folder names.")

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

    # --cache: bundle every dep into overrides (below) instead of listing it for the
    # launcher to download. The manifest then carries ZERO files[] (only our own jar,
    # which is always an override) — a self-contained pack.
    bundled = files if args.cache else []
    index = {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version,
        "name": name,
        "summary": summary,
        "files": [] if args.cache else files,
        "dependencies": {"minecraft": mc, "fabric-loader": loader_ver},
    }

    os.makedirs(args.out_dir, exist_ok=True)
    out_path = os.path.join(args.out_dir, f"{slug_name}-{version}.mrpack")

    with tempfile.TemporaryDirectory() as tmp:
        overrides = os.path.join(tmp, "overrides")
        with open(os.path.join(tmp, "modrinth.index.json"), "w") as fh:
            json.dump(index, fh, indent=2)

        copy_into(mod_jar, os.path.join(overrides, "mods"))

        # --cache: pull all deps from the local cache into overrides (self-contained pack).
        if bundled:
            print(f"Bundling {len(bundled)} dep(s) from {CACHE_DIR}/ (download-once cache):")
            reused, fresh = bundle_cached_files(bundled, overrides)
            print(f"  [OK] {reused} from cache, {fresh} downloaded → overrides/ "
                  f"(installed pack re-downloads nothing but our own jar)")

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

        # World(s) -> overrides/saves/, with the install command's WORLD-side effects
        # pre-baked into the COPY (never the source map): gamerules + difficulty +
        # hardcore from install.json land directly in level.dat, so a fresh pack
        # install opens ready — no `/cobblemon-initiative install run` needed for
        # those. (Config-side pieces ship as overrides below; NPC preset refresh and
        # sight registrations self-arm/self-seed in the mod on a fresh world.)
        for w, wname in zip(worlds, world_names):
            src_name = os.path.basename(w)
            print(f"  world: {src_name}" + (f" → {wname}" if wname != src_name else ""))
            wdst = os.path.join(overrides, "saves", wname)
            # Strip per-player/session state so a builder's full-map export can be dropped
            # in wholesale — players start fresh; terrain + entities + easy_npc/ ship.
            # dirs_exist_ok: under --cache the AllTheMons datapack was already bundled into
            # this world's datapacks/, so wdst exists — merge the world into it.
            shutil.copytree(w, wdst, symlinks=False, dirs_exist_ok=True,
                            ignore=shutil.ignore_patterns(*WORLD_USERDATA_STRIP))
            bake_install_into_level_dat(os.path.join(wdst, "level.dat"), level_name=wname)
            # level.dat.bak ships too (vanilla's corruption fallback) — bake it the
            # same way so the builder's Data.Player can't resurface via a .bak restore.
            if os.path.isfile(os.path.join(wdst, "level.dat.bak")):
                bake_install_into_level_dat(os.path.join(wdst, "level.dat.bak"), level_name=wname)
            # UPM 2's own world datapack carries stale rctmod trainers — clean the COPY.
            world_dp = os.path.join(wdst, "datapacks", "data.zip")
            if os.path.isfile(world_dp):
                strip_rctmod_trainers(world_dp)

        # Shop seed: the same badge_0 catalog `install run` copies at runtime, shipped
        # as a config override so the opening shop is right on first launch.
        shop_seed = "src/main/resources/cobbledollars_tiers/badge_0.json"
        if os.path.isfile(shop_seed):
            dst = os.path.join(overrides, "config", "cobbledollars", "default_shop.json")
            if not os.path.exists(dst):
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                shutil.copy2(shop_seed, dst)
                print("  config seed: cobbledollars/default_shop.json (badge_0 catalog)")

        # Auto-install marker: PACK-ONLY. Its presence makes the mod run
        # `/cobblemon-initiative install run` once per fresh world shortly after the
        # first join (zones + Map Frontiers — the pieces level.dat baking can't cover).
        # Bare-mod installs never have this file, so nothing auto-runs standalone.
        write_autoinstall_marker(os.path.join(overrides, "config"))
        print("  config seed: cobblemon-initiative-autoinstall.json (first-join auto-install)")

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
    if args.cache:
        print(f"\nWrote {out_path} ({size_mb:.1f} MB) — self-contained: {len(bundled)} deps "
              f"+ this mod bundled in overrides/, manifest files[] empty (installs offline).")
    else:
        print(f"\nWrote {out_path} ({size_mb:.1f} MB) — {len(files)} indexed files + this mod bundled.")
    if not args.with_map:
        print(f"(No map bundled. Use --with-map to include world folders from ./{args.map_dir}/.)")


if __name__ == "__main__":
    main()
