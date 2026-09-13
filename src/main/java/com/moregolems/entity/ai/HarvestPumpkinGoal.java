package com.moregolems.entity.ai;

import com.moregolems.entity.PumpkinGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Findet einen ausgewachsenen, ungeschnitzten Kürbis ({@code Blocks.PUMPKIN}) im 100-Block-Umkreis
 * um die Truhe, läuft hin und bearbeitet ihn in einem Zug:
 * <ol>
 *     <li>Schnitzen (wie ein Spieler mit einer Schere): der Block wird zu einem geschnitzten
 *     Kürbis, 4 Kürbiskerne fallen ab — hier direkt dem Golem gutgeschrieben statt physisch
 *     fallen zu lassen (analog {@code HarvestCropGoal}).</li>
 *     <li>Sofortiger Abbau des geschnitzten Kürbisses — der Drop (der geschnitzte Kürbis selbst)
 *     wandert ebenfalls direkt zum Golem.</li>
 * </ol>
 * Bereits geschnitzte Kürbisse und Kürbislaternen werden nie als Ziel gewählt — nur frisch
 * gewachsene {@code Blocks.PUMPKIN}-Blöcke gelten als Ernte. Das verhindert insbesondere, dass
 * der Golem einen Kürbis anrührt, der gerade auf einer Truhe/einem Wollblock sitzt und dort einen
 * anderen Golem erschaffen soll (siehe {@code event.WorldEventHandler}) — solche Kürbisse werden
 * ohnehin sofort bei Platzierung verarbeitet und existieren praktisch nie als normale
 * {@code Blocks.PUMPKIN}-Blöcke in freier Wildbahn.
 *
 * Der volle Suchbereich (bis zu ~31000 Positionen bei Radius 100) wird nicht jeden Tick abgesucht:
 * findet ein Durchlauf nichts Erntereifes, wartet das Ziel {@link #RESCAN_INTERVAL} Ticks, bevor
 * erneut gesucht wird — sonst würde der Golem im Leerlauf mehrmals pro Sekunde den riesigen
 * Bereich absuchen.
 */
public class HarvestPumpkinGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;
    private static final int RESCAN_INTERVAL = 40;

    private final PumpkinGolem golem;
    private final double speed;
    private BlockPos target;
    private int ticksUntilRescan;

    public HarvestPumpkinGoal(PumpkinGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private static boolean isHarvestablePumpkin(Level level, BlockPos fieldPos) {
        return level.getBlockState(fieldPos.above()).is(Blocks.PUMPKIN);
    }

    @Override
    public boolean canUse() {
        if (ticksUntilRescan > 0) {
            ticksUntilRescan--;
            return false;
        }
        target = findPumpkin();
        if (target == null) {
            ticksUntilRescan = RESCAN_INTERVAL;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && isHarvestablePumpkin(golem.level(), target);
    }

    private BlockPos findPumpkin() {
        Level level = golem.level();
        return golem.fieldPositions().stream()
                .filter(pos -> isHarvestablePumpkin(level, pos))
                .min(Comparator.comparingDouble(pos -> golem.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5)))
                .orElse(null);
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, speed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);

        if (golem.distanceToSqr(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5) > REACH_DISTANCE * REACH_DISTANCE) {
            golem.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, speed);
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;

        BlockPos pumpkinPos = target.above();
        target = null;
        BlockState state = server.getBlockState(pumpkinPos);
        if (!state.is(Blocks.PUMPKIN)) return;

        server.setBlock(pumpkinPos, Blocks.CARVED_PUMPKIN.defaultBlockState(), 3);
        server.playSound(null, pumpkinPos, SoundEvents.PUMPKIN_CARVE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        golem.addCarriedSeeds(new ItemStack(Items.PUMPKIN_SEEDS, 4));

        BlockState carvedState = server.getBlockState(pumpkinPos);
        List<ItemStack> drops = Block.getDrops(carvedState, server, pumpkinPos, server.getBlockEntity(pumpkinPos));
        server.setBlock(pumpkinPos, Blocks.AIR.defaultBlockState(), 3);
        for (ItemStack drop : drops) {
            golem.addCarriedPumpkin(drop);
        }
    }
}
