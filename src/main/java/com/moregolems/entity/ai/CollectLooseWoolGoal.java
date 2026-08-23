package com.moregolems.entity.ai;

import com.moregolems.entity.WoolGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Springt ein, wenn {@link ShearNearbySheepGoal} gerade kein scherbereites Schaf findet: sammelt
 * lose herumliegende Wolle im {@link WoolGolem#shearRadius()} um die Truhe ein (z.B. Wolle, die
 * aus einem anderen Grund liegen geblieben ist, oder die ein Spieler dort fallen lässt) und
 * trägt sie über {@link DepositWoolGoal} zur Truhe.
 */
public class CollectLooseWoolGoal extends Goal {

    private static final double REACH_DISTANCE = 1.5;

    private final WoolGolem golem;
    private final double speed;
    private ItemEntity target;

    public CollectLooseWoolGoal(WoolGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.isCarryingWool()) return false;
        BlockPos home = golem.getHomeChestPos();
        if (home == null) return false;

        AABB box = new AABB(home).inflate(golem.shearRadius());
        target = golem.level().getEntitiesOfClass(ItemEntity.class, box, CollectLooseWoolGoal::isWool).stream()
                .min(Comparator.comparingDouble(golem::distanceToSqr))
                .orElse(null);
        return target != null;
    }

    private static boolean isWool(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        return !stack.isEmpty() && stack.is(ItemTags.WOOL);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && isWool(target) && !golem.isCarryingWool();
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
        golem.setCarriedWool(stack.copy());
        target.discard();
    }
}
