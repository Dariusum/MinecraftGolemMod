package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Legt getragenen Bambus in der Truhe ab, sobald der Golem ihn nicht mehr zum Bepflanzen braucht
 * (kein unbepflanzter Erdblock im 10x10-Feld mehr übrig). Niedrigste Arbeits-Priorität, damit ein
 * neu erscheinendes leeres Feld ihn zuerst zum Pflanzen abzweigt.
 */
public class DepositBambooGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final BambooGolem golem;
    private final double speed;

    public DepositBambooGoal(BambooGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedBambooCount() <= 0) return false;
        if (golem.getHomeChestPos() == null) return false;
        return !BambooFieldUtil.anyEmptyDirt(golem);
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

        golem.setCarriedBamboo(ContainerUtil.insert(container, golem.getCarriedBamboo()));
    }
}
