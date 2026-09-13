package com.moregolems.entity.ai;

import com.moregolems.entity.SugarCaneGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Erntet einen hochgewachsenen Zuckerrohr-Stapel (mind. 2 Segmente) im 20-Block-Umkreis um die
 * Truhe: bricht nur das Segment direkt über dem untersten (das unterste bleibt stehen und wächst
 * von dort weiter nach) — die darüberliegenden Segmente verlieren dadurch ihren Halt und fallen
 * automatisch mit ab (Vanilla-Physik). {@link CollectLooseSugarCaneGoal} sammelt die Drops danach ein.
 *
 * Der volle Suchbereich (bis zu ~1300 Positionen bei Radius 20) wird nicht jeden Tick abgesucht:
 * findet ein Durchlauf nichts Erntereifes, wartet das Ziel {@link #RESCAN_INTERVAL} Ticks, bevor
 * erneut gesucht wird — sonst würde der Golem im Leerlauf (kein reifes Zuckerrohr vorhanden)
 * mehrmals pro Sekunde den gesamten Bereich absuchen.
 */
public class HarvestSugarCaneGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;
    /** Deckelt die Höhenzählung — Zuckerrohr wächst vanilla nur bis Höhe 3, etwas Sicherheitsrand. */
    private static final int MAX_HEIGHT = 16;
    private static final int RESCAN_INTERVAL = 20;

    private final SugarCaneGolem golem;
    private final double speed;
    private BlockPos target;
    private int ticksUntilRescan;

    public HarvestSugarCaneGoal(SugarCaneGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private static int columnHeight(Level level, BlockPos fieldPos) {
        int height = 0;
        BlockPos pos = fieldPos.above();
        while (height < MAX_HEIGHT && level.getBlockState(pos).is(Blocks.SUGAR_CANE)) {
            height++;
            pos = pos.above();
        }
        return height;
    }

    @Override
    public boolean canUse() {
        if (ticksUntilRescan > 0) {
            ticksUntilRescan--;
            return false;
        }
        target = findHarvestableColumn();
        if (target == null) {
            ticksUntilRescan = RESCAN_INTERVAL;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && columnHeight(golem.level(), target) >= 2;
    }

    private BlockPos findHarvestableColumn() {
        Level level = golem.level();
        return golem.fieldPositions().stream()
                .filter(pos -> columnHeight(level, pos) >= 2)
                .min(Comparator.comparingDouble(pos -> golem.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)))
                .orElse(null);
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
            golem.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, speed);
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;
        if (columnHeight(server, target) < 2) {
            target = null;
            return;
        }

        // target = Bodenblock; target.above() = unterstes Segment (bleibt stehen);
        // target.above().above() = erstes zu erntendes Segment.
        BlockPos breakPos = target.above().above();
        server.destroyBlock(breakPos, true, golem, 512);
        target = null;
    }
}
