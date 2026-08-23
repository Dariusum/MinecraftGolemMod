package com.moregolems.entity.ai;

import com.moregolems.entity.WoolGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Sucht das nächste scherbereite Schaf im {@link WoolGolem#shearRadius()} um die Truhe des
 * Golems (nicht um den Golem selbst — er darf beim Suchen/Tragen von seiner Truhe wegwandern),
 * läuft hin und schert es. Die dabei fallengelassene Wolle wird sofort eingesammelt (der Golem
 * steht direkt daneben) statt liegen zu bleiben — [[DepositWoolGoal]] trägt sie danach zur Truhe.
 */
public class ShearNearbySheepGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final WoolGolem golem;
    private final double speed;
    private Sheep target;

    public ShearNearbySheepGoal(WoolGolem golem, double speed) {
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
        target = golem.level().getEntitiesOfClass(Sheep.class, box, Sheep::readyForShearing).stream()
                .min(Comparator.comparingDouble(golem::distanceToSqr))
                .orElse(null);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && target.readyForShearing() && !golem.isCarryingWool();
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
        if (!(golem.level() instanceof ServerLevel server)) return;

        AABB dropArea = target.getBoundingBox().inflate(0.8);
        target.shear(server, SoundSource.NEUTRAL, ItemStack.EMPTY);

        // Der Golem steht direkt daneben — die eben erzeugten Wolle-Drops sofort einsammeln,
        // statt eine zusätzliche "lauf zum Item"-Logik für eine Distanz von <1 Block zu bauen.
        // Scheren erzeugt üblicherweise mehrere Wolle-ItemEntities (1-3 Stück je Vorgang) — alle
        // zu einem Stapel zusammenführen, statt (wie zuvor) nur den ersten mitzunehmen und den
        // Rest liegen zu lassen.
        List<ItemEntity> drops = server.getEntitiesOfClass(ItemEntity.class, dropArea);
        ItemStack collected = ItemStack.EMPTY;
        for (ItemEntity drop : drops) {
            ItemStack dropped = drop.getItem();
            if (dropped.isEmpty()) continue;
            if (collected.isEmpty()) {
                collected = dropped.copy();
            } else if (ItemStack.isSameItemSameComponents(collected, dropped)) {
                collected.grow(dropped.getCount());
            } else {
                continue; // andere Drops (sollte bei Scherwolle nicht vorkommen) unangetastet lassen
            }
            drop.discard();
        }
        if (!collected.isEmpty()) {
            golem.setCarriedWool(collected);
        }
    }
}
