package com.moregolems.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Sieht aus wie ein oranger Kupfer-Golem. Pflanzgut = Ertrag (Karotte pflanzt Karotte). */
public class CarrotGolem extends CropGolem {
    public CarrotGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public Item seedItem() {
        return Items.CARROT;
    }

    @Override
    public Block cropBlock() {
        return Blocks.CARROTS;
    }
}
