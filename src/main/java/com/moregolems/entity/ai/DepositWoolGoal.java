package com.moregolems.entity.ai;

import com.moregolems.entity.WoolGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Trägt die zuletzt geschorene Wolle ({@link WoolGolem#getCarriedWool()}) zur Truhe des Golems und legt sie hinein. */
public class DepositWoolGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final WoolGolem golem;
    private final double speed;

    public DepositWoolGoal(WoolGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return golem.isCarryingWool() && golem.getHomeChestPos() != null;
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

        golem.setCarriedWool(ContainerUtil.insert(container, golem.getCarriedWool()));
    }
}
