package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steingolem: baut Stein ab. Liefert dabei (wie beim Spieler-Abbau ohne Verzauberung)
 * Kopfsteinpflaster, nicht Stein selbst. Siehe {@link MiningGolem} für das gemeinsame Verhalten
 * aller Golems dieser Art.
 */
public class StoneGolem extends MiningGolem {

    public StoneGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.STONE);
    }

    @Override
    public BlockState resultBlockState(BlockState minedState) {
        return Blocks.COBBLESTONE.defaultBlockState();
    }
}
