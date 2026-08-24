package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.EnumSet;

/** Bepflanzt einen leeren Erdblock im 10x10-Feld um die Truhe mit getragenem Bambus. */
public class PlantBambooGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final BambooGolem golem;
    private final double speed;
    private BlockPos target;

    public PlantBambooGoal(BambooGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedBambooCount() <= 0) return false;
        target = findEmptyDirt();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && golem.getCarriedBambooCount() > 0 && BambooFieldUtil.isEmptyDirt(golem.level(), target);
    }

    private BlockPos findEmptyDirt() {
        return golem.fieldPositions().stream()
                .filter(pos -> BambooFieldUtil.isEmptyDirt(golem.level(), pos))
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
        if (!BambooFieldUtil.isEmptyDirt(server, target)) {
            target = null;
            return;
        }

        if (golem.tryConsumeBamboo()) {
            server.setBlock(target.above(), Blocks.BAMBOO_SAPLING.defaultBlockState(), 3);
        }
        target = null;
    }
}
