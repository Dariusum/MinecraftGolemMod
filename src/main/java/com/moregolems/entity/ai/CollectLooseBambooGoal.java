package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Sammelt lose herumliegenden Bambus (z.B. von {@link HarvestBambooGoal} abgeworfen, oder von
 * einem Spieler dort fallen gelassen) im Umkreis des 10x10-Felds ein und trägt ihn über
 * {@link DepositBambooGoal} / {@link PlantBambooGoal} weiter.
 */
public class CollectLooseBambooGoal extends Goal {

    private static final double REACH_DISTANCE = 1.5;
    /** Deckt das 10x10-Feld (Kante 5 Blöcke vom Truhen-Mittelpunkt) plus etwas Rand für gefallene Drops ab. */
    private static final double SEARCH_RADIUS = 8.0;

    private final BambooGolem golem;
    private final double speed;
    private ItemEntity target;

    public CollectLooseBambooGoal(BambooGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.getCarriedBambooCount() > 0) return false;
        BlockPos home = golem.getHomeChestPos();
        if (home == null) return false;

        AABB box = new AABB(home).inflate(SEARCH_RADIUS);
        target = golem.level().getEntitiesOfClass(ItemEntity.class, box, CollectLooseBambooGoal::isBamboo).stream()
                .min(Comparator.comparingDouble(golem::distanceToSqr))
                .orElse(null);
        return target != null;
    }

    private static boolean isBamboo(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        return !stack.isEmpty() && stack.is(Items.BAMBOO);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && isBamboo(target) && golem.getCarriedBambooCount() <= 0;
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
        golem.addCarriedBamboo(stack.copy());
        target.discard();
    }
}
