package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tuffgolem: baut Tuffstein ab, liefert Tuffstein. Siehe {@link MiningGolem} für das gemeinsame
 * Verhalten aller Golems dieser Art.
 */
public class TuffGolem extends MiningGolem {

    public TuffGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.TUFF);
    }
}
