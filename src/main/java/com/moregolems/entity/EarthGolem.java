package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Erdgolem: baut Erde und Grasblöcke ab. Ein Grasblock liefert dabei (wie beim Spieler-Abbau ohne
 * Verzauberung) normale Erde, nicht sich selbst. Siehe {@link MiningGolem} für das gemeinsame
 * Verhalten aller Golems dieser Art.
 */
public class EarthGolem extends MiningGolem {

    public EarthGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK);
    }

    @Override
    public BlockState resultBlockState(BlockState minedState) {
        return minedState.is(Blocks.GRASS_BLOCK) ? Blocks.DIRT.defaultBlockState() : super.resultBlockState(minedState);
    }
}
