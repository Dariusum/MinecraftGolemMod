package com.moregolems.entity.ai;

import com.moregolems.entity.EarthGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Niedrigste Bewegungs-Priorität (siehe {@link EarthGolem#registerGoals}): läuft zur Truhe und
 * bleibt dort stehen, sobald weder {@link HarvestEarthGoal} noch {@link DepositEarthGoal} etwas zu
 * tun haben — z.B. weil die Truhe voll ist, oder gerade kein gültiger Block im Arbeitsbereich
 * gefunden wurde. Anders als die übrigen Golems dieses Mods wandert der Erdgolem im Leerlauf nicht
 * ziellos umher, sondern kehrt zu seinem Posten zurück.
 */
public class StandByChestGoal extends Goal {

    // Siehe DepositEarthGoal: grosszuegiger als bei den uebrigen Golems, da die Zielkachel (auf der
    // Truhe) eine isolierte, von offenem Boden umgebene Kachel ohne "Zulauf" ist.
    private static final double REACH_DISTANCE = 3.0;

    private final EarthGolem golem;
    private final double speed;

    public StandByChestGoal(EarthGolem golem, double speed) {
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

        // Siehe DepositEarthGoal: der Golem steht AUF seiner Truhe, Zielpunkt ist daher eine Ebene
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
