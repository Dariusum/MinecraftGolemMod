package com.moregolems.entity.ai;

import com.moregolems.entity.SugarCaneGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Legt getragenes Zuckerrohr in der Truhe ab. Niedrigste Arbeits-Priorität (siehe
 * {@link SugarCaneGolem#registerGoals}), damit ein neu erntereifes oder loses Zuckerrohr im
 * Suchbereich den Golem zuerst weiter arbeiten lässt, statt jeden einzelnen Fund einzeln zur
 * Truhe zu tragen.
 */
public class DepositSugarCaneGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final SugarCaneGolem golem;
    private final double speed;

    public DepositSugarCaneGoal(SugarCaneGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return golem.getCarriedSugarCaneCount() > 0 && golem.getHomeChestPos() != null;
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

        golem.setCarriedSugarCane(ContainerUtil.insert(container, golem.getCarriedSugarCane()));
    }
}
