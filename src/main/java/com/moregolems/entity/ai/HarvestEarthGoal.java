package com.moregolems.entity.ai;

import com.moregolems.entity.EarthGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Findet den nächsten abbaubaren Erd-/Grasblock im quadratischen {@link #WORK_RADIUS}-Bereich um
 * die Truhe, läuft hin und baut ihn ab — der Golem trägt danach sichtbar genau diesen einen Block
 * (siehe {@link EarthGolem#setCarriedBlock}), bis {@link DepositEarthGoal} ihn in der Truhe abliegt.
 *
 * Regeln für einen gültigen Zielblock (siehe {@link #findTarget}):
 * <ol>
 *     <li>Nur auf oder über Truhen-Höhe.</li>
 *     <li>Nur Blöcke, die direkt exponiert sind (nichts liegt direkt darüber) — effizient über
 *     {@link Level#getHeight(Heightmap.Types, int, int)} ermittelt: der Golem bearbeitet damit
 *     immer nur den jeweils obersten Block einer Spalte. Blätter zählen dabei bewusst nicht als
 *     Hindernis ({@code MOTION_BLOCKING_NO_LEAVES}), sonst wäre unter jedem Baum kein Abbau
 *     möglich.</li>
 *     <li>Der Abbau darf keine Stufe von mehr als 1 Block zu einer der 4 orthogonal benachbarten
 *     Spalten erzeugen (verhindert Klippen/unpassierbare Kanten).</li>
 *     <li>Von allen so gültigen Blöcken wird der nächstgelegene gewählt, bei Gleichstand der
 *     nördlichste (kleinstes Z), bei erneutem Gleichstand der östlichste (größtes X) — zusammen
 *     also der nordöstlichste unter den nächstgelegenen.</li>
 * </ol>
 *
 * Der volle Suchbereich wird nicht jeden Tick abgesucht: findet ein Durchlauf nichts, wartet das
 * Ziel {@link #RESCAN_INTERVAL} Ticks (analog {@code HarvestPumpkinGoal}).
 */
public class HarvestEarthGoal extends Goal {

    /** Quadratischer Suchradius um die Truhe (Chebyshev-Distanz), leicht anpassbar. */
    static final int WORK_RADIUS = 16;

    private static final double REACH_DISTANCE = 2.0;
    private static final int RESCAN_INTERVAL = 40;

    private final EarthGolem golem;
    private final double speed;
    private BlockPos target;
    private int ticksUntilRescan;

    public HarvestEarthGoal(EarthGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private static boolean isEarthBlock(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK);
    }

    /** Y-Position des obersten, nicht-luftigen Blocks der Spalte (Regel 2, siehe Klassendoc). */
    private static int surfaceY(Level level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
    }

    @Override
    public boolean canUse() {
        if (golem.isCarryingBlock()) return false;
        BlockPos chestPos = golem.getHomeChestPos();
        if (chestPos == null) return false;
        if (!(golem.level() instanceof ServerLevel server)) return false;
        if (server.getBlockEntity(chestPos) instanceof Container container && ContainerUtil.isFull(container)) {
            return false;
        }

        if (ticksUntilRescan > 0) {
            ticksUntilRescan--;
            return false;
        }
        target = findTarget(server, chestPos);
        if (target == null) {
            ticksUntilRescan = RESCAN_INTERVAL;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null) return false;
        BlockState state = golem.level().getBlockState(target);
        return isEarthBlock(state);
    }

    private BlockPos findTarget(ServerLevel server, BlockPos chestPos) {
        List<BlockPos> candidates = new ArrayList<>();

        for (int dx = -WORK_RADIUS; dx <= WORK_RADIUS; dx++) {
            for (int dz = -WORK_RADIUS; dz <= WORK_RADIUS; dz++) {
                int x = chestPos.getX() + dx;
                int z = chestPos.getZ() + dz;
                int y = surfaceY(server, x, z);
                if (y < chestPos.getY()) continue; // Regel 1

                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = server.getBlockState(pos);
                if (!isEarthBlock(state)) continue;

                if (!respectsStepLimit(server, pos)) continue; // Regel 3

                candidates.add(pos);
            }
        }

        Comparator<BlockPos> byDistanceThenNortheast = Comparator
                .comparingDouble((BlockPos pos) -> golem.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5))
                .thenComparingInt(BlockPos::getZ) // noerdlichster (kleinstes Z)
                .thenComparingInt((BlockPos pos) -> -pos.getX()); // oestlichster (groesstes X)

        return candidates.stream().min(byDistanceThenNortheast).orElse(null);
    }

    /**
     * Regel 3: nach dem Abbau von [pos] entsteht die neue Oberflaechen-Hoehe pos.Y-1. Vergleicht
     * diese mit der aktuellen Oberflaechen-Hoehe der 4 orthogonalen Nachbarspalten - ein
     * Unterschied von mehr als 1 Block waere eine zu hohe Stufe.
     */
    private static boolean respectsStepLimit(ServerLevel server, BlockPos pos) {
        int newHeight = pos.getY() - 1;
        for (BlockPos neighbor : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
            int neighborHeight = surfaceY(server, neighbor.getX(), neighbor.getZ());
            if (Math.abs(newHeight - neighborHeight) > 1) return false;
        }
        return true;
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, speed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);

        if (golem.distanceToSqr(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5) > REACH_DISTANCE * REACH_DISTANCE) {
            // Nur neu anfordern, wenn die Navigation nicht schon unterwegs ist - siehe
            // DepositEarthGoal fuer die ausfuehrliche Begruendung.
            if (golem.getNavigation().isDone()) {
                golem.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, speed);
            }
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;

        BlockPos pos = target;
        target = null;
        BlockState state = server.getBlockState(pos);
        if (!isEarthBlock(state)) return;

        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        server.levelEvent(2001, pos, Block.getId(state));

        // Der Golem traegt sichtbar genau den abgebauten Blocktyp und legt ihn spaeter unveraendert
        // als 1 Item in der Truhe ab (DepositEarthGoal) - keine Loot-Table-Ermittlung noetig, Erde/
        // Grasblock droppen ohnehin immer genau sich selbst.
        golem.setCarriedBlock(state.getBlock().defaultBlockState());
    }
}
