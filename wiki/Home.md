# The Cobblemon Initiative

A single-player **Fabric** mod (Minecraft 1.21.1 + Cobblemon 1.7.3, currently **0.5.0-alpha.4**) built for UPM 2 and played as a live **hardcore + Nuzlocke** production. It layers gym-badge progression, level caps, Nuzlocke death mechanics, an amnesiac-founder mystery, a destabilizing in-world economy, elemental shrine challenges, a full side-quest board with an on-map quest tracker, and progress-scaled loot caches onto a curated journey through ten gyms.

## 📐 Architecture
- **[[Architecture Overview]]** — the nine subsystems, the Fabric init/lifecycle flow, runtime-mod integrations, and the recurring design patterns.
- **[[Architecture Data Flows]]** — mermaid workflows for every key system: battle → badge → level cap, faint → Nuzlocke → Dark Urge, NPC Sight, Memory Fragments, the Wheat War economy, the Quest HUD, the quest tracker (`]` / `[` keybinds → `/cobblemon-initiative track` → aqua ▶ sidebar highlight → JourneyMap waypoint, with a light-beam fallback), and the loot-chest system (unplaced chests stocked once from badge-tier loot tables, with a 75% empty roll).

## ⌨️ Reference
- **[[Commands]]** — the complete command surface: gameplay, the new player-facing quest tracker, admin/world-setup, and the dev-only authoring tools (flagged for removal at 1.0.0).

## 🗺️ Quests — every objective, region by region
- **[[Quests Overview]]** — how quests light up on the sidebar, tracking, and the full quest index.
- **[[Quests Main Story]]** — the main objective line from Mom's kitchen to the Ender Dragon.
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

---

> ⚠️ The **Guidebook and Quests sections contain story spoilers.** The Architecture and Commands pages track the
> live codebase. See also `docs/LORE_BIBLE.md`, `docs/HARDCORE_RUNBOOK.md`, and `TODO.md` in
> the repository.
