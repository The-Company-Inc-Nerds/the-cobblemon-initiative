# The Cobblemon Initiative

A single-player **Fabric** mod (Minecraft 1.21.1 + Cobblemon 1.7.3, **0.6.x alpha**) built for UPM 2 and played as a live **hardcore + Nuzlocke** production. It layers gym-badge progression, level caps, Nuzlocke death mechanics, an amnesia mystery, a destabilizing in-world economy, elemental shrine challenges, Legends-Arceus-style noble boss encounters, a full side-quest board with an on-map quest tracker, and progress-scaled loot caches onto a curated journey through ten gyms.

## ✨ Features

**The badge journey.** Ten gyms in a fixed route across a hand-built continent, each with its
own **signature arena mechanic** — a floating Bug leader who glides down to meet you, vine
walls that fall as you defeat the garden wardens, a Fairy leader who mirrors the team you
declare, a 2-v-1 Fighting-dojo finale, desert mirages, a tide clock that changes the Water
gym's teams with the weather, a stealth run past frost sentinels, a heat gauge that punishes
lingering in the Fire gym, and more. Beating trainers inside each gym **weakens the leader**
before the title bout, and every badge **raises your level cap** — training past the cap is
capped, so the ladder is the challenge.

**Hardcore + Nuzlocke, enforced by the mod.** Fainting costs real party health or the
Pokémon itself; fleeing demands a **sacrifice**, chosen on a dedicated selection screen; a
full whiteout means the run is over — with a custom Pokéball death screen to see you out.
**Safe zones** (towns and shrine grounds) suppress hostile spawns; the wilds between them do
not. Something in the dark also **whispers** to you out there. It knows your name.

**A story you wake up inside.** You are an amnesiac with a past this world remembers better
than you do. NPCs recognize you — some warmly, some not — and a corporate villain,
**The Company, Inc.**, is quietly buying the world's grain while its agents watch the roads.
A three-act arc runs from scattered field agents through an HQ raid to a boardroom none of
your badges can prepare you for. **Memory fragments** surface after every gym leader falls,
**cutscenes** stage the big reveals, your **PokéPhone** buzzes with calls, and Mom worries
about you the whole way.

**A living quest board.** 70+ authored side quests across every town and route — courier
runs, stealth tails, market audits, missing persons, festivals, and stranger things — all
tracked on a **sidebar quest HUD** with `]`/`[` cycling and live **JourneyMap waypoints**
for the tracked objective. Quest NPCs have real schedules, real dialogue trees, and real
opinions about you.

**An economy under attack.** Prize money, badge-tiered **Pokémart stock** that improves as
you earn badges, a Granary that trades in wheat, roaming wheat buyers, decline fees for
walking away from a fight, and a currency The Company is actively destabilizing — the wheat
war is not a backdrop, it is a questline you can push back on, farm by farm.

**Facilities.** A **Stadium** running level-bracketed exhibition circuits with cloned,
attrition-free teams; a **Daycare** that boards and levels your Pokémon (capped, like
everything else); a **Safari preserve** with timed permits, issued balls, and typed bait
lures; and a post-League **Battle Frontier** of seven halls for the endgame window.

**Boss encounters.** Five optional **elemental shrine trials** — timed parkour, dark
gauntlets, and multi-stage battle gauntlets — and seven **noble encounters**:
Legends-Arceus-style real-time boss fights where you dodge telegraphed elemental attacks,
wear the noble down, and catch the real, perfect-IV Pokémon underneath. A certain playful
legendary would rather be chased than fought. And somewhere mid-journey, a rift opens that
vanilla Minecraft players will find… familiar.

**A watched world.** Hundreds of placed NPCs with dialogue, and a line-of-sight engine that
makes eyes matter: spotters hail you, checkpoints stop you, some agents pursue you on sight,
and stealth quests are actually stealthy. When someone catches you, the camera turns to meet
them — smoothly.

**Naming the fallen — and the found.** Because this is a Nuzlocke, a name is a promise. Every
time a Pokémon joins you — caught, gifted, traded, or your very first partner — the game
pauses to let you **nickname** it on the spot (keep the species name with a keystroke if you'd
rather). A toggle in ModMenu turns the prompt off if you prefer to name them your own way.

**Quality of life.** One-command world install (zones, gamerules, map frontiers pre-baked),
milestone loot caches scaled to your badge count, custom advancements for every arc, ModMenu
config for the flavor systems, and a post-game that simply hands you Minecraft back: walk
off the map edge and live.

## ⌨️ Reference
- **[[Commands]]** — the complete command surface: gameplay, the new player-facing quest tracker, admin/world-setup, and the dev-only authoring tools (flagged for removal at 1.0.0).

## 🗺️ Quests — every objective, region by region
- **[[Quests Overview]]** — how quests light up on the sidebar, tracking, and the full quest index.
- **[[Quests Main Story]]** — the main objective line from Mom's kitchen to the Founder, and the open world beyond.
- **[[Quests Sango Town]]** — the starting town's job board.
- **[[Quests Blossom Path]]** — the meadow road between Sango and Takehara.
- **[[Quests Takehara Falls]]** — the first gym town's side work.
- **[[Quests Harvest Road]]** — wheat country and the first liberation.
- **[[Quests Hua Zhan City]]** — the garden city, the Granary, and the reveal.

## 📖 Guidebook — the campaign, route by route
- **[[Guidebook Overview]]** — how the campaign is structured, the route table, and the three-act arc.
- **[[Guidebook Route Map]]** — the full sequential walkthrough: every town, route, and shrine in order.
- **[[Guidebook Act I]]** — Sango Town + gyms 1–3 (Bug · Grass · Fairy).
- **[[Guidebook Act II]]** — gyms 4–7 (Fighting · Water · Ground · Electric) and the Company HQ raid.
- **[[Guidebook Act III]]** — gyms 8–10 (Dragon · Ice · Fire), the Royal League, the Board of Directors, and the Founder.
- **[[Guidebook Shrines]]** — the five optional elemental shrine trials.
- **[[Guidebook Nobles]]** — the seven Legends-Arceus-style noble boss encounters: real-time dodge-and-melee fights that end in a perfect-IV catch.

---

> ⚠️ The **Guidebook and Quests sections contain story spoilers.** The Commands page tracks the
> live codebase.
>
> **Contributors:** architecture and design docs live in the repo under
> [`docs/`](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/tree/main/docs) —
> the [Architecture Overview](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_OVERVIEW.md)
> (subsystem map) and [Architecture Data Flows](https://github.com/The-Company-Inc-Nerds/the-cobblemon-initiative/blob/main/docs/ARCHITECTURE_DATA_FLOWS.md)
> (mermaid runtime workflows), alongside `LORE_BIBLE.md`, `HARDCORE_RUNBOOK.md`, and `TODO.md`.
