# "Use the PC" (nurse / Professor Acacia dialog option) — DEFERRED open.
# Easy NPC auto-appends CLOSE_DIALOG and always runs it LAST → the client setScreen(null)s;
# if Cobblemon's PC screen opened this same tick it would be destroyed instantly
# (docs/ENGINE_FINDINGS.md §2). So schedule the open a few ticks out, onto a clean screen
# (mirrors phone/open). Single-player (CLAUDE.md) so the trampoline targets @a[limit=1].
schedule function cobblemon_initiative:pc/open_now 3t
