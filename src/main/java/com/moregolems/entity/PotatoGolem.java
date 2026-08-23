package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Sieht aus wie ein hellbrauner Kupfer-Golem. Pflanzgut = Ertrag (Kartoffel pflanzt Kartoffel). */
public class PotatoGolem extends CropGolem {
    public PotatoGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public Item seedItem() {
        return Items.POTATO;
    }

    @Override
    public Block cropBlock() {
        return Blocks.POTATOES;
    }
}
