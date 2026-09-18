package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Andesitgolem: baut Andesit ab, liefert Andesit. Siehe {@link MiningGolem} für das gemeinsame
 * Verhalten aller Golems dieser Art.
 */
public class AndesiteGolem extends MiningGolem {

    public AndesiteGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.ANDESITE);
    }
}
