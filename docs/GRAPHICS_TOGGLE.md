# Graphics High/Low Toggle

A one-click **HIGH ⇄ LOW** graphics switch for low-end machines. It swaps three things at once:

| Axis | HIGH | LOW |
|------|------|-----|
| Textures | `Prime's HD Textures [256x]` (local file) | `Prime's HD Textures (32x)` (Modrinth `primes-hd-textures` 35.2) |
| Shaders (Iris/BSL) | **BSL on, ULTRA tier** — full shadows (3072 @ 512 dist) + colored/soft shadows + AO + light shafts + TAA | **BSL stays on, LOW tier** — shadows kept but 1024, no colored/soft shadows, no AO, no light shafts, no TAA |
| Video settings | Fancy / RD 16 / sim 12 / AO on / all particles / fancy clouds / entity ×3 / biome blend 5 / mipmap 4 | Fast / RD 8 / sim 5 / AO off / minimal particles / clouds off / entity ×0.5 / biome blend 0 / mipmap 0 |

> Both modes apply only the BSL keys listed above (via option merge), so **any other BSL options you've enabled — e.g. `ADVANCED_MATERIALS` — are preserved**, not reset.
>
> **LOW keeps BSL running** — it just tones BSL down to its own LOW quality tier (the big cost, shadow map resolution + extra passes, drops) rather than disabling shaders. Set `low.shaders = false` in the config if you'd rather have LOW turn shaders off entirely.

## Architecture (why it's split Java + FancyMenu)

FancyMenu's `manage_resource_pack` action can swap resource packs from the **title screen**, but FancyMenu has **no action for Iris shaders**, and Iris's public API can't select a shaderpack *by name*. So the design puts all the logic in Java and lets the menu button do only the one thing it's good at:

- **`graphics/GraphicsPresetManager`** (client) is the engine. It applies all three axes from `config/cobblemon-initiative-graphics.json`.
- **The active resource pack is the source of truth for the mode.** A throttled client tick (~1 s) watches `PackRepository.getSelectedIds()`. When it sees the 256x or 32x pack become active — whether from the FancyMenu menu button, the pack screen, or `/gfx` — it syncs shaders + video to match. Works on the **title screen and in-world**.
- **Shaders** go through `graphics/IrisBridge` (reflection, soft-dep — no compile-time Iris dependency, same pattern as the JourneyMap bridge). BSL stays enabled in both modes; the bridge applies the mode's BSL option values live via `net.irisshaders.iris.Iris.queueShaderPackOptionsFromProperties(Properties)` + `Iris.reload()` — the same runtime path Iris's own shader screen uses — which also persists them to `shaderpacks/BSL_v10.1.3.zip.txt`. The tier keys (`SHADOW`, `SHADOW_COLOR`, `SHADOW_FILTER`, `AO`, `LIGHT_SHAFT`, `TAA`, `shadowMapResolution`, `shadowDistance`) are exactly what BSL's `profile.LOW` / `profile.ULTRA` set, verified against the BSL v10.1.3 zip. (`iris.properties` still ships `enableShaders=true` + BSL selected; Iris can't pick a pack by name via API.)
  - **Preserving your other BSL options:** a queued reload resets any option *not* in the queue toward the pack defaults — which silently dropped `ADVANCED_MATERIALS=true`. So the bridge first reads the current `shaderpacks/BSL_v10.1.3.zip.txt`, seeds the queue with *all* of it, then overlays the tier keys — nothing else you've toggled is disturbed. (If your local settings file already lost a setting from the earlier behaviour, re-enable it once in the Iris screen and it'll stick from then on.)

### Entry points

- **`/gfx high` · `/gfx low` · `/gfx toggle` · `/gfx status`** — client commands (in-world).
- **Keybind** `key.cobblemon-initiative.graphics_toggle` — registered **unbound**; bind it in Controls if you want a hotkey.
- **FancyMenu main-menu button** — see below.

## Config — `config/cobblemon-initiative-graphics.json`

Ships in `mrpack/overrides/config/`. Auto-generated on first run if absent. The pack ids:

- **`lowPackId`** = `"file/Prime's HD Textures (32x).zip"` — note **parentheses** `(32x)`, matching the Modrinth filename (the local 256x uses **brackets** `[256x]`).
- **`hdPackId`** = `"file/Prime's HD Textures [256x].zip"` — the local 256x pack.

Each preset also carries a **`shaderOptions`** map — BSL option overrides applied live (booleans `"true"`/`"false"`, sliders like `shadowMapResolution`/`shadowDistance`). Defaults mirror BSL's HIGH and LOW tiers; edit freely, or add more BSL keys (e.g. `BLOOM`, `MOTION_BLUR`) to push LOW further. An empty map leaves BSL untouched.

`applyVideoSettings: false` makes the toggle swap only textures + shaders and leave the video sliders alone.

## The 32x pack (already wired)

The 32x pack is pinned in `mrpack/modpack.json` → `resourcepacks` as `{"slug":"primes-hd-textures","version":"35.2"}`, so `build_mrpack.py` resolves it from Modrinth and it installs as `resourcepacks/Prime's HD Textures (32x).zip`. The 256x HIGH pack stays a local file in `mrpack/resourcepacks/` because Modrinth only offers ≤128x. No `options.txt` edit needed — the engine selects/reloads packs at runtime.

## FancyMenu main-menu buttons (SHIPPED)

Two custom buttons are authored into `mrpack/overrides/config/fancymenu/customization/title_screen_layout.txt` (and the identical `run/config/…` copy), anchored bottom-right — reposition/restyle in the FancyMenu editor (`Ctrl+Alt+F`).

Action + requirement formats are **bytecode-verified against FancyMenu 3.9.1**. `manage_resource_pack`: value `PackName|||MODE|||reloadBool`, modes `ENABLE`/`DISABLE`/`TOGGLE`; `matchesPack` compares **both display title and `Pack.getId()`** case-insensitively, so buttons reference packs by exact **`file/…` id**. `open_file_folder_in_game_dir`: game-dir-relative path. Loading requirements serialize as a container meta line `[loading_requirement_container_meta:<c>] = [groups:][instances:<i>;]` plus one instance line `[loading_requirement:<i>][requirement_mode:if|if_not][group:][req_id:<id>] = <value>`; `is_resource_pack_enabled` checks the **selected** packs (verified: `PackRepository.method_14444` = `getSelectedPacks`) and also matches by `file/…` id.

The result is **one state-showing toggle** built from two overlapping buttons at the same spot, only one of which renders based on the live state:

- **`Graphics: High`** — loading requirement *is_resource_pack_enabled(256x) = `if`*, so it shows only while the 256x is the active pack. Two actions: `TOGGLE 256x` + `TOGGLE 32x`.
- **`Graphics: Low`** — same two `TOGGLE` actions, loading requirement *is_resource_pack_enabled(256x) = `if_not`*, shows while the 256x is **not** active.

Since the engine always keeps the two packs in opposite states, clicking the visible one toggles both → the other pack becomes active → the label flips `Graphics: High` ⇄ `Graphics: Low`. A player without the 256x never sees `Graphics: High` (it can't become enabled), so they just see `Graphics: Low`.

- **`Get HD Textures`** — one action, `open_file_folder_in_game_dir /resourcepacks`; loading requirement *is_resource_pack_enabled(256x) = `if_not`*, so it appears **only when you're not on the HD pack** (don't own it, or on Low). Opens the resourcepacks folder to drop the paid 256x zip in; the engine's per-toggle `repo.reload()` picks it up without a restart.

## Paid-pack gate (256x may not be owned)

The 256x pack is a paid add-on (Modrinth only distributes ≤128x), so the engine treats HIGH as available only when it's installed:

- `applyMode(HIGH)` (from `/gfx high`, the keybind, or a detected swap) **downgrades to LOW** if `PackRepository` doesn't list the 256x — actionbar note, never a broken "high" with missing textures.
- **Single-toggle safety:** if the 256x is absent, its `TOGGLE` is a no-op while the 32x toggles off → *no* managed pack is left active. The engine detects that empty state and re-asserts the saved mode (which clamps to LOW), re-enabling the 32x. Net effect: a player without the pack presses the toggle and simply stays on Low.
- A fresh session with no 256x likewise resolves to LOW (32x + performance settings) via the same "no managed pack → assert saved mode" path.

Install the 256x with the **Get HD Textures** button (or drop `Prime's HD Textures [256x].zip` into `resourcepacks/`); the per-toggle `repo.reload()` discovers it without a restart and HIGH becomes reachable.

> Keep `mrpack/overrides/config/fancymenu/` in sync with `run/config/fancymenu/` (hand-synced — this change updated both).

## In-game verification checklist (couldn't be verified offline)

1. `/gfx low` then `/gfx high` in-world — do textures, shaders, and video sliders all change?
2. Bind the keybind in Controls and confirm it toggles.
3. On the **title screen** (with the 256x installed), the toggle button should read **`Graphics: High`**; click it → it becomes **`Graphics: Low`** (and `Get HD Textures` appears) → click again → back to `Graphics: High`. Load the world and confirm textures/shaders/video followed within ~1 s each time. *(If the label doesn't flip, the loading-requirement serialization is off — see the revert note below.)*
4. Confirm the buttons' `file/…` ids match the installed pack filenames exactly (256x = brackets, 32x = parens).
5. Confirm the BSL tier changes on `/gfx low` → `/gfx high` (shadow softness/AO/light-shafts visibly toggle, `shaderpacks/BSL_v10.1.3.zip.txt` gains the lines) **and `ADVANCED_MATERIALS=true` survives the round-trip** (the bug this wave fixed).
6. **Paid-pack gate:** with the 256x removed from `resourcepacks/`, confirm the toggle shows only **`Graphics: Low`** (never `Graphics: High`), clicking it leaves you on Low (brief 32x flicker as the engine re-asserts), and `Get HD Textures` is visible. Use it to drop the 256x in, then confirm `Graphics: High` becomes reachable without a restart.

> **If a requirement misbehaves (safe revert):** the three loading-requirement instance lines are the only new/uncertain bit. In `title_screen_layout.txt`, blanking each `[loading_requirement_container_meta:…] = [groups:][instances:X;]` back to `…[instances:]` and deleting the matching `[loading_requirement:X]…` line makes all three buttons always-visible again (no layout breakage). The buttons themselves are unaffected.
