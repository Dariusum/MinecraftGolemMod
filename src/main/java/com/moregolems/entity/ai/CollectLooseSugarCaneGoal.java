package com.moregolems.entity.ai;

import com.moregolems.entity.SugarCaneGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Sammelt lose herumliegendes Zuckerrohr (von {@link HarvestSugarCaneGoal} abgeworfen, oder von
 * einem Spieler dort fallen gelassen) im Umkreis des 20-Block-Suchbereichs ein und trägt es über
 * {@link DepositSugarCaneGoal} weiter zur Truhe.
 */
public class CollectLooseSugarCaneGoal extends Goal {

    private static final double REACH_DISTANCE = 1.5;
    /** Deckt den 20-Block-Suchbereich plus etwas Rand für gefallene Drops ab. */
    private static final double SEARCH_RADIUS = 22.0;

    private final SugarCaneGolem golem;
    private final double speed;
    private ItemEntity target;

    public CollectLooseSugarCaneGoal(SugarCaneGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockPos home = golem.getHomeChestPos();
        if (home == null) return false;

        AABB box = new AABB(home).inflate(SEARCH_RADIUS);
        target = golem.level().getEntitiesOfClass(ItemEntity.class, box, CollectLooseSugarCaneGoal::isSugarCane).stream()
                .min(Comparator.comparingDouble(golem::distanceToSqr))
                .orElse(null);
        return target != null;
    }

    private static boolean isSugarCane(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        return !stack.isEmpty() && stack.is(Items.SUGAR_CANE);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && isSugarCane(target);
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target, speed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        golem.getLookControl().setLookAt(target, 30f, 30f);

        if (golem.distanceToSqr(target) > REACH_DISTANCE * REACH_DISTANCE) {
            golem.getNavigation().moveTo(target, speed);
            return;
        }

        golem.getNavigation().stop();
        ItemStack stack = target.getItem();
        if (stack.isEmpty()) return;
        golem.addCarriedSugarCane(stack.copy());
        target.discard();
    }
}
