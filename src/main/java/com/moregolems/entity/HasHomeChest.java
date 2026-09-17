package com.moregolems.entity;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * Von jedem Golem dieses Mods implementiert - erlaubt {@code event.WorldEventHandler}, die an eine
 * Truhe gebundene Position generisch abzufragen, ohne für jeden Golem-Typ einzeln unterscheiden zu
 * müssen (z.B. um den Golem zu entfernen, sobald seine Truhe abgebaut wird).
 */
public interface HasHomeChest {
    @Nullable
    BlockPos getHomeChestPos();
}
