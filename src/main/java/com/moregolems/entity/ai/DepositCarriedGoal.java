package com.moregolems.entity.ai;

import com.moregolems.entity.CropGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Legt Ernte-Ertrag (z.B. Weizen, Rote Bete — sobald welcher getragen wird) und überzähliges
 * Pflanzgut (sobald kein unbepflanztes Feld im 9x9-Bereich mehr übrig ist) in der Kiste ab.
 * Niedrigere Priorität als Ernten/Pflanzen/Nachschub, damit ein neu erscheinendes reifes Feld
 * oder ein leeres Feld sie zuerst unterbricht.
 */
public class DepositCarriedGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final CropGolem golem;
    private final double speed;

    public DepositCarriedGoal(CropGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (golem.getHomeChestPos() == null) return false;
        boolean hasExcessSeeds = golem.getCarriedSeedCount() > 0 && !CropFieldUtil.anyEmptyFarmland(golem);
        return golem.isCarryingProduct() || hasExcessSeeds;
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

        if (golem.isCarryingProduct()) {
            golem.setCarriedProduct(ContainerUtil.insert(container, golem.getCarriedProduct()));
        }
        if (golem.getCarriedSeedCount() > 0 && !CropFieldUtil.anyEmptyFarmland(golem)) {
            golem.setCarriedSeeds(ContainerUtil.insert(container, golem.getCarriedSeeds()));
        }
    }
}
