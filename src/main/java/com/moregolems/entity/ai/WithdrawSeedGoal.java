package com.moregolems.entity.ai;

import com.moregolems.entity.CropGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Holt Pflanzgut-Nachschub aus der Kiste, wenn der Golem keines mehr trägt, aber noch
 * unbepflanzte Felder im 9x9-Bereich (max. 4 Blöcke Abstand) existieren — nimmt genau so viele
 * mit, wie gerade an leeren Feldern bekannt sind (gedeckelt durch Stapelgröße und Kisteninhalt).
 */
public class WithdrawSeedGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;
    private static final int MAX_STACK = 64;

    private final CropGolem golem;
    private final double speed;

    public WithdrawSeedGoal(CropGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedSeedCount() > 0) return false;
        BlockPos home = golem.getHomeChestPos();
        if (home == null) return false;
        if (CropFieldUtil.countEmptyFarmland(golem) <= 0) return false;
        return chestSeedCount(home) > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    private int chestSeedCount(BlockPos chestPos) {
        if (!(golem.level() instanceof ServerLevel server)) return 0;
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return 0;
        return ContainerUtil.countItem(container, golem.seedItem());
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

        int needed = Math.max(1, Math.min(CropFieldUtil.countEmptyFarmland(golem), MAX_STACK));
        golem.setCarriedSeeds(ContainerUtil.extract(container, golem.seedItem(), needed));
    }
}
