#!/usr/bin/env python3
"""
dialog_lint — offline static analyzer for Easy NPC dialog presets.

Finds dead, shadowed, and unreachable dialog content — the bug class the
band-tag gating system breeds. No server needed; pure static analysis over
the compiled presets + the auto-generated band_tags tick function.

ENGINE SEMANTICS MODELED (bytecode-verified, see docs/ENGINE_FINDINGS.md §2/§3
and docs/EASY_NPC_REFERENCE.md "Dialog" + "Actions & Conditions"):

  * PLAYER_TAG conditions evaluate `player.getTags().contains(Name)` ONLY.
    The Operation field (incl. NOT_EQUALS) is IGNORED — every PLAYER_TAG gate
    is effectively "player HAS the tag". Negation rides derived inverse tags
    `no_<X>` maintained by function/dialog/band_tags.mcfunction; numeric gates
    ride derived band tags (badges_gte_N / <obj>_gte_N / _lt_N / _eq_N) from
    the same file.
  * Dialog entry selection (DialogDataSet.getNextAvailableDialog):
      1. keep entries with Priority >= 0 (Priority -1 = MANUAL_ONLY, reachable
         only via OPEN_NAMED_DIALOG / OPEN_NAMED_DIALOG_CONDITIONAL actions or
         `easy_npc dialog open ... <label>` / npcsight-driven opens);
      2. keep entries whose Conditions ALL pass (no conditions = always pass);
      3. sort by Priority DESCENDING, ties broken by Label ASCENDING; take the
         first.
    Entries with empty label/text are dropped on load; a duplicate label
    overwrites the earlier entry (dialogByLabelMap).
  * Entry/button gates use the bare `Conditions` list. ACTION gates use the
    DOUBLED key `ConditionDataSet: {ConditionDataSet: [...]}` — a bare
    `Conditions` list on an ACTION is silently ignored (flagged here).

SIMULATION MODEL:

  Per NPC we build the tag universe from every condition, resolve each tag to
  a literal over independent world variables:
    - band tag        -> numeric range over its source objective
                         (memory_fragment aka badges enumerates 0..10; other
                         objectives enumerate breakpoint representatives)
    - inverse no_<X>  -> negation of X's literal (only if band_tags maintains it)
    - any other tag   -> free boolean (base/defeat/story tag)
    - non-PLAYER_TAG condition -> opaque free boolean keyed by its full tuple
  ORPHAN tags (referenced but granted nowhere) are still treated as FREE
  variables during simulation so structural shadowing is reported separately
  from the missing grant (otherwise every orphan gate would double-report as
  DEAD).

  For each auto-eligible entry E we enumerate states over the variables of E
  plus every higher-precedence entry (exact — variables outside that set
  cannot affect whether E wins). If the scoped state space exceeds
  MAX_STATES, we fall back to a pairwise-entailment approximation (an entry
  is dead if a single higher entry's conditions are entailed by its own;
  union-coverage deadness across several entries may be missed) and the
  finding is marked "approximate".

FINDINGS:
  ERROR   DEAD_ENTRY        entry can never be shown (never wins the ladder in
                            any state and is not a named-dialog target; or
                            unsatisfiable conditions / duplicate label / no text)
  ERROR   ORPHAN_TAG        tag referenced in conditions but granted nowhere
  ERROR   NEGATION_BUG      Operation NOT_EQUALS (or other comparison) on a
                            condition type that IGNORES Operation — it silently
                            behaves as HAS-tag
  WARN    UNREACHABLE_BUTTON button conditions can never pass in any state
                            where its entry is shown
  WARN    MISPLACED_ACTION_GATE bare `Conditions` on an ACTION (ignored by engine)
  WARN    PARSE/PRIORITY notes
  INFO    SHADOWED_RANGE    entry only wins in a clipped slice of an axis its
                            own conditions do not constrain (e.g. only below
                            badge N because a recognition entry outranks it)
  INFO    UNREACHABLE_ACTION action gate never passes in entry-winning states

Exit status: nonzero iff any DEAD_ENTRY / ORPHAN_TAG / NEGATION_BUG finding.
Output: human summary on stdout + dev/dialog_lint_report.json.

Usage:
    python3 scripts/dialog_lint.py [--json PATH] [--quiet]
"""

from __future__ import annotations

import argparse
import itertools
import json
import os
import re
import sys
import time
from collections import OrderedDict, defaultdict

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPT_DIR)
import snbt_merge  # tolerant SNBT parser (tokenizer + AST), reused

REPO = os.path.dirname(SCRIPT_DIR)
RES = os.path.join(REPO, "src", "main", "resources")
PRESET_ROOT = os.path.join(RES, "data", "easy_npc", "preset")
DATA_ROOT = os.path.join(RES, "data")
BAND_TAGS = os.path.join(
    DATA_ROOT, "cobblemon_initiative", "function", "dialog", "band_tags.mcfunction")
JAVA_ROOT = os.path.join(REPO, "src", "main", "java")
NPCSIGHT_PROFILES = os.path.join(DATA_ROOT, "cobblemon_initiative", "npcsight_profiles.json")
RCT_TRAINER_DIR = os.path.join(DATA_ROOT, "rctmod", "trainers")
DEFAULT_JSON_OUT = os.path.join(REPO, "dev", "dialog_lint_report.json")

# Cap on scoped state enumeration before falling back to the pairwise
# approximation (see module docstring).
MAX_STATES = 300_000
MAX_BOOL_VARS = 16
MAX_INFO_STDOUT = 60  # stdout cap; the JSON report always carries everything

# badges ride the memory_fragment objective (story-flag canon)
BADGE_OBJECTIVE = "memory_fragment"
BADGE_DOMAIN = list(range(0, 11))  # 0..10 gyms

# Condition types whose Operation field is IGNORED by the engine
# (EASY_NPC_REFERENCE "Conditions that ignore Operation")
OPERATION_IGNORED_TYPES = {
    "EXECUTION_LIMIT", "HAS_ITEM_IN_INVENTORY", "HAS_ITEM_IN_HAND",
    "ADVANCEMENT", "PLAYER_TAG", "TEAM", "GAMEMODE", "WEATHER",
    "FALLBACK", "NONE",
}

NAMED_OPEN_TYPES = {"OPEN_NAMED_DIALOG", "OPEN_NAMED_DIALOG_CONDITIONAL"}

# Compiler convention for entries missing an explicit Priority
# (scripts/content_compile PRIORITY_DEFAULT; the engine's own
# calculateDefaultPriority(Label) is label-derived — compiled presets always
# write Priority, so this only triggers on hand-authored files and is flagged)
PRIORITY_DEFAULT = {"default": 10, "start": 10, "main": 10,
                    "help": 5, "question": 5, "bye": 1, "idle": 1, "thanks": 1}

TAG_ADD_RE = re.compile(r"\btag\s+\S+(?:\[[^\]]*\])?\s+add\s+([A-Za-z0-9_.\-]+)")
# Quest helper commands grant their trailing success-tag argument at runtime
# (CobblemonInitiativeCommands: turnin <item> <count> [tag],
#  trade <take> <give> [level [tag]])
HELPER_GRANT_RES = [
    re.compile(r"\bcobblemon-initiative turnin \S+ \d+ ([A-Za-z0-9_.\-]+)"),
    re.compile(r"\bcobblemon-initiative trade \S+ \S+ \d+ ([A-Za-z0-9_.\-]+)"),
]
JAVA_STR_RE = re.compile(r'"([A-Za-z0-9_.\-]+)"')
LABEL_CHARS_RE = re.compile(r"^[a-z0-9_]+$")


# ---------------------------------------------------------------------------
# SNBT node unwrapping (snbt_merge AST -> plain python)
# ---------------------------------------------------------------------------

def unwrap(node):
    """Convert an snbt_merge AST node into plain python values."""
    if isinstance(node, OrderedDict):
        return OrderedDict((k, unwrap(v)) for k, v in node.items())
    if isinstance(node, tuple):
        kind = node[0]
        if kind == "list":
            return [unwrap(v) for v in node[1]]
        if kind == "arr":
            return [unwrap(v) for v in node[2]]
        if kind == "num":
            raw = node[1].rstrip("bBsSlLfFdD")
            try:
                return int(raw)
            except ValueError:
                return float(raw)
        if kind == "word":
            return node[1]
    if isinstance(node, str):
        return snbt_merge._unquote(node)
    return node


def load_preset(path):
    with open(path, "r", encoding="utf-8") as fh:
        text = fh.read()
    return unwrap(snbt_merge.parse_snbt(text)), text


# ---------------------------------------------------------------------------
# band_tags.mcfunction parsing — the derived-tag rulebook
# ---------------------------------------------------------------------------

BAND_RE = re.compile(
    r"^execute as @a if score @s (\S+) matches (\S+) run tag @s add (\S+)\s*$")
INverse_RE = re.compile(r"^tag @a\[tag=!(\S+)\] add (no_\S+)\s*$")


def parse_range(spec):
    """Minecraft range spec -> (lo, hi) inclusive; None = unbounded."""
    if ".." in spec:
        lo_s, hi_s = spec.split("..", 1)
        lo = int(lo_s) if lo_s else None
        hi = int(hi_s) if hi_s else None
        return lo, hi
    v = int(spec)
    return v, v


def parse_band_tags(path):
    """Return (band_rules: tag -> (objective, lo, hi), inverse: no_X -> X)."""
    band_rules = {}
    inverse = {}
    if not os.path.isfile(path):
        return band_rules, inverse
    with open(path, "r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            m = BAND_RE.match(line)
            if m:
                obj, spec, tag = m.groups()
                band_rules[tag] = (obj, *parse_range(spec))
                continue
            m = INverse_RE.match(line)
            if m:
                base, inv = m.groups()
                inverse[inv] = base
    return band_rules, inverse


# ---------------------------------------------------------------------------
# Grant scan — where is each base tag granted?
# ---------------------------------------------------------------------------

def scan_grants():
    """Collect tag -> [source, ...] for every `tag ... add <X>` in the data
    tree (functions + preset command strings + config JSONs), plus npcsight
    meettag grants. band_tags.mcfunction is EXCLUDED (its adds are the derived
    families modeled separately)."""
    grants = defaultdict(list)

    for root, _dirs, files in os.walk(DATA_ROOT):
        for fn in files:
            path = os.path.join(root, fn)
            if os.path.abspath(path) == os.path.abspath(BAND_TAGS):
                continue
            if not (fn.endswith(".mcfunction") or fn.endswith(".json")
                    or fn.endswith(".snbt")):
                continue
            try:
                with open(path, "r", encoding="utf-8", errors="replace") as fh:
                    text = fh.read()
            except OSError:
                continue
            rel = os.path.relpath(path, REPO)
            for m in TAG_ADD_RE.finditer(text):
                grants[m.group(1)].append(rel)
            # npcsight meettag <uuid> <tag> grants the tag when sight fires
            for m in re.finditer(r"\bnpcsight meettag \S+ ([A-Za-z0-9_.\-]+)", text):
                grants[m.group(1)].append(rel + " (npcsight meettag)")
            for hre in HELPER_GRANT_RES:
                for m in hre.finditer(text):
                    grants[m.group(1)].append(rel + " (quest helper cmd)")

    return grants


def scan_java_literals():
    """All string literals in the Java tree — a referenced tag matching one is
    considered Java-granted (InitiativeInit story flags, sight stop-tags...)."""
    lits = set()
    for root, _dirs, files in os.walk(JAVA_ROOT):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            try:
                with open(os.path.join(root, fn), "r", encoding="utf-8",
                          errors="replace") as fh:
                    lits.update(JAVA_STR_RE.findall(fh.read()))
            except OSError:
                continue
    return lits


UUID_RE = re.compile(
    r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
SELECTOR_TAG_RE = re.compile(r"@e\[[^\]]*\btag=([A-Za-z0-9_.\-]+)")


def scan_global_label_refs():
    """Dialog labels opened from OUTSIDE preset button actions:
    `easy_npc dialog open ... <label>` in functions, npcsight profile
    `dialog` fields, and register_sight `npcsight add <uuid> <range> <label>` /
    `npcsight dialog <uuid> <label>` lines.

    Each ref is SCOPED so one NPC's named open doesn't whitelist every NPC
    sharing the label: ('uuid', <uuid>) | ('etag', <entity tag>) | ('global',).
    Returns a list of (label, scope, source)."""
    refs = []

    for root, _dirs, files in os.walk(DATA_ROOT):
        for fn in files:
            if not fn.endswith(".mcfunction"):
                continue
            path = os.path.join(root, fn)
            rel = os.path.relpath(path, REPO)
            try:
                with open(path, "r", encoding="utf-8", errors="replace") as fh:
                    for line in fh:
                        line = line.strip()
                        if "easy_npc dialog open" in line:
                            last = line.split()[-1]
                            if LABEL_CHARS_RE.match(last):
                                m = SELECTOR_TAG_RE.search(line)
                                scope = ("etag", m.group(1)) if m else ("global",)
                                refs.append((last, scope, rel))
                        if line.startswith("npcsight add "):
                            parts = line.split()
                            if len(parts) >= 5 and LABEL_CHARS_RE.match(parts[4]) \
                                    and not parts[4].isdigit():
                                scope = (("uuid", parts[2])
                                         if UUID_RE.match(parts[2]) else ("global",))
                                refs.append((parts[4], scope, rel))
                        if line.startswith("npcsight dialog "):
                            parts = line.split()
                            if len(parts) >= 4 and LABEL_CHARS_RE.match(parts[3]):
                                scope = (("uuid", parts[2])
                                         if UUID_RE.match(parts[2]) else ("global",))
                                refs.append((parts[3], scope, rel))
            except OSError:
                continue

    if os.path.isfile(NPCSIGHT_PROFILES):
        try:
            with open(NPCSIGHT_PROFILES, "r", encoding="utf-8") as fh:
                for prof in json.load(fh):
                    lbl = prof.get("dialog")
                    if lbl:
                        tag = prof.get("tag")
                        scope = ("etag", tag) if tag else ("global",)
                        refs.append((lbl, scope,
                                     os.path.relpath(NPCSIGHT_PROFILES, REPO)))
        except (OSError, ValueError):
            pass
    return refs


def load_preset_map():
    """npc/preset_map.json: uuid -> preset resource path
    (easy_npc:preset/<type>/<name>.npc.snbt)."""
    path = os.path.join(DATA_ROOT, "cobblemon_initiative", "npc", "preset_map.json")
    out = {}
    if os.path.isfile(path):
        try:
            with open(path, "r", encoding="utf-8") as fh:
                for uuid, info in (json.load(fh).get("npcs") or {}).items():
                    out[uuid.lower()] = info.get("preset", "")
        except (OSError, ValueError):
            pass
    return out


def rct_trainer_ids():
    ids = set()
    if os.path.isdir(RCT_TRAINER_DIR):
        for root, _d, files in os.walk(RCT_TRAINER_DIR):
            for fn in files:
                if fn.endswith(".json"):
                    ids.add(fn[:-5])
    return ids


# ---------------------------------------------------------------------------
# Dialog model extraction
# ---------------------------------------------------------------------------

def generate_label(name):
    """DialogUtils.generateDialogLabel: trim, lowercase, spaces->_, strip
    non [a-z0-9_], truncate 32."""
    s = (name or "").strip().lower().replace(" ", "_")
    s = re.sub(r"[^a-z0-9_]", "", s)
    return s[:32]


class Cond:
    """One ConditionDataEntry."""

    __slots__ = ("type", "operation", "name", "value", "subtype")

    def __init__(self, d):
        self.type = str(d.get("Type", "NONE"))
        self.operation = str(d.get("Operation", "NONE"))
        self.name = str(d.get("Name", "")).strip()
        self.value = d.get("Value", 0)
        self.subtype = d.get("SubType")

    def key(self):
        return (self.type, self.operation, self.name, self.value, self.subtype)

    def describe(self):
        if self.type == "PLAYER_TAG":
            return "PLAYER_TAG %s" % self.name
        return "%s %s %s %s" % (self.type, self.name, self.operation, self.value)


def conds_from(node):
    out = []
    for c in (node or []):
        if isinstance(c, dict):
            out.append(Cond(c))
    return out


class Action:
    __slots__ = ("type", "cmd", "gates", "bare_conditions")

    def __init__(self, d):
        self.type = str(d.get("Type", "NONE"))
        self.cmd = str(d.get("Cmd", ""))
        gate_node = d.get("ConditionDataSet")
        inner = []
        if isinstance(gate_node, dict):
            inner = gate_node.get("ConditionDataSet") or []
        self.gates = conds_from(inner)
        # bare `Conditions` on an ACTION is silently ignored by the engine
        self.bare_conditions = conds_from(d.get("Conditions"))


class Button:
    __slots__ = ("name", "label", "conds", "actions")

    def __init__(self, d):
        self.name = str(d.get("Name", ""))
        self.label = str(d.get("Label") or generate_label(self.name))
        self.conds = conds_from(d.get("Conditions"))
        self.actions = []
        acts = d.get("Actions")
        if isinstance(acts, list):
            self.actions = [Action(a) for a in acts if isinstance(a, dict)]
        elif isinstance(acts, dict):
            inner = acts.get("ActionData") or acts.get("ActionDataSet") or []
            self.actions = [Action(a) for a in inner if isinstance(a, dict)]


class Entry:
    __slots__ = ("name", "label", "priority", "priority_assumed", "conds",
                 "buttons", "has_text", "index")

    def __init__(self, d, index):
        self.name = str(d.get("Name", ""))
        self.label = str(d.get("Label") or generate_label(self.name))
        self.index = index
        if "Priority" in d:
            self.priority = int(d["Priority"])
            self.priority_assumed = False
        else:
            self.priority = PRIORITY_DEFAULT.get(self.label, 5)
            self.priority_assumed = True
        self.conds = conds_from(d.get("Conditions"))
        self.buttons = [Button(b) for b in (d.get("Buttons") or [])
                        if isinstance(b, dict)]
        texts = d.get("Texts")
        self.has_text = bool(texts) or bool(str(d.get("Text", "")).strip())


class Npc:
    __slots__ = ("path", "rel", "resource", "name", "entries",
                 "trigger_actions", "local_label_refs", "entity_tags")

    def __init__(self, path, tree):
        self.path = path
        self.rel = os.path.relpath(path, REPO)
        # easy_npc:preset/<type>/<name>.npc.snbt (preset_map.json form)
        self.resource = "easy_npc:preset/" + os.path.relpath(
            path, PRESET_ROOT).replace(os.sep, "/")
        meta = tree.get("PresetMetadata") or {}
        self.name = str(meta.get("name", os.path.basename(path)))
        data = tree.get("data") or {}
        self.entity_tags = {str(t) for t in (data.get("Tags") or [])}
        dset = ((data.get("DialogData") or {}).get("DialogDataSet")) or []
        self.entries = [Entry(e, i) for i, e in enumerate(dset)
                        if isinstance(e, dict)]
        self.trigger_actions = []
        ev = ((data.get("ActionData") or {}).get("ActionEventSet")) or {}
        for event, actions in ev.items():
            if isinstance(actions, list):
                for a in actions:
                    if isinstance(a, dict):
                        self.trigger_actions.append((event, Action(a)))
        self.local_label_refs = set()
        for _ev, act in self.trigger_actions:
            if act.type in NAMED_OPEN_TYPES and act.cmd:
                self.local_label_refs.add(act.cmd.strip())
        for e in self.entries:
            for b in e.buttons:
                for act in b.actions:
                    if act.type in NAMED_OPEN_TYPES and act.cmd:
                        self.local_label_refs.add(act.cmd.strip())


# ---------------------------------------------------------------------------
# Tag universe + literal resolution
# ---------------------------------------------------------------------------

class TagUniverse:
    def __init__(self, band_rules, inverse, grants, java_literals, trainer_ids):
        self.band_rules = band_rules      # tag -> (obj, lo, hi)
        self.inverse = inverse            # no_X -> X
        self.grants = grants              # tag -> [source,...]
        self.java_literals = java_literals
        self.trainer_ids = trainer_ids
        self.referenced = defaultdict(list)   # tag -> [where,...]
        self._objectives = defaultdict(set)   # objective -> breakpoints
        for _t, (obj, lo, hi) in band_rules.items():
            if lo is not None:
                self._objectives[obj].add(lo)
            if hi is not None:
                self._objectives[obj].add(hi + 1)

    def note_reference(self, tag, where):
        self.referenced[tag].append(where)

    def objective_domain(self, obj):
        if obj == BADGE_OBJECTIVE:
            return BADGE_DOMAIN
        pts = sorted(self._objectives.get(obj, set()))
        dom = {0}
        for b in pts:
            dom.add(b)
            if b - 1 >= 0:
                dom.add(b - 1)
        if pts:
            dom.add(pts[-1] + 1)
        return sorted(dom)

    def resolve(self, tag):
        """tag -> literal:
             ('num', objective, lo, hi)       value within range => tag present
             ('num_not', objective, lo, hi)   value within range => tag ABSENT
             ('bool', base, wanted)           base tag presence == wanted"""
        if tag in self.band_rules:
            obj, lo, hi = self.band_rules[tag]
            return ("num", obj, lo, hi)
        if tag in self.inverse:
            base = self.inverse[tag]
            if base in self.band_rules:
                obj, lo, hi = self.band_rules[base]
                return ("num_not", obj, lo, hi)
            return ("bool", base, False)
        return ("bool", tag, True)

    def classify(self, tag):
        """-> (class, note)"""
        if tag in self.band_rules:
            obj, lo, hi = self.band_rules[tag]
            return ("band", "derived: %s in %s..%s" % (
                obj, "" if lo is None else lo, "" if hi is None else hi))
        if tag in self.inverse:
            base = self.inverse[tag]
            if self._is_granted(base):
                return ("inverse", "maintained inverse of %s" % base)
            return ("inverse_orphan_base",
                    "inverse maintained, but base %r is granted NOWHERE — "
                    "condition is constant-true" % base)
        if tag.startswith("no_"):
            return ("orphan",
                    "no_* tag NOT maintained by band_tags — never granted, "
                    "condition is constant-false")
        if self._is_granted(tag):
            if tag.startswith("defeated_"):
                return ("defeat", "granted by battle onwin / functions")
            return ("base", "granted")
        if tag.startswith("defeated_") and tag[len("defeated_"):] in self.trainer_ids:
            return ("orphan",
                    "trainer file exists (band no_%s IS maintained) but no "
                    "onwin/function ever adds %s" % (tag, tag))
        return ("orphan", "referenced but granted nowhere")

    def _is_granted(self, tag):
        return bool(self.grants.get(tag)) or tag in self.java_literals


# ---------------------------------------------------------------------------
# State-space simulation
# ---------------------------------------------------------------------------

class CondEval:
    """Compile a Cond list into literals over independent variables.
    Variables: ('obj', objective) numeric | ('tag', base) bool |
    ('cond', key...) opaque bool for non-PLAYER_TAG conditions."""

    def __init__(self, universe):
        self.u = universe

    def literals(self, conds):
        lits = []
        for c in conds:
            if c.type == "PLAYER_TAG" and c.name:
                lit = self.u.resolve(c.name)
                if lit[0] == "num":
                    lits.append(("num", ("obj", lit[1]), lit[2], lit[3], True))
                elif lit[0] == "num_not":
                    lits.append(("num", ("obj", lit[1]), lit[2], lit[3], False))
                else:
                    lits.append(("bool", ("tag", lit[1]), lit[2]))
            else:
                lits.append(("bool", ("cond",) + c.key(), True))
        return lits

    @staticmethod
    def variables(lits):
        return {l[1] for l in lits}

    @staticmethod
    def passes(lits, state):
        for l in lits:
            if l[0] == "num":
                _k, var, lo, hi, want_in = l
                v = state[var]
                inside = (lo is None or v >= lo) and (hi is None or v <= hi)
                if inside != want_in:
                    return False
            else:
                _k, var, wanted = l
                if state[var] != wanted:
                    return False
        return True


def domain_of(var, universe):
    if var[0] == "obj":
        return universe.objective_domain(var[1])
    return [False, True]


def enumerate_states(variables, universe):
    """Yield dict states over the given variables (ordered product)."""
    vs = sorted(variables, key=repr)
    domains = [domain_of(v, universe) for v in vs]
    total = 1
    for d in domains:
        total *= len(d)
    if total > MAX_STATES:
        return None, total
    states = []
    for combo in itertools.product(*domains):
        states.append(dict(zip(vs, combo)))
    return states, total


def entailed(sub_lits, super_lits):
    """True if every literal in super_lits is implied by sub_lits
    (conservative; used only in the fallback approximation)."""
    def num_bounds(lits, var):
        lo, hi = None, None
        for l in lits:
            if l[0] == "num" and l[1] == var and l[4]:
                if l[2] is not None:
                    lo = l[2] if lo is None else max(lo, l[2])
                if l[3] is not None:
                    hi = l[3] if hi is None else min(hi, l[3])
        return lo, hi

    for sl in super_lits:
        if sl[0] == "bool":
            if not any(l[0] == "bool" and l[1] == sl[1] and l[2] == sl[2]
                       for l in sub_lits):
                return False
        else:
            _k, var, lo, hi, want_in = sl
            if not want_in:
                return False  # don't reason about negated ranges here
            slo, shi = num_bounds(sub_lits, var)
            if slo is None and shi is None:
                return False
            if lo is not None and (slo is None or slo < lo):
                return False
            if hi is not None and (shi is None or shi > hi):
                return False
    return True


def format_axis(var):
    if var[0] == "obj":
        return "%s%s" % (var[1], " (badges)" if var[1] == BADGE_OBJECTIVE else "")
    if var[0] == "tag":
        return "tag %s" % var[1]
    return "cond %s" % (var[1:],)


def format_values(values, domain):
    """Compact human form of a winning value set."""
    values = sorted(values, key=repr)
    if values == sorted(domain, key=repr):
        return "any"
    if all(isinstance(v, bool) for v in values):
        return "/".join("present" if v else "absent" for v in values)
    ints = sorted(v for v in values)
    runs, start, prev = [], None, None
    for v in ints:
        if start is None:
            start = prev = v
        elif v == prev + 1:
            prev = v
        else:
            runs.append((start, prev))
            start = prev = v
    if start is not None:
        runs.append((start, prev))
    return ", ".join("%d" % a if a == b else "%d..%d" % (a, b) for a, b in runs)


# ---------------------------------------------------------------------------
# Lint driver
# ---------------------------------------------------------------------------

SEV_ORDER = {"ERROR": 0, "WARNING": 1, "INFO": 2}
GATING = {"DEAD_ENTRY", "ORPHAN_TAG", "NEGATION_BUG"}


class Report:
    def __init__(self):
        self.findings = []

    def add(self, severity, category, npc_rel, npc_name, entry, detail, **extra):
        f = {"severity": severity, "category": category, "file": npc_rel,
             "npc": npc_name, "entry": entry, "detail": detail}
        f.update(extra)
        self.findings.append(f)


def cond_sites(npc, universe, report):
    """Walk every condition site of an NPC: register tag references, flag
    NEGATION_BUG + MISPLACED_ACTION_GATE."""
    def visit(conds, where, entry_label):
        for c in conds:
            if c.type == "PLAYER_TAG" and c.name:
                universe.note_reference(
                    c.name, "%s :: %s" % (npc.rel, where))
            if c.type in OPERATION_IGNORED_TYPES and c.operation not in ("NONE", "EQUALS"):
                report.add(
                    "ERROR", "NEGATION_BUG", npc.rel, npc.name, entry_label,
                    "%s has Operation:%s on %s — the engine IGNORES the "
                    "Operation field for this type; it evaluates as plain "
                    "contains()/HAS (probable authoring error; negations must "
                    "ride derived no_<X> tags)" % (where, c.operation, c.describe()))

    for e in npc.entries:
        visit(e.conds, "entry '%s' Conditions" % e.label, e.label)
        for b in e.buttons:
            visit(b.conds, "entry '%s' button '%s' Conditions" % (e.label, b.label),
                  e.label)
            for i, act in enumerate(b.actions):
                visit(act.gates,
                      "entry '%s' button '%s' action[%d] gate" % (e.label, b.label, i),
                      e.label)
                if act.bare_conditions:
                    report.add(
                        "WARNING", "MISPLACED_ACTION_GATE", npc.rel, npc.name,
                        e.label,
                        "entry '%s' button '%s' action[%d] (%s) carries a bare "
                        "Conditions list — ACTIONS only honor the doubled "
                        "ConditionDataSet:{ConditionDataSet:[...]} key; this "
                        "gate is silently ignored and the action fires "
                        "unconditionally" % (e.label, b.label, i, act.type))
    for event, act in npc.trigger_actions:
        visit(act.gates, "trigger %s action gate" % event, None)
        if act.bare_conditions:
            report.add(
                "WARNING", "MISPLACED_ACTION_GATE", npc.rel, npc.name, None,
                "trigger %s action (%s) carries a bare Conditions list — "
                "silently ignored by the engine" % (event, act.type))


def npc_external_labels(npc, global_label_refs, preset_map):
    """Labels this NPC can have opened by name from outside its own preset."""
    uuids = {u for u, res in preset_map.items() if res == npc.resource}
    out = set()
    for label, scope, _src in global_label_refs:
        if scope[0] == "global":
            out.add(label)
        elif scope[0] == "uuid" and scope[1].lower() in uuids:
            out.add(label)
        elif scope[0] == "etag" and scope[1] in npc.entity_tags:
            out.add(label)
    return out


def analyze_npc(npc, universe, external_labels, report):
    ev = CondEval(universe)

    # --- load-time filtering: empty text / duplicate labels -----------------
    live_entries = []
    seen = {}
    for e in npc.entries:
        if not e.label or not e.has_text:
            report.add("ERROR", "DEAD_ENTRY", npc.rel, npc.name, e.label or "(no label)",
                       "entry #%d ('%s') has empty label/text — Easy NPC drops "
                       "it on load (addDialog rejects)" % (e.index, e.name))
            continue
        if e.label in seen:
            prev = seen[e.label]
            report.add("ERROR", "DEAD_ENTRY", npc.rel, npc.name, prev.label,
                       "duplicate Label '%s': entry #%d is OVERWRITTEN by entry "
                       "#%d in dialogByLabelMap — the earlier entry can never "
                       "be shown" % (e.label, prev.index, e.index))
            live_entries.remove(prev)
        seen[e.label] = e
        live_entries.append(e)
        if e.priority_assumed and len(npc.entries) > 1:
            report.add("WARNING", "PRIORITY_ASSUMED", npc.rel, npc.name, e.label,
                       "entry '%s' has no explicit Priority — assumed %d "
                       "(engine uses calculateDefaultPriority(Label); write it "
                       "explicitly)" % (e.label, e.priority))

    referenced_labels = set(npc.local_label_refs) | external_labels

    auto = [e for e in live_entries if e.priority >= 0]

    entry_results = {}

    for e in live_entries:
        e_lits = ev.literals(e.conds)
        named_ref = e.label in referenced_labels

        if e.priority < 0:
            # MANUAL_ONLY: reachable only via named-dialog opens
            if not named_ref:
                report.add(
                    "ERROR", "DEAD_ENTRY", npc.rel, npc.name, e.label,
                    "entry '%s' is MANUAL_ONLY (Priority %d) but no "
                    "OPEN_NAMED_DIALOG action, trigger, function `easy_npc "
                    "dialog open`, or npcsight profile ever opens it"
                    % (e.label, e.priority))
                entry_results[e.label] = {"reachable": False}
            else:
                entry_results[e.label] = {
                    "reachable": True, "via": "named-dialog only"}
            continue

        higher = [h for h in auto if h is not e and
                  (h.priority > e.priority or
                   (h.priority == e.priority and h.label < e.label))]
        h_lits = {h.label: ev.literals(h.conds) for h in higher}

        variables = set(ev.variables(e_lits))
        for hl in h_lits.values():
            variables |= ev.variables(hl)

        n_bools = sum(1 for v in variables if v[0] != "obj")
        states = None
        if n_bools <= MAX_BOOL_VARS:
            states, total = enumerate_states(variables, universe)

        approximate = states is None
        if approximate:
            # pairwise-entailment fallback (documented approximation)
            sat = entailed(e_lits, e_lits)  # trivially true; real check below
            dead_by = None
            for h in higher:
                if entailed(e_lits, h_lits[h.label]):
                    dead_by = h
                    break
            if dead_by is not None and not named_ref:
                report.add(
                    "ERROR", "DEAD_ENTRY", npc.rel, npc.name, e.label,
                    "entry '%s' (P%d) can never win: conditions of "
                    "higher-priority '%s' (P%d) are entailed by its own "
                    "[approximate: pairwise entailment, >%d states]"
                    % (e.label, e.priority, dead_by.label, dead_by.priority,
                       MAX_STATES), approximate=True)
                entry_results[e.label] = {"reachable": False, "approximate": True}
            else:
                entry_results[e.label] = {"reachable": True, "approximate": True}
            continue

        own_states = [s for s in states if CondEval.passes(e_lits, s)]
        win_states = []
        blockers = defaultdict(int)
        for s in own_states:
            blocking = [h for h in higher if CondEval.passes(h_lits[h.label], s)]
            if blocking:
                # the actual winner is the highest of them, but every passing
                # higher entry contributes to the clipping picture
                top = max(blocking, key=lambda h: (h.priority, ))
                blockers[top.label] += 1
            else:
                win_states.append(s)

        entry_results[e.label] = {
            "reachable": bool(win_states) or named_ref,
            "win_states": win_states,
            "own_states": own_states,
            "variables": variables,
            "lits": e_lits,
        }

        if not own_states:
            report.add(
                "ERROR", "DEAD_ENTRY", npc.rel, npc.name, e.label,
                "entry '%s' (P%d) has UNSATISFIABLE conditions (contradictory "
                "tag/band literals): %s" % (
                    e.label, e.priority,
                    "; ".join(c.describe() for c in e.conds)))
            continue

        if not win_states:
            blk = sorted(blockers.items(), key=lambda kv: -kv[1])
            blk_desc = ", ".join(
                "'%s' (P%d%s)" % (
                    lbl, next(h.priority for h in higher if h.label == lbl),
                    ", unconditional" if not h_lits[lbl] else "")
                for lbl, _n in blk[:4])
            if named_ref:
                report.add(
                    "INFO", "AUTO_DEAD_NAMED_ONLY", npc.rel, npc.name, e.label,
                    "entry '%s' (P%d) never wins auto-selection (outranked by "
                    "%s) but is opened by name — OK if intentional"
                    % (e.label, e.priority, blk_desc))
            else:
                report.add(
                    "ERROR", "DEAD_ENTRY", npc.rel, npc.name, e.label,
                    "entry '%s' (P%d) NEVER wins in any simulated state — "
                    "always outranked by %s; it is dead code"
                    % (e.label, e.priority, blk_desc))
            continue

        # ---- SHADOWED RANGE: axes the entry does not itself constrain ------
        # Suppress the NPC's designated FALLBACK (unconditional entry at the
        # bottom of the auto ladder): losing whenever anything specific passes
        # is definitionally its job, not shadowing.
        min_auto_prio = min(a.priority for a in auto)
        if not e.conds and e.priority == min_auto_prio:
            continue
        own_vars = ev.variables(e_lits)
        clipped = []
        for var in sorted(variables - own_vars, key=repr):
            dom = domain_of(var, universe)
            proj_win = {s[var] for s in win_states}
            proj_own = {s[var] for s in own_states}
            if proj_win < proj_own:
                lost = proj_own - proj_win
                # which higher entries pass in the lost slice?
                who = defaultdict(int)
                for s in own_states:
                    if s[var] in lost:
                        for h in higher:
                            if CondEval.passes(h_lits[h.label], s):
                                who[(h.label, h.priority)] += 1
                clipped.append((var, dom, proj_win, lost, who))
        if clipped:
            parts = []
            for var, dom, proj_win, lost, who in clipped:
                blockers_s = ", ".join(
                    "'%s'(P%d)" % (lbl, pr)
                    for (lbl, pr), _n in sorted(who.items(),
                                                key=lambda kv: -kv[1])[:3])
                parts.append(
                    "%s: wins only at [%s], loses [%s] to %s" % (
                        format_axis(var), format_values(proj_win, dom),
                        format_values(lost, dom), blockers_s or "?"))
            report.add(
                "INFO", "SHADOWED_RANGE", npc.rel, npc.name, e.label,
                "entry '%s' (P%d) wins only in a clipped state slice on axes "
                "its own conditions do not constrain — %s"
                % (e.label, e.priority, "; ".join(parts)),
                winning_slice={format_axis(v): format_values(pw, d)
                               for v, d, pw, _l, _w in clipped})

    # ---- buttons + action gates against the entry's shown states ----------
    for e in live_entries:
        res = entry_results.get(e.label) or {}
        if not res.get("reachable") or "own_states" not in res:
            continue
        base_states = res["win_states"] or res["own_states"]
        base_vars = res["variables"]
        e_lits = res["lits"]

        for b in e.buttons:
            extra_conds = list(b.conds)
            if not extra_conds and not any(a.gates for a in b.actions):
                continue
            # extend the state space with variables the button/action adds
            add_vars = set()
            b_lits = ev.literals(b.conds)
            add_vars |= ev.variables(b_lits) - base_vars
            gate_lits = {}
            for i, act in enumerate(b.actions):
                gl = ev.literals(act.gates)
                gate_lits[i] = gl
                add_vars |= ev.variables(gl) - base_vars

            ext_states = base_states
            if add_vars:
                extra_states, total = enumerate_states(add_vars, universe)
                if extra_states is None:
                    continue  # too big; skip silently (rare)
                ext_states = [{**s, **x} for s in base_states
                              for x in extra_states]

            if b.conds:
                shown = [s for s in ext_states if CondEval.passes(b_lits, s)]
                if not shown:
                    report.add(
                        "WARNING", "UNREACHABLE_BUTTON", npc.rel, npc.name,
                        e.label,
                        "button '%s' on entry '%s' can NEVER unlock: its "
                        "conditions [%s] never pass in any state where the "
                        "entry is shown" % (
                            b.label, e.label,
                            "; ".join(c.describe() for c in b.conds)))
                    continue
            else:
                shown = ext_states

            # tag names gated on by each action, for boilerplate detection
            def gate_tags(act):
                return {c.name for c in act.gates
                        if c.type == "PLAYER_TAG" and c.name}

            def mirror(tag):
                return tag[3:] if tag.startswith("no_") else "no_" + tag

            for i, act in enumerate(b.actions):
                if not act.gates:
                    continue
                if not any(CondEval.passes(gate_lits[i], s) for s in shown):
                    # Suppress compiler battle-block boilerplate: the standard
                    # battle button stamps a beaten-say gated defeated_X next
                    # to the battle gated no_defeated_X; in entries where one
                    # side is impossible the other action is knowingly inert.
                    mirrored = any(
                        mirror(t) in gate_tags(other)
                        for t in gate_tags(act)
                        for j, other in enumerate(b.actions) if j != i)
                    if mirrored:
                        continue
                    report.add(
                        "INFO", "UNREACHABLE_ACTION", npc.rel, npc.name, e.label,
                        "entry '%s' button '%s' action[%d] (%s) gate [%s] "
                        "never passes in any state where the button is "
                        "clickable" % (
                            e.label, b.label, i, act.type,
                            "; ".join(c.describe() for c in act.gates)))


def find_presets():
    out = []
    for root, _dirs, files in os.walk(PRESET_ROOT):
        for fn in sorted(files):
            if fn.endswith(".npc.snbt") and not fn.startswith("_"):
                out.append(os.path.join(root, fn))
    return out


def main():
    ap = argparse.ArgumentParser(description="Easy NPC dialog static analyzer")
    ap.add_argument("--json", default=DEFAULT_JSON_OUT,
                    help="JSON report path (default dev/dialog_lint_report.json)")
    ap.add_argument("--quiet", action="store_true",
                    help="suppress INFO findings on stdout")
    args = ap.parse_args()

    t0 = time.time()
    band_rules, inverse = parse_band_tags(BAND_TAGS)
    grants = scan_grants()
    java_literals = scan_java_literals()
    trainer_ids = rct_trainer_ids()
    global_label_refs = scan_global_label_refs()

    universe = TagUniverse(band_rules, inverse, grants, java_literals, trainer_ids)
    report = Report()

    presets = find_presets()
    npcs = []
    n_entries = 0
    for path in presets:
        try:
            tree, _text = load_preset(path)
        except Exception as ex:  # tolerant: report, keep going
            report.add("WARNING", "PARSE_ERROR", os.path.relpath(path, REPO),
                       os.path.basename(path), None,
                       "SNBT parse failed: %s" % ex)
            continue
        npc = Npc(path, tree)
        npcs.append(npc)
        n_entries += len(npc.entries)

    # pass 1: condition sites (tag references, negation bugs, misplaced gates)
    for npc in npcs:
        cond_sites(npc, universe, report)

    # pass 2: orphan tags (global, once)
    tag_classes = {}
    for tag in sorted(universe.referenced):
        cls, note = universe.classify(tag)
        tag_classes[tag] = {"class": cls, "note": note,
                            "grants": sorted(set(universe.grants.get(tag, [])))[:6],
                            "referenced_by": universe.referenced[tag][:8],
                            "reference_count": len(universe.referenced[tag])}
        if cls == "orphan":
            report.add(
                "ERROR", "ORPHAN_TAG", None, None, None,
                "tag '%s' is referenced by %d condition(s) but GRANTED NOWHERE "
                "(%s) — e.g. %s" % (
                    tag, len(universe.referenced[tag]), note,
                    universe.referenced[tag][0]),
                tag=tag, references=universe.referenced[tag][:10])
        elif cls == "inverse_orphan_base":
            report.add(
                "ERROR", "ORPHAN_TAG", None, None, None,
                "tag '%s': %s — every gate on it is constant (references: %s)"
                % (tag, note, universe.referenced[tag][0]),
                tag=tag, references=universe.referenced[tag][:10])

    # pass 3: per-NPC ladder simulation
    preset_map = load_preset_map()
    for npc in npcs:
        analyze_npc(npc, universe,
                    npc_external_labels(npc, global_label_refs, preset_map),
                    report)

    # ---- output ------------------------------------------------------------
    findings = sorted(report.findings,
                      key=lambda f: (SEV_ORDER[f["severity"]], f["category"],
                                     f["file"] or "", f["entry"] or ""))
    counts = defaultdict(int)
    for f in findings:
        counts[f["category"]] += 1
    sev_counts = defaultdict(int)
    for f in findings:
        sev_counts[f["severity"]] += 1

    print("DIALOG LINT — %d presets, %d dialog entries, %d referenced tags "
          "(%d band, %d inverse rules) — %.1fs"
          % (len(npcs), n_entries, len(universe.referenced),
             len(band_rules), len(inverse), time.time() - t0))
    print()
    last_sev = None
    info_shown = 0
    info_total = sev_counts["INFO"]
    for f in findings:
        if f["severity"] == "INFO":
            if args.quiet:
                continue
            info_shown += 1
            if info_shown > MAX_INFO_STDOUT:
                continue
        if f["severity"] != last_sev:
            print("== %s ==" % f["severity"])
            last_sev = f["severity"]
        loc = f["file"] or "(global)"
        if f["entry"]:
            loc += " :: %s" % f["entry"]
        print("[%s] %s\n    %s" % (f["category"], loc, f["detail"]))
    if not args.quiet and info_total > MAX_INFO_STDOUT:
        print("... %d more INFO findings (all in the JSON report)"
              % (info_total - MAX_INFO_STDOUT))
    print()
    print("Summary: %d error(s), %d warning(s), %d info — %s"
          % (sev_counts["ERROR"], sev_counts["WARNING"], sev_counts["INFO"],
             ", ".join("%s=%d" % kv for kv in sorted(counts.items()))))

    out = {
        "generated": time.strftime("%Y-%m-%dT%H:%M:%S"),
        "presets_scanned": len(npcs),
        "dialog_entries": n_entries,
        "semantics": {
            "selection": "Priority>=0, all Conditions pass (PLAYER_TAG = "
                         "contains() only, Operation ignored), sort Priority "
                         "desc then Label asc, first wins",
            "action_gates": "doubled ConditionDataSet key only; bare "
                            "Conditions on actions is ignored",
            "approximation": "per-entry scoped exact enumeration; falls back "
                             "to pairwise entailment above %d states / %d "
                             "bool vars (findings marked approximate)"
                             % (MAX_STATES, MAX_BOOL_VARS),
            "orphan_tags_in_simulation": "treated as free variables so "
                                         "structural findings stay separate "
                                         "from missing grants",
        },
        "summary": {"by_severity": dict(sev_counts),
                    "by_category": dict(counts)},
        "tag_universe": tag_classes,
        "findings": findings,
    }
    os.makedirs(os.path.dirname(os.path.abspath(args.json)), exist_ok=True)
    with open(args.json, "w", encoding="utf-8") as fh:
        json.dump(out, fh, indent=2)
    print("JSON report: %s" % os.path.relpath(args.json, os.getcwd()))

    gate = any(f["category"] in GATING or
               (f["severity"] == "ERROR" and f["category"] == "DEAD_ENTRY")
               for f in findings)
    sys.exit(1 if gate else 0)


if __name__ == "__main__":
    main()
