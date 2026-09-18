package com.moregolems.entity.ai;

import com.moregolems.entity.MiningGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Niedrigste Bewegungs-Priorität (siehe {@link MiningGolem#registerGoals}): läuft zur Truhe und
 * bleibt dort stehen, sobald weder {@link HarvestMiningGoal} noch {@link DepositMiningGoal} etwas
 * zu tun haben — z.B. weil die Truhe voll ist, oder gerade kein gültiger Block im Arbeitsbereich
 * gefunden wurde. Anders als die Feld-/Ernte-Golems dieses Mods wandert ein Mining-Golem im
 * Leerlauf nicht ziellos umher, sondern kehrt zu seinem Posten zurück.
 */
public class StandByChestGoal extends Goal {

    // Siehe DepositMiningGoal: grosszuegiger als bei den Feld-/Ernte-Golems, da die Zielkachel (auf
    // der Truhe) eine isolierte, von offenem Boden umgebene Kachel ohne "Zulauf" ist.
    private static final double REACH_DISTANCE = 3.0;

    private final MiningGolem golem;
    private final double speed;

    public StandByChestGoal(MiningGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return golem.getHomeChestPos() != null;
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

        // Siehe DepositMiningGoal: der Golem steht AUF seiner Truhe, Zielpunkt ist daher eine Ebene
        // darueber, nicht die Truhen-Position selbst.
        BlockPos standPos = chestPos.above();
        if (golem.distanceToSqr(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5) > REACH_DISTANCE * REACH_DISTANCE) {
            if (golem.getNavigation().isDone()) {
                golem.getNavigation().moveTo(standPos.getX() + 0.5, standPos.getY(), standPos.getZ() + 0.5, speed);
            }
        } else {
            golem.getNavigation().stop();
        }
    }
}
