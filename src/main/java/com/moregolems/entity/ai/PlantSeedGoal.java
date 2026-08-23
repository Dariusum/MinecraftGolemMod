package com.moregolems.entity.ai;

import com.moregolems.entity.CropGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;

/** Bepflanzt ein leeres Ackerfeld im 9x9-Bereich (max. 4 Blöcke Abstand) um die Kiste mit getragenem Pflanzgut. */
public class PlantSeedGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final CropGolem golem;
    private final double speed;
    private BlockPos target;

    public PlantSeedGoal(CropGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedSeedCount() <= 0) return false;
        target = findEmptyFarmland();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && golem.getCarriedSeedCount() > 0 && CropFieldUtil.isEmptyFarmland(golem.level(), target);
    }

    private BlockPos findEmptyFarmland() {
        return golem.fieldPositions().stream()
                .filter(pos -> CropFieldUtil.isEmptyFarmland(golem.level(), pos))
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
        if (!CropFieldUtil.isEmptyFarmland(server, target)) {
            target = null;
            return;
        }

        if (golem.tryConsumeSeed()) {
            server.setBlock(target.above(), golem.cropBlock().defaultBlockState(), 3);
        }
        target = null;
    }
}
