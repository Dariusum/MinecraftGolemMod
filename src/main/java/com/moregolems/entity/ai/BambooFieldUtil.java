package com.moregolems.entity.ai;

import com.moregolems.entity.BambooGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Gemeinsame Feld-Abfragen, die von mehreren {@link BambooGolem}-KI-Zielen gebraucht werden. */
final class BambooFieldUtil {

    /** Deckelt die Höhenzählung analog zu {@code BambooStalkBlock.MAX_HEIGHT}. */
    private static final int MAX_HEIGHT = 16;

    private BambooFieldUtil() {}

    private static boolean isBambooPlant(BlockState state) {
        return state.is(Blocks.BAMBOO_SAPLING) || state.is(Blocks.BAMBOO);
    }

    /** true, wenn an [fieldPos] Erde liegt und der Block darüber frei (Luft) ist. */
    static boolean isEmptyDirt(Level level, BlockPos fieldPos) {
        return level.getBlockState(fieldPos).is(Blocks.DIRT) && level.getBlockState(fieldPos.above()).isAir();
    }

    static boolean anyEmptyDirt(BambooGolem golem) {
        Level level = golem.level();
        for (BlockPos pos : golem.fieldPositions()) {
            if (isEmptyDirt(level, pos)) return true;
        }
        return false;
    }

    static int countEmptyDirt(BambooGolem golem) {
        Level level = golem.level();
        int count = 0;
        for (BlockPos pos : golem.fieldPositions()) {
            if (isEmptyDirt(level, pos)) count++;
        }
        return count;
    }

    /**
     * Höhe des Bambus-Stapels, der direkt über [fieldPos] beginnt (0, falls dort kein Bambus wächst).
     * Zählt Setzling/Stängel-Blöcke von unten nach oben durch.
     */
    static int columnHeight(Level level, BlockPos fieldPos) {
        int height = 0;
        BlockPos pos = fieldPos.above();
        while (height < MAX_HEIGHT && isBambooPlant(level.getBlockState(pos))) {
            height++;
            pos = pos.above();
        }
        return height;
    }
}
