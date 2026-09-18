package com.moregolems.entity.ai;

import com.moregolems.entity.MiningGolem;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Trägt den vom Golem gehaltenen Block (siehe {@link HarvestMiningGoal}) zur Truhe und legt ihn
 * dort als ein Item ab. Ist die Truhe voll, schlägt das Ablegen fehl und der Golem bleibt (schon an
 * der Truhe angekommen) einfach stehen — das erfüllt implizit "bleibt vor der Kiste stehen bei
 * voller Kiste", ohne dass dafür ein Sonderfall nötig wäre.
 */
public class DepositMiningGoal extends Goal {

    // Grosszuegiger als bei den Feld-/Ernte-Golems dieses Mods (dort 2.0): der Golem steht AUF
    // seiner Truhe, einer einzelnen, von offenem Boden umgebenen Zielkachel ohne "Zulauf" - die
    // Wegfindung naehert sich ihr empirisch manchmal nur bis auf ~2.2-2.5 Bloecke an, statt exakt
    // zu zentrieren.
    private static final double REACH_DISTANCE = 3.0;

    private final MiningGolem golem;
    private final double speed;

    public DepositMiningGoal(MiningGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return golem.getHomeChestPos() != null && golem.isCarryingBlock();
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

        // Der Golem steht AUF seiner Truhe (siehe WorldEventHandler#trySpawnMiningGolem), anders
        // als bei den Feld-/Ernte-Golems dieses Mods, die NEBEN ihrer Truhe auf derselben Ebene
        // stehen - Zielpunkt ist daher eine Ebene ueber der Truhe, sonst zielt die Navigation in
        // den soliden Truhen-/Boden-Block hinein und findet nie einen gueltigen Pfad.
        BlockPos standPos = chestPos.above();
        if (golem.distanceToSqr(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5) > REACH_DISTANCE * REACH_DISTANCE) {
            // Nur neu anfordern, wenn die Navigation nicht schon unterwegs ist - staendiges
            // Neuberechnen des Pfads jeden Tick verhindert sonst, dass ueberhaupt Fortschritt
            // gemacht wird.
            if (golem.getNavigation().isDone()) {
                golem.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, speed);
            }
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return;

        BlockState carried = golem.getCarriedBlock();
        if (carried == null) return;

        ItemStack remaining = ContainerUtil.insert(container, new ItemStack(carried.getBlock().asItem()));
        if (remaining.isEmpty()) {
            golem.setCarriedBlock(null);
        }
        // Sonst (Truhe voll) bleibt der Block getragen - der Golem steht bereits hier und versucht
        // es naechsten Tick erneut, bis wieder Platz ist.
    }
}
