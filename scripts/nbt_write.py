#!/usr/bin/env python3
"""nbt_write — minimal binary-NBT writer, the inverse of scripts/nbt_read.py.

Serializes the exact tree shape nbt_read produces — (tag_id, value) with
COMPOUND = dict[name -> (tag, payload)], LIST = (item_tag, [payloads]) — back to
(optionally gzipped) binary NBT. Round-trip safe for level.dat editing:

    from nbt_read import read_nbt_file
    from nbt_write import write_nbt_file
    name, tree = read_nbt_file("level.dat")
    ...mutate tree...
    write_nbt_file("level.dat", name, tree, gzipped=True)
"""

import gzip
import struct

from nbt_read import (
    TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE,
    TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND,
    TAG_INT_ARRAY, TAG_LONG_ARRAY, TAG_END,
)


def _string(out: bytearray, s: str) -> None:
    data = s.encode("utf-8")
    out += struct.pack(">H", len(data))
    out += data


def _payload(out: bytearray, tag: int, value) -> None:
    if tag == TAG_BYTE:
        out += struct.pack(">b", value)
    elif tag == TAG_SHORT:
        out += struct.pack(">h", value)
    elif tag == TAG_INT:
        out += struct.pack(">i", value)
    elif tag == TAG_LONG:
        out += struct.pack(">q", value)
    elif tag == TAG_FLOAT:
        out += struct.pack(">f", value)
    elif tag == TAG_DOUBLE:
        out += struct.pack(">d", value)
    elif tag == TAG_BYTE_ARRAY:
        out += struct.pack(">i", len(value))
        for v in value:
            out += struct.pack(">b", v)
    elif tag == TAG_STRING:
        _string(out, value)
    elif tag == TAG_LIST:
        item_tag, items = value
        out.append(item_tag)
        out += struct.pack(">i", len(items))
        for item in items:
            _payload(out, item_tag, item)
    elif tag == TAG_COMPOUND:
        for name, (t, v) in value.items():
            out.append(t)
            _string(out, name)
            _payload(out, t, v)
        out.append(TAG_END)
    elif tag == TAG_INT_ARRAY:
        out += struct.pack(">i", len(value))
        for v in value:
            out += struct.pack(">i", v)
    elif tag == TAG_LONG_ARRAY:
        out += struct.pack(">i", len(value))
        for v in value:
            out += struct.pack(">q", v)
    else:
        raise ValueError(f"unknown NBT tag {tag}")


def to_bytes(root_name: str, tree) -> bytes:
    tag, value = tree
    if tag != TAG_COMPOUND:
        raise ValueError("root must be a compound")
    out = bytearray()
    out.append(TAG_COMPOUND)
    _string(out, root_name)
    _payload(out, TAG_COMPOUND, value)
    return bytes(out)


def write_nbt_file(path: str, root_name: str, tree, gzipped: bool = True) -> None:
    raw = to_bytes(root_name, tree)
    if gzipped:
        raw = gzip.compress(raw)
    with open(path, "wb") as fh:
        fh.write(raw)
