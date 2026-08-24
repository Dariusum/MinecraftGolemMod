package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Holt Bambus-Nachschub aus der Truhe, wenn der Golem keinen mehr trägt, aber noch unbepflanzte
 * Erdblöcke im 10x10-Feld existieren — nimmt genau so viele mit, wie gerade an leeren Feldern
 * bekannt sind (gedeckelt durch Stapelgröße und Kisteninhalt).
 */
public class WithdrawBambooGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;
    private static final int MAX_STACK = 64;

    private final BambooGolem golem;
    private final double speed;

    public WithdrawBambooGoal(BambooGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedBambooCount() > 0) return false;
        BlockPos home = golem.getHomeChestPos();
        if (home == null) return false;
        if (BambooFieldUtil.countEmptyDirt(golem) <= 0) return false;
        return chestBambooCount(home) > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    private int chestBambooCount(BlockPos chestPos) {
        if (!(golem.level() instanceof ServerLevel server)) return 0;
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return 0;
        return ContainerUtil.countItem(container, Items.BAMBOO);
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

        int needed = Math.max(1, Math.min(BambooFieldUtil.countEmptyDirt(golem), MAX_STACK));
        golem.setCarriedBamboo(ContainerUtil.extract(container, Items.BAMBOO, needed));
    }
}
