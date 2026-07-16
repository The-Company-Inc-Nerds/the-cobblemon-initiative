# PokéPhone + Care Systems — Design Spec

> **PLAN, not implementation.** Companion to `MINECRAFT_FLAVOR.md` (the beacon "in stock" call below is
> the phone's first use). All three systems verified feasible against the current code (see notes).

## §1 — The PokéPhone (remote story-beat delivery) ★
**Concept:** a PokéGear-style phone (Gold/Silver). NPCs **call you** when you're nowhere near them —
Mom checks in, the Mayor says a beacon came in, a grunt taunts, the villain gloats. It's the delivery
system for beats that shouldn't wait until you happen to walk up to someone.

**Feasibility — CONFIRMED, the mechanism already ships.** `easy_npc dialog open <npc> <player> <dialog>`
opens an Easy NPC dialog on a player remotely; the mod already uses it for the loiter/eavesdrop beats
(`sidequest/audit/loiter_ready`, `sidequest/memo/loiter_ready` open a dialog on the nearest listener NPC).
So a "call" is just: ring the phone + open a call dialog.

**Mechanism:**
- A **`phone/ring` function** (per caller): `title @s actionbar [{"text":"☎ Phone ringing — <Caller>","color":"gold"}]`
  + a chime (`playsound minecraft:block.note_block.bell` or similar), then
  `easy_npc dialog open <phone-host> @s <caller_call_dialog>` to open the "call."
- **Reliability:** host all call dialogs on a dedicated **invisible, always-loaded PhoneNPC** (spawned
  like the audit/memo "listener" NPCs, tagged `phone_host`, follows/near the player or at a loaded
  anchor) so a call never fails because the real caller's chunk is unloaded. The call dialog *is* the
  caller's remote voice (Mom, Suzune, …), authored as a normal Easy NPC dialog with `PLAYER_TAG` gates.
- **Trigger:** a light tick (or event) checks each call's condition and fires it **once** (guarded by a
  `call_<id>_done` tag). Missed/declined calls can re-ring or sit as a "1 missed call" actionbar.
- **Answer UX (v1):** the ring flourishes the actionbar, then the dialog auto-opens (you're "answering").
  *Optional v2:* the ring persists and you answer by sneaking / a PokéPhone item — more phone-like, more work.

**Initial calls (both requested):**
1. **Mayor Suzune — "beacon in stock":** when you can afford the next homestead beacon (a tick checks
   `cobbledollars query @s ≥ price` AND a `beacon_restocked` flag), Suzune rings: *"Another beacon came in
   off the coast. Bring me [X] CobbleDollars and it is yours."* → sends you to buy it (ties to
   `MINECRAFT_FLAVOR.md` §1). Re-arms each time a new beacon is available.
2. **Mom — "I want to watch your Pokémon":** after **gym 3** (`memory_fragment ≥ 3`), Mom rings to
   introduce her friendship-care service (§3): *"You have a real team now. Leave one with me a while — a
   mother is good for a Pokémon's heart. Come home when you can."*

**Extensible:** any beat becomes a call — grunt recognition taunts, the Acting CEO's HQ warning, Board
gloats, Professor updates, "your team misses you" nudges. One reusable system; each caller is a dialog.

**Open questions:** auto-open vs press-to-answer; missed-call handling; whether the PhoneNPC follows the
player (guaranteed loaded) or lives at a fixed anchor (simpler, but needs the player loaded near it —
the follow approach is safer for true "call anywhere").

---

## §2 — The Daycare is independent (not the Company)
**Current state:** the daycare keeper is already neutral — **no** Company/"verified" framing in its dialog
(checked). This is a small **positive lore beat**, not a rewrite: make the independence *explicit*. In a
world where the Company runs the money, the shops, the fields, and the exchange, the daycare is one of the
**last honest, un-bought services** — a safe haven for your team.
- Add a keeper line: *"We are not Company. They came round with an offer and a form; we told them where to
  file it. A Pokémon left here is left with people, not a ledger."* Pairs thematically with Mom (§3) —
  both are non-Company care.
- **Effort:** trivial (a say-line or two on the existing keeper dialog). No mechanics change.

---

## §3 — Mom, the Friendship Caretaker
**Concept:** leave **one** Pokémon with **Mom** and it grows **friendship** while it boards — distinct
from the Sango daycare (levels/breeding). Thematically perfect: your mother nurtures your team's heart
while you reclaim your past, and it's a purely-loving, non-Company service (pairs with §2).

**Feasibility — CONFIRMED, reuse the daycare infra.** `DaycareManager` is already a full hardcore-safe
boarding system: deposit/withdraw, a periodic **drip**, relog/crash-persistent **custody**, stand-ins,
and the hard invariant that *a boarded Pokémon can never faint/die/be lost*. Mom's care is a **second,
one-slot boarding service** modeled on it (or a "friendship mode" flag on `DaycareManager`):
- **1 slot.** Deposit one party Pokémon with Mom (reuse the party-picker bridge the daycare already has).
- **Drip = friendship, not XP.** Each in-game day (hook `economy/dawn`) or on a tick, raise the boarded
  mon's Cobblemon **friendship** (0→255) by a set amount, capped. (Cobblemon exposes a friendship value;
  the daycare already deserializes and mutates boarded Pokémon in Java, so setting `friendship` is the
  same access pattern — confirm the exact property name at build.)
- **Hardcore-safe.** Same invariant as the daycare — the boarded mon is in custody and cannot die.
- **Payoff.** High friendship enables **friendship evolutions** (Golbat→Crobat, Riolu→Lucario, Pichu→Pikachu,
  Eevee→Espeon/Umbreon, Chansey→Blissey, Budew→Roselia, …), powers Return, and just *feels* like a bonded
  team. A genuine reason to visit home.
- **Intro:** the phone call in §1 (post-gym-3). Withdraw any time; friendship persists on the mon.

**Open questions:** friendship gain rate + cap (per day vs per real-minute); does Mom's slot share the
daycare's XP-drip or stay friendship-only (recommend friendship-only, so Mom is *distinct* from the
daycare); any small fee or is a mother's care free (recommend **free** — she's your mom).

---

## §4 — Config & defaults (ModMenu-tunable)
Same house rule as everything else: sensible defaults, all exposed in the config + `InitiativeConfigScreen`
(ModMenu) so the showrunner can retune live.

| Knob | Default | Notes |
|---|---|---|
| `phone_enabled` | `true` | master switch for the PokéPhone system |
| `phone_auto_open` | `true` | `false` = ring persists, answer by sneak / item (v2) |
| `phone_ring_sound` | `minecraft:block.note_block.bell` | the chime |
| `phone_npc_follows` | `true` | `true` = call anywhere (PhoneNPC stays loaded near you); `false` = fixed anchor |
| `mom_care_enabled` | `true` | Mom's friendship-caretaker service |
| `mom_friendship_per_day` | `5` | friendship gained per in-game day boarded (0→255; a friendship evo needs ~160+) |
| `mom_friendship_rate_multiplier` | `1.0` | global scale on the gain |
| `mom_friendship_cap` | `255` | ceiling |
| `mom_care_fee` | `0` | free — she's your mom |
| `daycare_independent_flavor` | `true` | the "not Company" lore line on the keeper (§2) |

Multipliers default to `1.0`; the base numbers are the source of truth and multipliers are pure overrides.
