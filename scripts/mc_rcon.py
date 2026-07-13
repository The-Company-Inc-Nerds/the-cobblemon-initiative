#!/usr/bin/env python3
"""Minimal Source-RCON client for driving the dev dedicated server.

Why RCON instead of the FIFO console: the FIFO → gradle-stdin path dies when a
second gradle build launches (gradle's daemon stdin forwarding starves — found
2026-07-12), and log-scraping for command results is racy. RCON is a direct TCP
socket to the server with SYNCHRONOUS responses: each command returns its own
output (e.g. every `[TEST] ...` line), and a tp into ungenerated chunks simply
blocks the response until done — free handshaking.

Usage:
  from mc_rcon import Rcon
  with Rcon() as r:                      # defaults: 127.0.0.1:25575 / devtest
      print(r.cmd('list'))
  # or CLI:  python3 scripts/mc_rcon.py 'say hi' 'list'

Needs run/server.properties: enable-rcon=true, rcon.port=25575, rcon.password=devtest.
"""
import socket
import struct
import sys
import time

SERVERDATA_AUTH = 3
SERVERDATA_EXECCOMMAND = 2
SERVERDATA_RESPONSE_VALUE = 0


class Rcon:
    def __init__(self, host='127.0.0.1', port=25575, password='devtest',
                 timeout=600.0, retries=30, retry_delay=2.0):
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
            raise ConnectionError(f'rcon: cannot reach {host}:{port}: {last}')
        self.sock.settimeout(timeout)
        if not self._auth(password):
            raise PermissionError('rcon: auth failed (check rcon.password)')

    def _send(self, ptype, body):
        self._id += 1
        payload = struct.pack('<ii', self._id, ptype) + body.encode('utf-8') + b'\x00\x00'
        self.sock.sendall(struct.pack('<i', len(payload)) + payload)
        return self._id

    def _recv_packet(self):
        raw = b''
        while len(raw) < 4:
            chunk = self.sock.recv(4 - len(raw))
            if not chunk:
                raise ConnectionError('rcon: socket closed')
            raw += chunk
        (length,) = struct.unpack('<i', raw)
        data = b''
        while len(data) < length:
            chunk = self.sock.recv(length - len(data))
            if not chunk:
                raise ConnectionError('rcon: socket closed mid-packet')
            data += chunk
        pid, ptype = struct.unpack('<ii', data[:8])
        return pid, ptype, data[8:-2].decode('utf-8', 'replace')

    def _auth(self, password):
        self._send(SERVERDATA_AUTH, password)
        while True:
            pid, ptype, _ = self._recv_packet()
            if ptype == SERVERDATA_EXECCOMMAND:  # auth response type == 2
                return pid != -1

    def cmd(self, command):
        """Run one command; return its full (possibly multi-packet) output."""
        cid = self._send(SERVERDATA_EXECCOMMAND, command)
        # Fence trick: a second, empty command; everything before its response
        # belongs to `command` (vanilla answers in-order on one connection).
        fence = self._send(SERVERDATA_RESPONSE_VALUE, '')
        out = []
        while True:
            pid, ptype, body = self._recv_packet()
            if pid == fence:
                return ''.join(out)
            if pid == cid and ptype == SERVERDATA_RESPONSE_VALUE:
                out.append(body)

    def close(self):
        try:
            self.sock.close()
        except OSError:
            pass

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        self.close()


if __name__ == '__main__':
    with Rcon() as r:
        for c in sys.argv[1:]:
            print(r.cmd(c))
