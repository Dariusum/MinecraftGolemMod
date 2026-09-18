package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dioritgolem: baut Diorit ab, liefert Diorit. Siehe {@link MiningGolem} für das gemeinsame
 * Verhalten aller Golems dieser Art.
 */
public class DioriteGolem extends MiningGolem {

    public DioriteGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.DIORITE);
    }
}
