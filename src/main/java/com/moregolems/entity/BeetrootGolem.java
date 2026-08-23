package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Sieht aus wie ein dunkelroter Kupfer-Golem. Pflanzgut ≠ Ertrag: gepflanzt wird mit
 * Rote-Bete-Samen, geerntet werden Rote Bete (Ertrag, landet nur in der Truhe, siehe
 * {@link CropGolem#addHarvestDrop}) plus ggf. weitere Samen.
 */
public class BeetrootGolem extends CropGolem {
    public BeetrootGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public Item seedItem() {
        return Items.BEETROOT_SEEDS;
    }

    @Override
    public Block cropBlock() {
        return Blocks.BEETROOTS;
    }
}
