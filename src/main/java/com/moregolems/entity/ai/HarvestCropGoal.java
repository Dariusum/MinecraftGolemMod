package com.moregolems.entity.ai;

import com.moregolems.entity.CropGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Erntet ein ausgewachsenes {@link CropGolem#cropBlock()}-Feld im 9x9-Bereich (max. 4 Blöcke
 * Abstand) um die Kiste und bepflanzt die Stelle sofort neu (mit einer der gerade geernteten
 * Samen, siehe {@link CropGolem#addHarvestDrop}) — höchste Priorität, damit reife Felder nicht
 * überaltern, während der Golem noch pflanzt oder Nachschub holt.
 */
public class HarvestCropGoal extends Goal {

    private static final double REACH_DISTANCE = 2.0;

    private final CropGolem golem;
    private final double speed;
    private BlockPos target;

    public HarvestCropGoal(CropGolem golem, double speed) {
        this.golem = golem;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = findMatureCrop();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && isMatureCrop(golem.level(), target);
    }

    private BlockPos findMatureCrop() {
        Level level = golem.level();
        return golem.fieldPositions().stream()
                .map(BlockPos::above)
                .filter(pos -> isMatureCrop(level, pos))
                .min(Comparator.comparingDouble(pos -> golem.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)))
                .orElse(null);
    }

    private boolean isMatureCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(golem.cropBlock()) && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
    }

    @Override
    public void stop() {
        target = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        golem.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

        if (golem.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) > REACH_DISTANCE * REACH_DISTANCE) {
            golem.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
            return;
        }

        golem.getNavigation().stop();
        if (!(golem.level() instanceof ServerLevel server)) return;
        BlockState state = server.getBlockState(target);
        if (!state.is(golem.cropBlock())) {
            target = null;
            return;
        }

        List<ItemStack> drops = Block.getDrops(state, server, target, server.getBlockEntity(target));
        for (ItemStack drop : drops) {
            golem.addHarvestDrop(drop);
        }

        // Bei Karotte/Kartoffel ist Ertrag=Pflanzgut, daher garantiert mindestens eines zum
        // Neubepflanzen verfügbar. Bei Weizen/Roter Bete ist der Samen-Bonus-Drop dagegen nicht
        // garantiert — dann bleibt das Feld leer (AIR) und wird später über PlantSeedGoal erneut
        // bepflanzt, sobald wieder Pflanzgut verfügbar ist.
        if (golem.tryConsumeSeed()) {
            server.setBlock(target, golem.cropBlock().defaultBlockState(), 3);
        } else {
            server.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
        target = null;
    }
}
