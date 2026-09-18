package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiefenschiefergolem: baut Tiefenschiefer ab. Liefert dabei (wie beim Spieler-Abbau ohne
 * Verzauberung) Kopfsteintiefenschiefer, nicht Tiefenschiefer selbst. Siehe {@link MiningGolem}
 * für das gemeinsame Verhalten aller Golems dieser Art.
 */
public class DeepslateGolem extends MiningGolem {

    public DeepslateGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.DEEPSLATE);
    }

    @Override
    public BlockState resultBlockState(BlockState minedState) {
        return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    }
}
