package com.moregolems.entity.ai;

import com.moregolems.entity.CropGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Gemeinsame Feld-Abfragen, die von mehreren {@link CropGolem}-KI-Zielen gebraucht werden. */
final class CropFieldUtil {

    private CropFieldUtil() {}

    /** true, wenn an [fieldPos] Ackerland liegt und der Block darüber frei (Luft) ist. */
    static boolean isEmptyFarmland(Level level, BlockPos fieldPos) {
        return level.getBlockState(fieldPos).is(Blocks.FARMLAND) && level.getBlockState(fieldPos.above()).isAir();
    }

    static boolean anyEmptyFarmland(CropGolem golem) {
        Level level = golem.level();
        for (BlockPos pos : golem.fieldPositions()) {
            if (isEmptyFarmland(level, pos)) return true;
        }
        return false;
    }

    static int countEmptyFarmland(CropGolem golem) {
        Level level = golem.level();
        int count = 0;
        for (BlockPos pos : golem.fieldPositions()) {
            if (isEmptyFarmland(level, pos)) count++;
        }
        return count;
    }
}
