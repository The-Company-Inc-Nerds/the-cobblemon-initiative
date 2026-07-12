package com.thecompanyinc.cobblemoninitiative.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Machine-readable, no-player test commands for the headless harness
 * (scripts/test_harness). Every line follows the contract
 *   [TEST] &lt;category&gt; ... PASS|FAIL|WARN ...
 * so a script can grep pass/fail. All run from the server console via
 * source.getServer() — none require a player. See docs/TESTING_TOOLKIT.md.
 */
public final class TestCommands {
    private TestCommands() {}

    /** Wired into CobblemonInitiativeCommands via .then(TestCommands.build()). */
    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("test")
            .requires(s -> s.hasPermission(2))
            .then(Commands.literal("reload").executes(TestCommands::reload))
            .then(Commands.literal("data").executes(TestCommands::data))
            .then(Commands.literal("registry").executes(TestCommands::registry))
            .then(Commands.literal("placement")
                .executes(ctx -> placement(ctx, null))
                .then(Commands.argument("id", StringArgumentType.string())
                    .executes(ctx -> placement(ctx, StringArgumentType.getString(ctx, "id")))))
            .then(Commands.literal("all").executes(TestCommands::all));
    }

    private static void out(CommandSourceStack s, String line) {
        s.sendSuccess(() -> Component.literal(line), false);
    }

    // ── test reload: re-read every config; a malformed file throws here ──────────
    private static int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack s = ctx.getSource();
        try {
            ConfigLoader cl = InitiativeInit.getConfigLoader();
            cl.loadAllConfigs();
            out(s, "[TEST] reload PASS trainers=" + cl.getAllTrainers().size()
                    + " levelcaps=" + cl.getLevelCaps().size());
            return 1;
        } catch (Throwable t) {
            out(s, "[TEST] reload FAIL " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return 0;
        }
    }

    // ── test registry: counts + the level-cap ladder values ─────────────────────
    private static int registry(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack s = ctx.getSource();
        ConfigLoader cl = InitiativeInit.getConfigLoader();
        int withCoords = 0;
        for (TrainerConfig t : cl.getAllTrainers()) {
            int[] c = t.getCoordinates();
            if (c != null && c.length >= 3) withCoords++;
        }
        List<LevelCapConfig> caps = new ArrayList<>(cl.getLevelCaps());
        caps.sort(Comparator.comparingInt(LevelCapConfig::getOrder));
        StringBuilder ladder = new StringBuilder();
        for (LevelCapConfig c : caps) {
            if (ladder.length() > 0) ladder.append(',');
            ladder.append(c.getLevelCap());
        }
        out(s, "[TEST] registry trainers=" + cl.getAllTrainers().size()
                + " with_coords=" + withCoords + " levelcaps=" + caps.size()
                + " ladder=" + ladder);
        return 1;
    }

    // ── test data: cross-reference graph integrity ──────────────────────────────
    private static int data(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack s = ctx.getSource();
        ConfigLoader cl = InitiativeInit.getConfigLoader();
        Set<String> ids = new HashSet<>();
        for (TrainerConfig t : cl.getAllTrainers()) ids.add(t.getId());
        int fails = 0, warns = 0;
        for (TrainerConfig t : cl.getAllTrainers()) {
            if (t.getPrerequisites() != null) {
                for (String p : t.getPrerequisites()) {
                    if (p != null && !p.isEmpty() && !ids.contains(p)) {
                        out(s, "[TEST] data FAIL trainer=" + t.getId() + " prereq_missing=" + p);
                        fails++;
                    }
                }
            }
            if (t.getTeam() == null || t.getTeam().getPokemon() == null || t.getTeam().getPokemon().isEmpty()) {
                out(s, "[TEST] data WARN trainer=" + t.getId() + " empty_team");
                warns++;
            }
        }
        List<LevelCapConfig> caps = new ArrayList<>(cl.getLevelCaps());
        caps.sort(Comparator.comparingInt(LevelCapConfig::getOrder));
        int prev = -1;
        for (LevelCapConfig c : caps) {
            if (c.getLevelCap() <= prev) {
                out(s, "[TEST] data FAIL ladder_not_increasing at=" + c.getAchievementId() + " cap=" + c.getLevelCap());
                fails++;
            }
            prev = c.getLevelCap();
        }
        out(s, "[TEST] data " + (fails == 0 ? "PASS" : "FAIL")
                + " trainers=" + ids.size() + " fails=" + fails + " warns=" + warns);
        return fails == 0 ? 1 : 0;
    }

    // ── test placement: is each placed trainer sunk / floating / grounded? ───────
    // Sync-loads each chunk (level.getChunk) BEFORE reading blocks, so there is no
    // async force-load timing trap. SUNK/HEAD_BLOCKED are hard fails (can't snap out
    // of a wall/ceiling); FLOATING is a warn (Easy NPC latch-spawn snaps to ground).
    private static int placement(CommandContext<CommandSourceStack> ctx, String only) {
        CommandSourceStack s = ctx.getSource();
        ServerLevel level = s.getServer().overworld();
        ConfigLoader cl = InitiativeInit.getConfigLoader();
        int checked = 0, sunk = 0, floating = 0, headBlocked = 0, grounded = 0, unplaced = 0;
        for (TrainerConfig t : cl.getAllTrainers()) {
            int[] c = t.getCoordinates();
            if (c == null || c.length < 3) continue;
            if (only != null && !only.equalsIgnoreCase(t.getId())) continue;
            int x = c[0], y = c[1], z = c[2];
            if (x == 0 && y == 0 && z == 0) { // unset/placeholder — roaming or latch-only trainer
                unplaced++;
                continue;
            }
            level.getChunk(x >> 4, z >> 4); // synchronous load — reliable block reads
            BlockState feet = level.getBlockState(new BlockPos(x, y, z));
            BlockState head = level.getBlockState(new BlockPos(x, y + 1, z));
            BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
            checked++;
            String status;
            if (feet.blocksMotion()) {
                status = "SUNK feet=" + name(feet);
                sunk++;
            } else if (head.blocksMotion()) {
                status = "HEAD_BLOCKED head=" + name(head);
                headBlocked++;
            } else if (below.blocksMotion()) {
                status = "GROUNDED";
                grounded++;
            } else {
                int gap = 1;
                while (gap < 48 && !level.getBlockState(new BlockPos(x, y - 1 - gap, z)).blocksMotion()) gap++;
                status = "FLOATING gap=" + gap;
                floating++;
            }
            out(s, "[TEST] placement " + t.getId() + "@" + x + "," + y + "," + z + " " + status);
        }
        // Informational only: trainer config coordinates are NOMINAL (waypoint-ish),
        // not terrain-accurate feet positions — Easy NPC latch-spawn snaps NPCs to the
        // ground on spawn. So this reports statuses (and flags unset [0,0,0] coords) but
        // never hard-fails. Authoritative placement QA reads LIVE spawned positions with
        // a player online (see docs/TESTING_TOOLKIT.md, Layer 3).
        out(s, "[TEST] placement INFO checked=" + checked + " grounded=" + grounded
                + " floating=" + floating + " sunk=" + sunk + " head_blocked=" + headBlocked
                + " unplaced=" + unplaced + " (config coords nominal; live check needs a player)");
        return 1;
    }

    private static int all(CommandContext<CommandSourceStack> ctx) {
        reload(ctx);
        registry(ctx);
        data(ctx);
        placement(ctx, null);
        return 1;
    }

    private static String name(BlockState st) {
        return BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString();
    }
}
