package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Entsteht durch einen Weizensamen (nicht Weizen selbst) in der Kiste. Sieht aus wie ein
 * goldener/weizenfarbener Kupfer-Golem. Pflanzgut ≠ Ertrag: gepflanzt wird mit Weizensamen,
 * geerntet wird Weizen (Ertrag, landet nur in der Truhe, siehe {@link CropGolem#addHarvestDrop})
 * plus ggf. weitere Samen.
 */
public class WheatGolem extends CropGolem {
    public WheatGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public Item seedItem() {
        return Items.WHEAT_SEEDS;
    }

    @Override
    public Block cropBlock() {
        return Blocks.WHEAT;
    }
}
