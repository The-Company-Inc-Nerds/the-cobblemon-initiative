#!/usr/bin/env python3
"""Full-map LIVE NPC placement sweep — attaches to a running dev server via RCON.

Derives every latch trigger point from ambient/placements.mcfunction (no hardcoded
areas), clusters them into tp stops, walks a Carpet fake player through all of them,
and runs `/cobblemon-initiative test placement live` at each stop. RCON responses are
synchronous, so a tp into ungenerated chunks self-paces (no log scraping, no races —
the FIFO console approach died to gradle-stdin starvation and writer contention).

Prereqs:
  - dev server running (gradle runServer) with rcon enabled in run/server.properties
    (enable-rcon=true, rcon.port=25575, rcon.password=devtest)
  - Carpet in the dev mod set (it is: build.gradle.kts modRuntimeOnly)

Usage: python3 scripts/npc_live_sweep.py [--bot smoke1] [--dwell 4]
Output: human summary on stdout; full JSON report to dev/live_sweep_report.json
Exit: nonzero if any HARD problem (SUNK / HEAD_BLOCKED / IN_WALL) is found.
"""
import argparse
import json
import math
import os
import re
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mc_rcon import Rcon

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PLACEMENTS = os.path.join(
    ROOT, 'src/main/resources/data/cobblemon_initiative/function/ambient/placements.mcfunction')
REPORT = os.path.join(ROOT, 'dev/live_sweep_report.json')
CLUSTER_RADIUS = 25.0  # latch trigger is 40 blocks; keep stops well inside it


def load_latches():
    pts = []
    rx = re.compile(r'#amb_(\S+) ci_ambient matches 0 if entity '
                    r'@a\[x=([\d.-]+),y=([\d.-]+),z=([\d.-]+)')
    with open(PLACEMENTS) as f:
        for line in f:
            m = rx.search(line)
            if m:
                pts.append((m.group(1), float(m.group(2)), float(m.group(3)), float(m.group(4))))
    return pts


def cluster(pts):
    clusters = []
    for name, x, y, z in pts:
        best, bd = None, 1e9
        for c in clusters:
            d = math.dist((x, y, z), (c[0], c[1], c[2]))
            if d < bd:
                bd, best = d, c
        if best is not None and bd < CLUSTER_RADIUS:
            n = len(best[3])
            best[0] = (best[0] * n + x) / (n + 1)
            best[1] = (best[1] * n + y) / (n + 1)
            best[2] = (best[2] * n + z) / (n + 1)
            best[3].append(name)
        else:
            clusters.append([x, y, z, [name]])
    # nearest-neighbour ordering keeps consecutive stops close (fewer chunk stalls)
    route = [clusters.pop(0)]
    while clusters:
        last = route[-1]
        clusters.sort(key=lambda c: math.dist((c[0], c[2]), (last[0], last[2])))
        route.append(clusters.pop(0))
    return route


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--bot', default='smoke1')
    ap.add_argument('--dwell', type=float, default=4.0,
                    help='seconds after tp for latch fire + Easy NPC import')
    args = ap.parse_args()

    pts = load_latches()
    route = cluster(pts)
    print(f'{len(pts)} latches -> {len(route)} stops')

    r = Rcon()
    if args.bot not in r.cmd('list'):
        r.cmd(f'player {args.bot} spawn')
        time.sleep(2)
        r.cmd(f'gamemode creative {args.bot}')

    problems = {}   # "name@x,y,z" -> status line
    summaries = []
    for i, (x, y, z, names) in enumerate(route):
        resp = r.cmd(f'tp {args.bot} {x} {y + 1} {z}')
        if 'Teleported' not in resp:
            print(f'stop {i}: tp failed: {resp.strip()[:120]}', flush=True)
            continue
        time.sleep(args.dwell)
        out = r.cmd('cobblemon-initiative test placement live')
        # RCON concatenates the command's output lines WITHOUT newlines — re-split
        # on the stable [TEST] prefix instead of relying on line breaks.
        for line in re.split(r'(?=\[TEST\] )', out):
            m = re.match(r'\[TEST\] liveplace (\S+@[\d,-]+) ([A-Z_]+\S*(?: \S+=\S+)*)', line)
            if m:
                problems[m.group(1)] = m.group(2)
            elif 'liveplace PASS' in line or 'liveplace FAIL' in line:
                summaries.append({'stop': i, 'names': names, 'summary': line.strip()})
        if i % 10 == 0:
            print(f'stop {i}/{len(route)}', flush=True)

    # which latches never fired at all (score != 1 -> never placed in this world)
    unfired = []
    for name, *_ in pts:
        resp = r.cmd(f'scoreboard players get #amb_{name} ci_ambient')
        if ' has 1' not in resp:
            unfired.append(name)
    r.close()

    hard = {k: v for k, v in problems.items()
            if any(s in v for s in ('SUNK', 'HEAD_BLOCKED', 'IN_WALL'))}
    soft = {k: v for k, v in problems.items() if k not in hard}
    report = {'stops': len(route), 'latches': len(pts), 'hard': hard, 'soft': soft,
              'unfired_latches': unfired, 'stop_summaries': summaries}
    os.makedirs(os.path.dirname(REPORT), exist_ok=True)
    with open(REPORT, 'w') as f:
        json.dump(report, f, indent=1)

    print(f'\n== LIVE SWEEP RESULT ==')
    print(f'hard problems (sunk/head/in-wall): {len(hard)}')
    for k, v in sorted(hard.items()):
        print(f'  {k} {v}')
    print(f'floating (soft, verify visually): {len(soft)}')
    for k, v in sorted(soft.items()):
        print(f'  {k} {v}')
    print(f'latches never fired in this world: {len(unfired)}')
    for n in unfired:
        print(f'  {n}')
    print(f'full report: {REPORT}')
    return 1 if hard else 0


if __name__ == '__main__':
    sys.exit(main())
