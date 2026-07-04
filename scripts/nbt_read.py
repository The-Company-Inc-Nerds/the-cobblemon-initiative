#!/usr/bin/env python3
"""nbt_read — minimal binary-NBT reader emitting SNBT text.

Reads a (optionally gzipped) NBT file — e.g. Easy NPC's per-NPC world storage under
<world>/easy_npc/npcs/<uuid>.npc.nbt — and converts it to SNBT text that
scripts/snbt_merge.py can parse. Used by content_compile to merge builder-authored
NPC data (skins, models, names) with the compiled quest presets.

Usage:
    python3 scripts/nbt_read.py <file.nbt>          # dump SNBT to stdout
Library:
    from nbt_read import read_nbt_file, to_snbt
    root_name, tree = read_nbt_file(path)           # tree is (tag_id, python value)
    text = to_snbt(tree)
"""

import gzip
import struct
import sys

TAG_END, TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE = range(7)
TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND = 7, 8, 9, 10
TAG_INT_ARRAY, TAG_LONG_ARRAY = 11, 12


class _Reader:
    def __init__(self, buf: bytes):
        self.buf = buf
        self.pos = 0

    def take(self, n):
        b = self.buf[self.pos:self.pos + n]
        self.pos += n
        return b

    def u8(self):  return self.take(1)[0]
    def i8(self):  return struct.unpack(">b", self.take(1))[0]
    def i16(self): return struct.unpack(">h", self.take(2))[0]
    def u16(self): return struct.unpack(">H", self.take(2))[0]
    def i32(self): return struct.unpack(">i", self.take(4))[0]
    def i64(self): return struct.unpack(">q", self.take(8))[0]
    def f32(self): return struct.unpack(">f", self.take(4))[0]
    def f64(self): return struct.unpack(">d", self.take(8))[0]
    def string(self): return self.take(self.u16()).decode("utf-8", "replace")


def _payload(r: _Reader, tag: int):
    if tag == TAG_BYTE:   return r.i8()
    if tag == TAG_SHORT:  return r.i16()
    if tag == TAG_INT:    return r.i32()
    if tag == TAG_LONG:   return r.i64()
    if tag == TAG_FLOAT:  return r.f32()
    if tag == TAG_DOUBLE: return r.f64()
    if tag == TAG_BYTE_ARRAY: return [r.i8() for _ in range(r.i32())]
    if tag == TAG_STRING: return r.string()
    if tag == TAG_LIST:
        item_tag = r.u8()
        return (item_tag, [_payload(r, item_tag) for _ in range(r.i32())])
    if tag == TAG_COMPOUND:
        out = {}
        while True:
            t = r.u8()
            if t == TAG_END:
                return out
            name = r.string()
            out[name] = (t, _payload(r, t))
    if tag == TAG_INT_ARRAY:  return [r.i32() for _ in range(r.i32())]
    if tag == TAG_LONG_ARRAY: return [r.i64() for _ in range(r.i32())]
    raise ValueError(f"unknown NBT tag {tag} at offset {r.pos}")


def read_nbt_file(path: str):
    """Returns (root_name, (TAG_COMPOUND, dict))."""
    raw = open(path, "rb").read()
    if raw[:2] == b"\x1f\x8b":
        raw = gzip.decompress(raw)
    r = _Reader(raw)
    tag = r.u8()
    if tag != TAG_COMPOUND:
        raise ValueError(f"root tag is {tag}, expected compound")
    name = r.string()
    return name, (TAG_COMPOUND, _payload(r, TAG_COMPOUND))


# ─── SNBT emission (matches vanilla SNBT; parseable by snbt_merge) ─────────────

_SAFE_KEY = set("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.+")


def _q(s: str) -> str:
    """Quote an SNBT string, preferring double quotes; falls back to single quotes
    when the payload itself contains double quotes (Easy NPC json-text names)."""
    if '"' in s and "'" not in s:
        return "'" + s.replace("\\", "\\\\") + "'"
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def _key(k: str) -> str:
    return k if k and all(c in _SAFE_KEY for c in k) else _q(k)


def _f32s(v: float) -> str:
    # compact float repr; struct round-trip keeps the stored precision
    s = repr(v)
    return s + "f"


def to_snbt(node) -> str:
    tag, v = node
    if tag == TAG_BYTE:   return f"{v}b"
    if tag == TAG_SHORT:  return f"{v}s"
    if tag == TAG_INT:    return str(v)
    if tag == TAG_LONG:   return f"{v}L"
    if tag == TAG_FLOAT:  return _f32s(v)
    if tag == TAG_DOUBLE: return f"{v!r}d"
    if tag == TAG_STRING: return _q(v)
    if tag == TAG_BYTE_ARRAY: return "[B;" + ",".join(f"{x}b" for x in v) + "]"
    if tag == TAG_INT_ARRAY:  return "[I;" + ",".join(str(x) for x in v) + "]"
    if tag == TAG_LONG_ARRAY: return "[L;" + ",".join(f"{x}L" for x in v) + "]"
    if tag == TAG_LIST:
        item_tag, items = v
        return "[" + ",".join(to_snbt((item_tag, it)) for it in items) + "]"
    if tag == TAG_COMPOUND:
        return "{" + ",".join(f"{_key(k)}:{to_snbt(sub)}" for k, sub in v.items()) + "}"
    raise ValueError(f"unknown tag {tag}")


if __name__ == "__main__":
    name, tree = read_nbt_file(sys.argv[1])
    sys.stderr.write(f"# root name: {name!r}\n")
    print(to_snbt(tree))
