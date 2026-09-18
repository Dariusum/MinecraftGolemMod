package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kiesgolem: baut Kies ab, liefert Kies. Vanilla lässt Kies beim Spieler-Abbau gelegentlich Feuerstein
 * statt Kies droppen (Zufallschance) — das bildet dieser Golem bewusst NICHT nach: er trägt/legt
 * immer sichtbar einen Block ab, kein loses Item wie Feuerstein. Siehe {@link MiningGolem} für das
 * gemeinsame Verhalten aller Golems dieser Art.
 */
public class GravelGolem extends MiningGolem {

    public GravelGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isTargetBlock(BlockState state) {
        return state.is(Blocks.GRAVEL);
    }
}
