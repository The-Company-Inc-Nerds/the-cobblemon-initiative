#!/usr/bin/env python3
"""
snbt-merge — splice a keyed section from one SNBT file into another.

Usage:
  snbt-merge SOURCE KEY TARGET [options]

  SOURCE   SNBT file (or fragment) to read the snippet from
  KEY      Dot-separated key path in TARGET where the section will be written,
           e.g.  "data.DialogData"  or  "data.ActionData"
  TARGET   SNBT file to modify

Source file formats accepted:
  Full SNBT file   { ... }                     extract at KEY (or --src-key path)
  Bare value       { Type:"YES_NO", ... }       use with --src-key .
  Key-value frag   DialogData: { ... },         auto-detected; leading key is used

Options:
  -o, --out FILE      Output file (default: overwrite TARGET in-place; use - for stdout)
  -m, --mode MODE     replace (default) — overwrite the key entirely
                      merge            — deep-merge SOURCE compound into TARGET compound
  --src-key PATH      Key path inside SOURCE to extract (default: auto).
                      Use "." to take the entire SOURCE file as the value.
  --indent N          Spaces per indent level in output (default: 4)
  --compact           Write single-line SNBT (overrides --indent)

Examples:
  # Fragment snippet: DialogData: { ... },  → replaces data.DialogData in full preset
  snbt-merge dialog_snippet.snbt data.DialogData full_preset.snbt

  # Full preset → full preset, same path in both
  snbt-merge preset_a.snbt data.DialogData preset_b.snbt

  # Print to stdout instead of overwriting
  snbt-merge snippet.snbt data.DialogData preset.snbt -o -

  # Deep-merge source's ActionData into target's ActionData
  snbt-merge snippet.snbt data.ActionData preset.snbt --mode merge

  # Snippet file is just the bare value (no wrapper key)
  snbt-merge bare_value.snbt data.DialogData preset.snbt --src-key .

  # Extract from a different path in source than target
  snbt-merge other.snbt data.DialogData preset.snbt --src-key DialogData
"""

from __future__ import annotations
import argparse
import re
import sys
from collections import OrderedDict
from typing import Any


# ─────────────────────────────────────────────────────────────────────────────
# Tokenizer
# ─────────────────────────────────────────────────────────────────────────────

T_LBRACE   = 'LBRACE'    # {
T_RBRACE   = 'RBRACE'    # }
T_LBRACKET = 'LBRACKET'  # [
T_RBRACKET = 'RBRACKET'  # ]
T_COLON    = 'COLON'     # :
T_COMMA    = 'COMMA'     # ,
T_ARRPFX   = 'ARRPFX'    # B / I / L  (from [B;  [I;  [L;)
T_STRING   = 'STRING'    # "..." or '...'
T_NUMBER   = 'NUMBER'    # 1, 1b, 1.5f, -12L, …
T_WORD     = 'WORD'      # unquoted identifier: true, false, a_key, …

_NUM_RE    = re.compile(r'-?(?:\d+\.?\d*|\d*\.\d+)(?:[eE][+\-]?\d+)?[bBsSlLfFdD]?')
_ARRPFX_RE = re.compile(r'\[([BIL]);')
_WORD_RE   = re.compile(r'[^\s,:\[\]{}]+')


class Token:
    __slots__ = ('type', 'value', 'pos')

    def __init__(self, type_: str, value: str, pos: int = 0):
        self.type  = type_
        self.value = value
        self.pos   = pos

    def __repr__(self) -> str:
        return f'Token({self.type}, {self.value!r}@{self.pos})'


def tokenize(text: str) -> list[Token]:
    tokens: list[Token] = []
    i, n = 0, len(text)

    while i < n:
        c = text[i]

        # Whitespace (handles both comma- and newline-separated SNBT dialects)
        if c in ' \t\n\r':
            i += 1
            continue

        if c == '{':
            tokens.append(Token(T_LBRACE,   '{', i)); i += 1
        elif c == '}':
            tokens.append(Token(T_RBRACE,   '}', i)); i += 1
        elif c == ']':
            tokens.append(Token(T_RBRACKET, ']', i)); i += 1
        elif c == ':':
            tokens.append(Token(T_COLON,    ':', i)); i += 1
        elif c == ',':
            tokens.append(Token(T_COMMA,    ',', i)); i += 1

        elif c == '[':
            m = _ARRPFX_RE.match(text, i)
            if m:
                tokens.append(Token(T_LBRACKET, '[', i))
                tokens.append(Token(T_ARRPFX, m.group(1), i))
                i = m.end()
            else:
                tokens.append(Token(T_LBRACKET, '[', i)); i += 1

        elif c in '"\'':
            # Quoted string — preserve the raw form including outer quotes so
            # that e.g. '{"text":"Name"}' round-trips without modification.
            quote = c
            j = i + 1
            raw = [c]
            while j < n:
                ch = text[j]
                raw.append(ch)
                if ch == '\\' and j + 1 < n:
                    raw.append(text[j + 1])
                    j += 2
                elif ch == quote:
                    j += 1
                    break
                else:
                    j += 1
            tokens.append(Token(T_STRING, ''.join(raw), i))
            i = j

        elif c == '-' or c.isdigit():
            m = _NUM_RE.match(text, i)
            if m and (m.end() >= n or text[m.end()] in ' \t\n\r,}]'):
                tokens.append(Token(T_NUMBER, m.group(), i))
                i = m.end()
            else:
                m2 = _WORD_RE.match(text, i)
                tokens.append(Token(T_WORD, m2.group(), i))
                i = m2.end()

        else:
            m = _WORD_RE.match(text, i)
            if m:
                tokens.append(Token(T_WORD, m.group(), i))
                i = m.end()
            else:
                raise SyntaxError(f'Unexpected character {c!r} at offset {i}')

    return tokens


# ─────────────────────────────────────────────────────────────────────────────
# AST representation
#
#   compound  →  OrderedDict[str, node]          (preserves insertion order)
#   list      →  ('list', [node, …])
#   array     →  ('arr',  'B'|'I'|'L', [node])   typed NBT array
#   string    →  str                              raw quoted form e.g. '"hello"'
#   number    →  ('num',  '1.5f')                 raw text preserved
#   word      →  ('word', 'true')                 unquoted identifier
# ─────────────────────────────────────────────────────────────────────────────

SNBTNode = Any


def _is_primitive(node: SNBTNode) -> bool:
    return isinstance(node, str) or (isinstance(node, tuple) and node[0] in ('num', 'word'))


# ─────────────────────────────────────────────────────────────────────────────
# Parser
# ─────────────────────────────────────────────────────────────────────────────

class Parser:
    def __init__(self, tokens: list[Token]):
        self._t = tokens
        self._i = 0

    def _peek(self) -> Token | None:
        return self._t[self._i] if self._i < len(self._t) else None

    def _eat(self, ttype: str | None = None) -> Token:
        tok = self._t[self._i]
        if ttype and tok.type != ttype:
            raise SyntaxError(
                f'Expected {ttype} but got {tok.type}={tok.value!r} at offset {tok.pos}')
        self._i += 1
        return tok

    def parse(self) -> SNBTNode:
        v = self._value()
        if self._i < len(self._t):
            tok = self._t[self._i]
            raise SyntaxError(f'Trailing content at offset {tok.pos}: {tok.value!r}')
        return v

    def _value(self) -> SNBTNode:
        tok = self._peek()
        if tok is None:
            raise SyntaxError('Unexpected end of input')
        if tok.type == T_LBRACE:
            return self._compound()
        if tok.type == T_LBRACKET:
            return self._list_or_array()
        if tok.type == T_STRING:
            self._eat()
            return tok.value
        if tok.type == T_NUMBER:
            self._eat()
            return ('num', tok.value)
        if tok.type == T_WORD:
            self._eat()
            return ('word', tok.value)
        raise SyntaxError(f'Unexpected token {tok}')

    def _compound(self) -> OrderedDict:
        self._eat(T_LBRACE)
        d: OrderedDict = OrderedDict()
        tok = self._peek()
        while tok and tok.type != T_RBRACE:
            kt = self._eat()
            if kt.type == T_STRING:
                key = _unquote(kt.value)
            elif kt.type in (T_WORD, T_NUMBER):
                key = kt.value
            else:
                raise SyntaxError(f'Expected key, got {kt}')
            self._eat(T_COLON)
            d[key] = self._value()
            tok = self._peek()
            if tok and tok.type == T_COMMA:    # commas are optional
                self._eat(T_COMMA)
                tok = self._peek()
        self._eat(T_RBRACE)
        return d

    def _list_or_array(self) -> SNBTNode:
        self._eat(T_LBRACKET)
        tok = self._peek()
        if tok and tok.type == T_ARRPFX:
            arr_type = tok.value
            self._eat(T_ARRPFX)
            items = self._csv()
            self._eat(T_RBRACKET)
            return ('arr', arr_type, items)
        items = self._csv()
        self._eat(T_RBRACKET)
        return ('list', items)

    def _csv(self) -> list[SNBTNode]:
        items: list[SNBTNode] = []
        tok = self._peek()
        while tok and tok.type not in (T_RBRACKET, T_RBRACE):
            items.append(self._value())
            tok = self._peek()
            if tok and tok.type == T_COMMA:
                self._eat(T_COMMA)
                tok = self._peek()
        return items


def _unquote(s: str) -> str:
    """Strip outer quote chars from a raw quoted-string token."""
    if len(s) >= 2 and s[0] in '"\'':
        inner = s[1:-1]
        q = s[0]
        return inner.replace(f'\\{q}', q).replace('\\\\', '\\')
    return s


def parse_snbt(text: str) -> SNBTNode:
    return Parser(tokenize(text)).parse()


# ─────────────────────────────────────────────────────────────────────────────
# Fragment normalisation
#
# Accepts three source file formats:
#   1. Full SNBT compound/value   { ... }           → parsed as-is
#   2. Key-value fragment         DialogData: {...}, → detected, auto-wrapped
#   3. Bare value                 { Type: "YES_NO" } → parsed as-is; use --src-key .
#
# A fragment is detected by: text starts with an unquoted word OR a quoted
# string immediately followed by ':'  (i.e. it is a key-value pair without
# the surrounding braces).
# ─────────────────────────────────────────────────────────────────────────────

_FRAGMENT_RE = re.compile(
    r'^(?:[A-Za-z_][A-Za-z0-9_\-+.]*|"[^"]*"|\'[^\']*\')\s*:'
)


def load_source(path: str) -> tuple[SNBTNode, bool]:
    """
    Parse a source SNBT file. Returns (tree, is_fragment).
    is_fragment is True when the file was a key-value fragment that was
    auto-wrapped in braces before parsing.
    """
    try:
        raw = open(path).read()
    except OSError as e:
        die(str(e))

    # Strip surrounding whitespace and a trailing comma (copy-paste artefact)
    text = raw.strip()
    if text.endswith(','):
        text = text[:-1].strip()

    # Detect key-value fragment (e.g. "DialogData: { ... }")
    is_fragment = bool(_FRAGMENT_RE.match(text))
    if is_fragment:
        text = '{' + text + '}'

    try:
        return parse_snbt(text), is_fragment
    except SyntaxError as e:
        die(f'Parse error in {path!r}: {e}')


def resolve_source_path(
    tree: SNBTNode,
    is_fragment: bool,
    src_key_raw: str | None,
    target_path: list[str],
) -> list[str]:
    """
    Work out the key path to extract from the source tree.

    Priority:
      1. Explicit --src-key given by user.
      2. Fragment auto-detect: single-key compound → use that key.
      3. Default: same path as target.
    """
    if src_key_raw is not None:
        return [] if src_key_raw.strip() == '.' else \
               [p for p in src_key_raw.split('.') if p]

    if is_fragment and isinstance(tree, OrderedDict):
        keys = list(tree.keys())
        if len(keys) == 1:
            # The single key IS the snippet wrapper — extract its value.
            return keys   # e.g. ['DialogData']
        # Multiple keys in a fragment: fall back to target path

    return target_path   # same path in both files


# ─────────────────────────────────────────────────────────────────────────────
# Serializer
# ─────────────────────────────────────────────────────────────────────────────

_SAFE_KEY_RE = re.compile(r'^[A-Za-z0-9_\-+.]+$')


def _quote_key(k: str) -> str:
    if _SAFE_KEY_RE.match(k):
        return k
    escaped = k.replace('\\', '\\\\').replace('"', '\\"')
    return f'"{escaped}"'


def serialize(node: SNBTNode, indent_size: int = 4, compact: bool = False) -> str:
    if compact:
        return _compact(node)
    return _pretty(node, 0, indent_size)


def _compact(node: SNBTNode) -> str:
    if isinstance(node, OrderedDict):
        if not node:
            return '{}'
        return '{' + ', '.join(f'{_quote_key(k)}: {_compact(v)}' for k, v in node.items()) + '}'
    if isinstance(node, str):
        return node
    tag = node[0]
    if tag == 'list':
        return '[' + ', '.join(_compact(i) for i in node[1]) + ']' if node[1] else '[]'
    if tag == 'arr':
        _, arr_type, items = node
        return f'[{arr_type}; {", ".join(_compact(i) for i in items)}]'
    if tag in ('num', 'word'):
        return node[1]
    raise ValueError(f'Unknown node {tag!r}')


def _pretty(node: SNBTNode, depth: int, size: int) -> str:
    pad  = ' ' * (size * depth)
    ipad = ' ' * (size * (depth + 1))

    if isinstance(node, OrderedDict):
        if not node:
            return '{}'
        parts = [
            f'{ipad}{_quote_key(k)}: {_pretty(v, depth + 1, size)}'
            for k, v in node.items()
        ]
        return '{\n' + ',\n'.join(parts) + '\n' + pad + '}'

    if isinstance(node, str):
        return node

    tag = node[0]

    if tag == 'list':
        items = node[1]
        if not items:
            return '[]'
        if all(_is_primitive(i) for i in items):
            return '[' + ', '.join(_pretty(i, depth, size) for i in items) + ']'
        parts = [f'{ipad}{_pretty(i, depth + 1, size)}' for i in items]
        return '[\n' + ',\n'.join(parts) + '\n' + pad + ']'

    if tag == 'arr':
        _, arr_type, items = node
        return f'[{arr_type}; {", ".join(_pretty(i, 0, size) for i in items)}]'

    if tag in ('num', 'word'):
        return node[1]

    raise ValueError(f'Unknown node {tag!r}')


# ─────────────────────────────────────────────────────────────────────────────
# Path navigation & mutation  (non-destructive — returns new trees)
# ─────────────────────────────────────────────────────────────────────────────

def get_path(node: SNBTNode, path: list[str]) -> SNBTNode:
    if not path:
        return node
    if not isinstance(node, OrderedDict):
        raise KeyError(f'Cannot descend into non-compound with key {path[0]!r}')
    key = path[0]
    if key not in node:
        available = ', '.join(repr(k) for k in list(node.keys())[:10])
        raise KeyError(f'Key {key!r} not found  (available: {available})')
    return get_path(node[key], path[1:])


def set_path(node: SNBTNode, path: list[str], value: SNBTNode) -> SNBTNode:
    """Return a new tree with value inserted/replaced at path.
    Missing intermediate compounds are created automatically."""
    if not path:
        return value
    if not isinstance(node, OrderedDict):
        raise KeyError(f'Cannot set key inside a non-compound node (at key {path[0]!r})')
    key = path[0]
    new: OrderedDict = OrderedDict(node)
    child = new.get(key, OrderedDict())
    new[key] = set_path(child, path[1:], value)
    return new


def merge_nodes(base: SNBTNode, overlay: SNBTNode) -> SNBTNode:
    """Recursively overlay compound keys; non-compound values are replaced."""
    if isinstance(base, OrderedDict) and isinstance(overlay, OrderedDict):
        merged: OrderedDict = OrderedDict(base)
        for k, v in overlay.items():
            merged[k] = merge_nodes(merged[k], v) if k in merged else v
        return merged
    return overlay


# ─────────────────────────────────────────────────────────────────────────────
# CLI
# ─────────────────────────────────────────────────────────────────────────────

def die(msg: str) -> None:
    print(f'snbt-merge: error: {msg}', file=sys.stderr)
    sys.exit(1)


def main() -> None:
    ap = argparse.ArgumentParser(
        prog='snbt-merge',
        description='Splice a keyed section from one SNBT file into another.',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""\
source file formats:
  Full SNBT preset    { PresetMetadata: {...}, data: {...} }
  Key-value fragment  DialogData: { Type: "YES_NO", ... },
  Bare value          { Type: "YES_NO", ... }   (use --src-key .)

examples:
  # Fragment snippet → replace data.DialogData in full preset (most common)
  snbt-merge dialog_snippet.snbt data.DialogData full_preset.snbt

  # Full preset → full preset, same path
  snbt-merge preset_a.snbt data.DialogData preset_b.snbt

  # Write to stdout instead of overwriting
  snbt-merge snippet.snbt data.DialogData preset.snbt -o -

  # Deep-merge source ActionData into target ActionData
  snbt-merge snippet.snbt data.ActionData preset.snbt --mode merge

  # Bare-value snippet (no key wrapper) → explicit root extraction
  snbt-merge bare_dialog.snbt data.DialogData preset.snbt --src-key .

  # Different source/target paths
  snbt-merge other.snbt data.DialogData preset.snbt --src-key DialogData
""")

    ap.add_argument('source', help='SNBT file (or fragment) to read the snippet from')
    ap.add_argument('key',    help='Dot-separated target path, e.g. "data.DialogData"')
    ap.add_argument('target', help='SNBT file to modify')
    ap.add_argument('-o', '--out', default=None,
                    help='Output file (default: overwrite TARGET; use - for stdout)')
    ap.add_argument('-m', '--mode', choices=['replace', 'merge'], default='replace',
                    help='replace (default): overwrite; merge: deep-merge compounds')
    ap.add_argument('--src-key', default=None, metavar='PATH',
                    help='Extraction path inside SOURCE (auto-detected for fragments; '
                         'use "." for entire file)')
    ap.add_argument('--indent', type=int, default=4, metavar='N',
                    help='Spaces per indent level (default: 4)')
    ap.add_argument('--compact', action='store_true',
                    help='Write single-line SNBT')
    args = ap.parse_args()

    target_path = [p for p in args.key.split('.') if p]
    if not target_path:
        die('"key" must be a non-empty dot-path')

    # ── Source ───────────────────────────────────────────────────────────────
    src_tree, is_fragment = load_source(args.source)

    source_path = resolve_source_path(
        src_tree, is_fragment, args.src_key, target_path
    )

    if is_fragment and not args.src_key:
        key_used = source_path[0] if source_path else '.'
        print(f'snbt-merge: fragment detected — extracting key {key_used!r} from source',
              file=sys.stderr)

    try:
        snippet = get_path(src_tree, source_path)
    except KeyError as e:
        die(f'In source {args.source!r}: {e}')

    # ── Target ───────────────────────────────────────────────────────────────
    try:
        tgt_text = open(args.target).read()
    except OSError as e:
        die(str(e))

    try:
        tgt_tree = parse_snbt(tgt_text)
    except SyntaxError as e:
        die(f'Parse error in {args.target!r}: {e}')

    # ── Combine ──────────────────────────────────────────────────────────────
    if args.mode == 'merge':
        try:
            existing = get_path(tgt_tree, target_path)
        except KeyError:
            existing = OrderedDict()
        combined = merge_nodes(existing, snippet)
    else:
        combined = snippet

    try:
        new_tree = set_path(tgt_tree, target_path, combined)
    except KeyError as e:
        die(f'In target {args.target!r}: {e}')

    # ── Output ───────────────────────────────────────────────────────────────
    output = serialize(new_tree, indent_size=args.indent, compact=args.compact)

    out_path = args.out if args.out is not None else args.target

    if out_path == '-':
        print(output)
    else:
        try:
            with open(out_path, 'w') as f:
                f.write(output + '\n')
            label = 'Updated' if out_path == args.target else 'Written to'
            print(f'{label} {out_path}', file=sys.stderr)
        except OSError as e:
            die(str(e))


if __name__ == '__main__':
    main()
