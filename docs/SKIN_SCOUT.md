# SKIN SCOUT — the civilian dress pass toolkit

`scripts/skin_scout` makes the "~300 civilians default to Steve" problem tractable:
fetch candidate skins per role, review them on one contact sheet, apply the pick
into the real skin pipeline (`content_compile`'s `rctmod`/`custom` skin types).

Requires: python3 (stdlib only) + ImageMagick `magick`. No PIL, no API keys.

## Usage

```bash
# 1. Fetch candidates for a role (default out: dev/skins/<slug>/ — gitignored)
scripts/skin_scout search "fisherman" -n 12
scripts/skin_scout search "miner" --out dev/skins/deepcore_extras

# 2. Build the review contact sheet (face+hat / torso / legs front figure,
#    12x nearest-neighbor, labeled montage) -> <dir>/sheet.png
scripts/skin_scout sheet dev/skins/fisherman

# 3. Apply the showrunner's pick into the pipeline
scripts/skin_scout apply gaviota_fisher dev/skins/fisherman/mineskin_42448060_fisherman.png
#    or, for an NPC that already has a custom-skin uuid slot:
scripts/skin_scout apply ignored chosen.png --uuid <dashed-uuid>

# Bonus: borrow a real player's current skin by Minecraft name (Mojang API)
scripts/skin_scout player jeb_
```

`apply --dry-run` prints destinations without writing; `--force` is required to
overwrite an existing, different file (protects e.g. leader textures and the 68
already-provisioned `mrpack/overrides/.../humanoid/*.png` skins).

## What apply actually does (pipeline fit)

Mirrors `scripts/content_compile` `skin_node()` exactly:

- **default (rctmod route)** — copies the PNG to
  `src/main/resources/resourcepacks/trainer_textures/assets/rctmod/textures/trainers/single/<target>.png`
  and pre-provisions
  `mrpack/overrides/config/easy_npc/skin/humanoid/<uuid5>.png`, where uuid5 =
  `uuid5(NAMESPACE_URL, "cobblemon-initiative:rctmod-skin:single/<target>")` — the
  same deterministic name content_compile derives, so the compile's own copy step
  becomes a byte-identical no-op. You then set
  `"skin": {"type": "rctmod", "texture": "single/<target>"}` in the character's
  dialog-src JSON (dialog-src/ is gitignored — lives outside plain checkouts) and
  run a **full** `scripts/content_compile` (any error aborts ALL writes; end at
  errors:0). Skins appear in-world after `/cobblemon-initiative install run`.
- **`--uuid` (custom route)** — copies the PNG straight to
  `mrpack/overrides/config/easy_npc/skin/humanoid/<uuid>.png` and prints the
  signed `[I;a,b,c,d]` int-array for
  `"skin": {"type": "custom", "uuid": [a, b, c, d]}`.

Per docs/ENGINE_FINDINGS.md, CUSTOM local skins are the RELIABLE mechanism —
remote-URL skins are whitelist-restricted in Easy NPC and minecraftskins.com
hotlink-protects (serves a blank stub to the game). skin_scout only ever produces
local CUSTOM skins.

## Sources & reliability (probed 2026-07-12)

| Source | Status | Notes |
|--------|--------|-------|
| **MineSkin v2** (`api.mineskin.org/v2/skins?filter=…`) | WORKS, keyless | Named community skins; texture hashes served raw from `textures.minecraft.net/texture/<hash>` (Mojang CDN, always up). Detail endpoint gives slim/classic variant. Plain-text name filter — single-word queries ("nomad") beat phrases ("desert nomad"). Keyless use is rate-limit-polite in the script (page cap + delays). |
| **Mojang profile API** (`api.mojang.com` → `sessionserver.mojang.com`) | WORKS | `player <name>` chain: name → uuid → base64 textures → skin PNG. Official, stable. |
| minecraftskins.com (Skindex) | BLOCKED (HTTP 403 Cloudflare) | Not scriptable with a plain user-agent; also hotlink-protects images. Skipped. |
| NameMC | BLOCKED (HTTP 403 Cloudflare) | Skipped. |

If MineSkin is down, `search` fails gracefully with a pointer to the `player`
fallback; already-downloaded candidates are never lost (manifest is additive and
re-runs skip existing files).

## Validation & gotchas

- Downloads are validated as real PNGs at 64x64 (modern) or 64x32 (legacy — kept
  for review, tagged `"format": "legacy"` in the manifest, but **refused by
  apply**: Easy NPC humanoid skins need the modern layout).
- `"variant": "slim"` in the manifest flags slim-arm (Alex-model) skins — they
  still work on the wide humanoid model but arm edges may look slightly off;
  prefer classic where the sheet shows two equal candidates.
- The contact sheet is the quality filter: novelty junk (brick textures, pickle
  skins, head-only skins) is instantly visible. Expect to discard half of any
  batch — fetch `-n 12`, pick 2-3.

## Licensing

Community skins are user-made uploads with no formal license. Fine for a private
modpack / stream build (this project); do not redistribute them as an asset pack.
Every download's credit trail (source API URL, texture URL, uploader-given name,
fetch date) is kept in each directory's `manifest.json`.
