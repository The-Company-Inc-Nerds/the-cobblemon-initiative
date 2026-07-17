#!/usr/bin/env python3
"""Client test-driver protocol — the CLIENT twin of mc_rcon.py.

Talks JSON-lines to the in-mod TestDriverClient socket (devtools/client/, dormant
unless the game client was launched with CI_DRIVER_PORT set). Where RCON drives the
SERVER (setup, tp, asserts), this drives the PLAYER: right-click NPCs, read the
dialog that actually rendered, click its buttons, walk (real movement — latch
radii + zone triggers fire), read chat/titles/sidebar, take framebuffer
screenshots (immune to the compositor/hypridle-lock gotcha).

Usage:
  from mc_client import Driver
  with Driver() as d:                     # default 127.0.0.1:25580
      d.wait_connected()                  # world joined
      npcs = d.op('entity.list', match='easy_npc')['entities']
      d.walk_to(npcs[0]['x'], npcs[0]['z'], tol=2.5)
      d.op('interact.entity', uuid=npcs[0]['uuid'])
      d.wait_screen('Dialog')
      print(d.op('screen.dump')['texts'])
      d.op('screen.click', text='Accept')
  # CLI:  python3 scripts/mc_client.py state
  #       python3 scripts/mc_client.py entity.list match=easy_npc radius=32
  #       python3 scripts/mc_client.py screen.click text='I accept'

Ops (see docs/TESTING_TOOLKIT.md § Client driver for the full table):
  ping state screen.dump screen.click screen.close screen.key entity.list
  interact.entity attack.entity interact.block use.item look.at move.to
  move.path move.status move.stop input.key hud.chat hud.sidebar party screenshot

Long hauls: don't chain guessed move.to legs — ask the server for a mob-grade A* route
(`cobblemon-initiative dev path <player> <x> <y> <z>` via RCON, parse route=x,y,z;...)
and feed it to Driver.follow_path(). e2e_run wraps both sides as the path_to step.

Launch the client with the driver enabled (env var survives the gradle fork):
  CI_DRIVER_PORT=25580 DISPLAY=:0 nix develop -c run-client \
      '--args=--quickPlayMultiplayer 127.0.0.1'
"""
import json
import re
import socket
import sys
import time

DEFAULT_PORT = 25580


class DriverError(RuntimeError):
    pass


class Driver:
    def __init__(self, host='127.0.0.1', port=DEFAULT_PORT,
                 timeout=60.0, retries=30, retry_delay=2.0):
        self._id = 0
        last = None
        for _ in range(retries):
            try:
                self.sock = socket.create_connection((host, port), timeout=timeout)
                break
            except OSError as e:
                last = e
                time.sleep(retry_delay)
        else:
            raise ConnectionError(f'driver: cannot reach {host}:{port}: {last} '
                                  f'(is the client up with CI_DRIVER_PORT={port}?)')
        self.sock.settimeout(timeout)
        self._buf = b''

    # -- protocol ------------------------------------------------------------

    def op(self, op, **args):
        """Run one op; return its data dict. Raises DriverError on ok:false."""
        self._id += 1
        req = {'id': self._id, 'op': op}
        if args:
            req['args'] = args
        self.sock.sendall((json.dumps(req) + '\n').encode('utf-8'))
        line = self._readline()
        resp = json.loads(line)
        if not resp.get('ok'):
            raise DriverError(f'{op}: {resp.get("error", "unknown error")}')
        return resp.get('data', {})

    def _readline(self):
        while b'\n' not in self._buf:
            chunk = self.sock.recv(65536)
            if not chunk:
                raise ConnectionError('driver: socket closed')
            self._buf += chunk
        line, self._buf = self._buf.split(b'\n', 1)
        return line.decode('utf-8')

    # -- wait helpers (Python-side polling; ops are synchronous) --------------

    def wait_connected(self, timeout=600.0):
        """Block until the player is in a world (client boot + join is minutes)."""
        return self._poll(lambda: self.op('state')['connected'] or None,
                          timeout, 'world join')

    def wait_screen(self, class_contains, timeout=15.0):
        """Block until a screen whose class name contains the needle is open."""
        def check():
            s = self.op('state')
            name = s.get('screenClass') or ''
            return s if class_contains.lower() in name.lower() else None
        return self._poll(check, timeout, f'screen ~ {class_contains!r}')

    def wait_no_screen(self, timeout=15.0):
        return self._poll(
            lambda: True if self.op('state').get('screen') is None else None,
            timeout, 'screen closed')

    def wait_chat(self, pattern, since=-1, timeout=30.0, kinds=None):
        """Block until a HUD entry (chat/system/overlay/title/subtitle) matches
        the regex. Returns (entry, last_seq). Use since=cursor for lossless scans."""
        rx = re.compile(pattern)
        state = {'since': since}

        def check():
            data = self.op('hud.chat', since=state['since'])
            state['since'] = data['last']
            for e in data['entries']:
                if kinds and e['kind'] not in kinds:
                    continue
                if rx.search(e['text']):
                    return (e, state['since'])
            return None
        return self._poll(check, timeout, f'hud text ~ /{pattern}/')

    def chat_cursor(self):
        """Current HUD sequence cursor — take BEFORE the action you want to observe.
        since=-1 returns everything retained; 'last' is then the real max seq (the
        Java side only advances 'last' over entries it returns, so a too-high since
        would echo back unreachable)."""
        return self.op('hud.chat', since=-1)['last']

    def walk_to(self, x, z, tol=1.5, timeout_ticks=1200, sprint=False):
        """move.to + block until arrival. Raises on timeout/stop."""
        self.op('move.to', x=x, z=z, tol=tol, timeoutTicks=timeout_ticks, sprint=sprint)
        deadline = time.time() + timeout_ticks / 20.0 + 10
        while time.time() < deadline:
            s = self.op('move.status')
            if not s['active']:
                if s['result'] != 'arrived':
                    raise DriverError(
                        f'walk_to({x},{z}): {s["result"]} at dist={s["dist"]:.1f}')
                return s
            time.sleep(0.25)
        self.op('move.stop')
        raise DriverError(f'walk_to({x},{z}): python-side timeout')

    def follow_path(self, nodes, tol=1.5, timeout_ticks=2400, sprint=False):
        """move.path + block until arrival. nodes = [[x,y,z],...] block ints from the
        server's `dev path <player> <x> <y> <z>` A* probe (PathProbe). The Walker steers
        node-to-node — stairs/steps/corners route the way mobs walk them. Raises on
        stuck:node=i/n (mid-route snag; the index says where) / timeout / stop."""
        self.op('move.path', nodes=nodes, tol=tol,
                timeoutTicks=timeout_ticks, sprint=sprint)
        deadline = time.time() + timeout_ticks / 20.0 + 10
        while time.time() < deadline:
            s = self.op('move.status')
            if not s['active']:
                if s['result'] != 'arrived':
                    raise DriverError(
                        f'follow_path({len(nodes)} nodes): {s["result"]} '
                        f'at dist={s["dist"]:.1f}')
                return s
            time.sleep(0.25)
        self.op('move.stop')
        raise DriverError(f'follow_path({len(nodes)} nodes): python-side timeout')

    def find_npc(self, name=None, radius=48):
        """Nearest matching entity dict (or None). Easy NPCs match on custom name."""
        kwargs = {'radius': radius}
        if name:
            kwargs['match'] = name
        ents = self.op('entity.list', **kwargs)['entities']
        return ents[0] if ents else None

    def _poll(self, check, timeout, what, interval=0.5):
        deadline = time.time() + timeout
        while time.time() < deadline:
            result = check()
            if result is not None:
                return result
            time.sleep(interval)
        raise TimeoutError(f'driver: timed out waiting for {what} ({timeout}s)')

    # -- lifecycle -----------------------------------------------------------

    def close(self):
        try:
            self.sock.close()
        except OSError:
            pass

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        self.close()


def _cli():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    op = sys.argv[1]
    args = {}
    for tok in sys.argv[2:]:
        if '=' not in tok:
            print(f'mc_client: args are key=value, got {tok!r}', file=sys.stderr)
            return 2
        k, v = tok.split('=', 1)
        try:
            args[k] = json.loads(v)   # numbers/bools/json pass through
        except json.JSONDecodeError:
            args[k] = v               # bare strings
    with Driver(retries=1, retry_delay=0) as d:
        print(json.dumps(d.op(op, **args), indent=2, ensure_ascii=False))
    return 0


if __name__ == '__main__':
    sys.exit(_cli())
