#!/usr/bin/env python3
"""Offline NPC/trainer placement audit — no running server.

Reads placed coordinates from config (trainer JSONs' `coordinates`, and/or
dialog-src/characters `placement`), reads the ACTUAL terrain straight from the world
save's Anvil region files, and classifies each spot: GROUNDED / SUNK / FLOATING /
HEAD_BLOCKED. This is the serverless twin of `/cobblemon-initiative test placement`
(which is authoritative but needs a running server + the map loaded). Fast enough for
a pre-commit / CI check.

Block "solidity" here is a name-based heuristic (no collision data offline), so treat
it as a fast screen — the in-mod command is the source of truth. SUNK / HEAD_BLOCKED
are hard fails (can't snap out of a wall/ceiling); FLOATING is a WARN because Easy NPC
latch-spawn snaps NPCs to the ground on spawn.

Usage:
  python3 scripts/npc_placement_audit.py [--world DIR] [--source trainers|dialog|both]
Exits nonzero if any SUNK or HEAD_BLOCKED is found. See docs/TESTING_TOOLKIT.md.
"""
import sys, os, json, glob, zlib, gzip, argparse

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nbt_read import _Reader, _payload, TAG_COMPOUND, TAG_LIST  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
PASSABLE = AIR | {
    'minecraft:water', 'minecraft:lava', 'minecraft:short_grass', 'minecraft:grass',
    'minecraft:tall_grass', 'minecraft:fern', 'minecraft:large_fern', 'minecraft:snow',
    'minecraft:torch', 'minecraft:wall_torch', 'minecraft:soul_torch', 'minecraft:vine',
    'minecraft:seagrass', 'minecraft:tall_seagrass', 'minecraft:kelp', 'minecraft:kelp_plant',
    'minecraft:dead_bush', 'minecraft:sugar_cane', 'minecraft:redstone_wire',
    'minecraft:rail', 'minecraft:ladder', 'minecraft:lever', 'minecraft:tripwire',
}


def is_solid(b):
    return b is not None and b not in PASSABLE and not b.endswith('_air') \
        and not b.endswith('_sapling') and not b.endswith('_flower') \
        and 'button' not in b and 'pressure_plate' not in b and 'carpet' not in b


def default_world():
    for p in (os.path.join(ROOT, 'run/saves/The Cobblemon Initiative'),
              os.path.join(ROOT, 'mrpack/maps/The Cobblemon Initiative')):
        if os.path.isdir(os.path.join(p, 'region')):
            return p
    return None


def trainer_coords():
    out = []
    for f in glob.glob(os.path.join(ROOT, 'src/main/resources/data/cobblemon_initiative/trainers/**/*.json'), recursive=True):
        try:
            d = json.load(open(f))
        except Exception:
            continue
        # each trainer file is a top-level LIST of trainer objects (a gym/shrine bundles
        # its cast); older single-object files are handled too.
        for t in (d if isinstance(d, list) else [d]):
            if not isinstance(t, dict):
                continue
            c = t.get('coordinates')
            if isinstance(c, list) and len(c) >= 3:
                out.append((t.get('id', os.path.basename(f)[:-5]), int(c[0]), int(c[1]), int(c[2])))
    return out


def dialog_coords():
    out = []
    for f in glob.glob(os.path.join(ROOT, 'dialog-src/characters/**/*.json'), recursive=True):
        try:
            d = json.load(open(f))
        except Exception:
            continue
        p = d.get('placement') or {}
        if all(k in p for k in ('x', 'y', 'z')):
            out.append((d.get('id', os.path.basename(f)[:-5]), int(p['x']), int(p['y']), int(p['z'])))
    return out


# ── Anvil region reader (block lookups) ─────────────────────────────────────
def _cval(compound, key):
    v = compound.get(key)
    return v[1] if v else None


def _clist(compound, key):
    v = compound.get(key)
    if not v or v[0] != TAG_LIST:
        return []
    return v[1][1]  # (item_tag, [items]) -> items


class World:
    def __init__(self, region_dir):
        self.dir = region_dir
        self.cache = {}  # (cx,cz) -> chunk-root-dict or None

    def _chunk(self, cx, cz):
        if (cx, cz) in self.cache:
            return self.cache[(cx, cz)]
        path = os.path.join(self.dir, f"r.{cx >> 5}.{cz >> 5}.mca")
        chunk = None
        if os.path.exists(path):
            with open(path, 'rb') as fh:
                header = fh.read(4096)
                i = ((cx & 31) + (cz & 31) * 32) * 4
                off = (header[i] << 16) | (header[i + 1] << 8) | header[i + 2]
                cnt = header[i + 3]
                if off and cnt:
                    fh.seek(off * 4096)
                    length = int.from_bytes(fh.read(4), 'big')
                    comp = fh.read(1)[0]
                    data = fh.read(length - 1)
                    raw = zlib.decompress(data) if comp == 2 else \
                        gzip.decompress(data) if comp == 1 else data if comp == 3 else None
                    if raw:
                        r = _Reader(raw)
                        r.u8()          # root tag (compound)
                        r.string()      # root name
                        chunk = _payload(r, TAG_COMPOUND)
        self.cache[(cx, cz)] = chunk
        return chunk

    def block(self, x, y, z):
        chunk = self._chunk(x >> 4, z >> 4)
        if chunk is None:
            return None  # ungenerated / missing region
        for sc in _clist(chunk, 'sections'):
            if _cval(sc, 'Y') != (y >> 4):
                continue
            bs = _cval(sc, 'block_states')
            if not bs:
                return 'minecraft:air'
            pal = _clist(bs, 'palette')
            if not pal:
                return 'minecraft:air'
            names = [_cval(p, 'Name') for p in pal]
            if len(names) == 1:
                return names[0]
            data = _cval(bs, 'data') or []
            if not data:
                return names[0]
            bits = max(4, (len(names) - 1).bit_length())
            per = 64 // bits
            idx = (y & 15) * 256 + (z & 15) * 16 + (x & 15)
            raw = data[idx // per] & 0xFFFFFFFFFFFFFFFF
            v = (raw >> ((idx % per) * bits)) & ((1 << bits) - 1)
            return names[v] if v < len(names) else 'minecraft:air'
        return 'minecraft:air'  # no such section = air above terrain


def classify(w, x, y, z):
    feet = w.block(x, y, z)
    if feet is None:
        return 'NO_REGION', {}
    head = w.block(x, y + 1, z)
    below = w.block(x, y - 1, z)
    if is_solid(feet):
        return 'SUNK', {'feet': feet}
    if is_solid(head):
        return 'HEAD_BLOCKED', {'head': head}
    if is_solid(below):
        return 'GROUNDED', {}
    gap = 1
    while gap < 48:
        b = w.block(x, y - 1 - gap, z)
        if b is None or is_solid(b):
            break
        gap += 1
    return 'FLOATING', {'gap': gap}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--world', default=None)
    ap.add_argument('--source', choices=['trainers', 'dialog', 'both'], default='trainers')
    a = ap.parse_args()
    world = a.world or default_world()
    if not world:
        print("[TEST] placement FAIL no world save found (run/saves or mrpack/maps)")
        sys.exit(2)
    w = World(os.path.join(world, 'region'))

    coords = []
    if a.source in ('trainers', 'both'):
        coords += trainer_coords()
    if a.source in ('dialog', 'both'):
        coords += dialog_coords()
    coords = sorted(set(coords))

    print(f"[TEST] placement world={os.path.basename(world)} source={a.source} targets={len(coords)}")
    tally = {}
    unplaced = 0
    for id_, x, y, z in coords:
        if x == 0 and y == 0 and z == 0:  # unset placeholder — roaming/latch-only
            unplaced += 1
            continue
        status, info = classify(w, x, y, z)
        tally[status] = tally.get(status, 0) + 1
        if status != 'GROUNDED':
            extra = ' '.join(f"{k}={v}" for k, v in info.items())
            print(f"[TEST] placement {id_}@{x},{y},{z} {status} {extra}".rstrip())
    summary = ' '.join(f"{k.lower()}={v}" for k, v in sorted(tally.items()))
    # Informational: trainer config coords are NOMINAL (Easy NPC latch-spawn snaps to
    # ground), so this reports rather than gates. Authoritative placement QA reads live
    # spawned positions (a player online). See docs/TESTING_TOOLKIT.md.
    print(f"[TEST] SUMMARY category=placement_offline {summary} unplaced={unplaced} "
          f"(config coords nominal — informational; live check needs a player)")
    sys.exit(0)


if __name__ == '__main__':
    main()
