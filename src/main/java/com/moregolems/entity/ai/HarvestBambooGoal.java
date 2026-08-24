package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Erntet einen hochgewachsenen Bambus-Stapel (mind. 2 Segmente) im 10x10-Feld um die Truhe: bricht
 * nur das Segment direkt über dem untersten (das unterste bleibt stehen und wächst von dort weiter
 * nach) — die darüberliegenden Segmente verlieren dadurch ihren Halt und fallen automatisch mit ab
 * (Vanilla-Physik, wie bei Zuckerrohr). {@link CollectLooseBambooGoal} sammelt die Drops danach ein.
 */
public class HarvestBambooGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final BambooGolem golem;
    private final double speed;
    private BlockPos target;

    public HarvestBambooGoal(BambooGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = findHarvestableColumn();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && BambooFieldUtil.columnHeight(golem.level(), target) >= 2;
    }

    private BlockPos findHarvestableColumn() {
        Level level = golem.level();
        return golem.fieldPositions().stream()
                .filter(pos -> BambooFieldUtil.columnHeight(level, pos) >= 2)
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
        if (BambooFieldUtil.columnHeight(server, target) < 2) {
            target = null;
            return;
        }

        // target = Erdblock; target.above() = unterstes Segment (bleibt stehen);
        // target.above().above() = erstes zu erntendes Segment.
        BlockPos breakPos = target.above().above();
        server.destroyBlock(breakPos, true, golem, 512);
        target = null;
    }
}
