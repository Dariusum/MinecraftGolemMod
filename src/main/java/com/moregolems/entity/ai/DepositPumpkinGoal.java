package com.moregolems.entity.ai;

import com.moregolems.entity.PumpkinGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Legt sowohl die beim Schnitzen gewonnenen Kürbiskerne als auch die abgebauten geschnitzten
 * Kürbisse in der Truhe ab. Niedrigste Arbeits-Priorität (siehe
 * {@link PumpkinGolem#registerGoals}), damit ein neu gefundener Kürbis den Golem zuerst weiter
 * ernten lässt, statt jeden einzelnen Fund einzeln zur Truhe zu tragen.
 */
public class DepositPumpkinGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final PumpkinGolem golem;
    private final double speed;

    public DepositPumpkinGoal(PumpkinGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (golem.getHomeChestPos() == null) return false;
        return golem.getCarriedSeedCount() > 0 || golem.isCarryingPumpkin();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        BlockPos chestPos = golem.getHomeChestPos();
        if (chestPos == null) return;

        if (golem.blockPosition().distSqr(chestPos) > REACH_DISTANCE * REACH_DISTANCE) {
            golem.getNavigation().moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, speed);
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return;

        if (golem.getCarriedSeedCount() > 0) {
            golem.setCarriedSeeds(ContainerUtil.insert(container, golem.getCarriedSeeds()));
        }
        if (golem.isCarryingPumpkin()) {
            golem.setCarriedPumpkins(ContainerUtil.insert(container, golem.getCarriedPumpkins()));
        }
    }
}
