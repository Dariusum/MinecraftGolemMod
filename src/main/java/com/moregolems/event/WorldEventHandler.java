package com.moregolems.event;

import com.moregolems.MoreGolemsMod;
import com.moregolems.entity.BambooGolem;
import com.moregolems.entity.BeetrootGolem;
import com.moregolems.entity.CarrotGolem;
import com.moregolems.entity.CropGolem;
import com.moregolems.entity.EarthGolem;
import com.moregolems.entity.PotatoGolem;
import com.moregolems.entity.PumpkinGolem;
import com.moregolems.entity.SugarCaneGolem;
import com.moregolems.entity.WheatGolem;
import com.moregolems.entity.WoolGolem;
import com.moregolems.registry.ModEntities;
import com.moregolems.util.ContainerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.function.BiFunction;

@EventBusSubscriber(modid = MoreGolemsMod.MODID)
public final class WorldEventHandler {

    private WorldEventHandler() {}

    /**
     * Erdgolem-Struktur: dasselbe T-Muster wie ein echter Eisengolem (siehe
     * {@code CarvedPumpkinBlock.getOrCreateIronGolemFull} in Vanilla), aber mit Erdblöcken statt
     * Eisenblöcken und einer Truhe statt eines vierten Erdblocks an der Beine-Position. Genau wie
     * beim echten Eisengolem prüft {@link BlockPattern#find} automatisch alle 4 Himmelsrichtungen,
     * die Struktur kann also in beliebiger Ausrichtung gebaut werden.
     */
    private static final BlockPattern EARTH_GOLEM_PATTERN = BlockPatternBuilder.start()
            .aisle("~^~",
                   "###",
                   "~C~")
            .where('^', BlockInWorld.hasState(state -> state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN)))
            .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.DIRT)))
            .where('C', BlockInWorld.hasState(state -> state.is(Blocks.CHEST)))
            .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
            .build();

    /**
     * Erschafft Golems analog zum Kupfer-Golem: ein geschnitzter Kürbis auf dem passenden Block
     * lässt einen Golem entstehen, gebunden an eine Truhe.
     * - Wollgolem: Kürbis auf einem weißen Wollblock — der Wollblock wird zu einer (ganz
     *   normalen) Truhe. Bewusst eine Vanilla-Truhe statt eines eigenen Blocks — ein eigener
     *   {@code ChestBlock}-Subtyp brauchte einen eigenen
     *   {@link net.minecraft.world.level.block.entity.BlockEntityRenderer}, den es (noch) nicht
     *   gibt, wodurch die Truhe unsichtbar blieb.
     * - Feld-Golems (Karotte/Kartoffel/Rote Bete/Weizen): Kürbis auf einer bereits vorhandenen
     *   Truhe, die ausschließlich genau ein Stück Pflanzgut enthält — die Truhe bleibt
     *   unverändert, nur der Golem wird daran gebunden. Bei Weizen und Roter Bete ist das
     *   Pflanzgut nicht der Ertrag selbst, sondern der jeweilige Samen (Weizensamen /
     *   Rote-Bete-Samen), siehe {@link CropGolem#seedItem()}.
     * - Bambusgolem: wie die Feld-Golems (Truhe mit genau einem Stück Bambus), aber kein
     *   {@link CropGolem} — siehe {@link #trySpawnBambooGolem}.
     * - Zuckerrohrgolem: wie der Bambusgolem, Truhe mit genau einem Stück Zuckerrohr — siehe
     *   {@link #trySpawnSugarCaneGolem}.
     * - Kürbisgolem: Truhe mit genau einem Stück Kürbiskerne (analog zu Weizen-/Rote-Bete-Golem:
     *   Auslöse-Item ist der Samen, nicht der Ertrag) — siehe {@link #trySpawnPumpkinGolem}.
     * - Erdgolem: anders als alle übrigen Golems kein Kürbis-auf-Einzelblock-Rezept, sondern eine
     *   ganze Struktur wie beim echten Eisengolem (drei Erdblöcke im Eisengolem-T-Muster, Truhe
     *   statt vierten Erdblocks an der Beine-Position, Kürbis obendrauf) — siehe
     *   {@link #EARTH_GOLEM_PATTERN}, {@link #trySpawnEarthGolem}.
     *
     * Reagiert auf {@code NeighborNotifyEvent} statt (wie zunächst versucht)
     * {@code BlockEvent.EntityPlaceEvent} — Letzteres feuert nur bei Spieler-/Mob-Platzierung,
     * nicht bei {@code /setblock} oder einem Werfer. NeighborNotifyEvent liefert die Position
     * des Blocks, dessen Nachbar sich geändert hat (hier: der Kürbis selbst, nach seiner
     * Platzierung) — Filterung auf Kürbis zuerst (seltener als Wolle/Truhen) statt auf den
     * Block darunter, dann Prüfung des Blocks darunter.
     */
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;

        BlockState state = event.getState();
        if (!state.is(Blocks.CARVED_PUMPKIN) && !state.is(Blocks.JACK_O_LANTERN)) return;

        BlockPos pumpkinPos = event.getPos();
        BlockPos basePos = pumpkinPos.below();
        BlockState baseState = server.getBlockState(basePos);

        if (baseState.is(Blocks.WOOL.white())) {
            spawnWoolGolem(server, pumpkinPos, basePos);
        } else if (baseState.is(Blocks.DIRT)) {
            trySpawnEarthGolem(server, pumpkinPos);
        } else if (baseState.is(Blocks.CHEST)) {
            boolean spawned = trySpawnCropGolem(server, pumpkinPos, basePos, Items.CARROT, ModEntities.CARROT_GOLEM.get(), CarrotGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.POTATO, ModEntities.POTATO_GOLEM.get(), PotatoGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.BEETROOT_SEEDS, ModEntities.BEETROOT_GOLEM.get(), BeetrootGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.WHEAT_SEEDS, ModEntities.WHEAT_GOLEM.get(), WheatGolem::new)
                    || trySpawnBambooGolem(server, pumpkinPos, basePos)
                    || trySpawnSugarCaneGolem(server, pumpkinPos, basePos)
                    || trySpawnPumpkinGolem(server, pumpkinPos, basePos);
            if (spawned) return; // nur zur Klarheit, dass die Kurzschlussauswertung absichtlich ist
        }
    }

    private static void spawnWoolGolem(ServerLevel server, BlockPos pumpkinPos, BlockPos woolPos) {
        server.setBlockAndUpdate(pumpkinPos, Blocks.AIR.defaultBlockState());
        server.setBlockAndUpdate(woolPos, Blocks.CHEST.defaultBlockState());

        WoolGolem golem = new WoolGolem(ModEntities.WOOL_GOLEM.get(), server);
        golem.setPos(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5);
        golem.setHomeChestPos(woolPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("Wollgolem erschaffen bei {} (Truhe bei {})", pumpkinPos, woolPos);
    }

    /**
     * Erdgolem: eigener Spawn-Pfad über {@link #EARTH_GOLEM_PATTERN} statt der einfachen
     * Ein-Block-Prüfung der übrigen Golems, da die Struktur (wie beim echten Eisengolem) mehrere
     * Blöcke in beliebiger Ausrichtung umfasst.
     *
     * @return true, falls die volle T-Struktur gefunden und der Golem erschaffen wurde.
     */
    private static boolean trySpawnEarthGolem(ServerLevel server, BlockPos pumpkinPos) {
        BlockPattern.BlockPatternMatch match = EARTH_GOLEM_PATTERN.find(server, pumpkinPos);
        if (match == null) return false;

        BlockPos chestPos = match.getBlock(1, 2, 0).getPos();

        // Alle Musterbloecke ausser der Truhe raeumen (anders als Vanillas
        // CarvedPumpkinBlock.clearPatternBlocks, das ausnahmslos alles raeumt) - die Truhe bleibt
        // als Heimat-Container des Golems erhalten, er steht am Ende auf ihr.
        for (int x = 0; x < match.getWidth(); x++) {
            for (int y = 0; y < match.getHeight(); y++) {
                BlockInWorld block = match.getBlock(x, y, 0);
                if (block.getPos().equals(chestPos)) continue;
                server.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 3);
                server.levelEvent(2001, block.getPos(), Block.getId(block.getState()));
            }
        }

        EarthGolem golem = new EarthGolem(ModEntities.EARTH_GOLEM.get(), server);
        BlockPos spawnPos = chestPos.above();
        golem.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        golem.setHomeChestPos(chestPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("Erdgolem erschaffen bei {} (Truhe bei {})", spawnPos, chestPos);
        return true;
    }

    /** @return true, falls die Truhe genau [seedItem] enthielt und der Golem erschaffen wurde. */
    private static <T extends CropGolem> boolean trySpawnCropGolem(
            ServerLevel server, BlockPos pumpkinPos, BlockPos chestPos, Item seedItem,
            EntityType<T> entityType, BiFunction<EntityType<? extends AbstractGolem>, ServerLevel, T> factory
    ) {
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return false;
        if (!ContainerUtil.containsOnly(container, seedItem, 1)) return false;

        server.setBlockAndUpdate(pumpkinPos, Blocks.AIR.defaultBlockState());

        T golem = factory.apply(entityType, server);
        golem.setPos(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5);
        golem.setHomeChestPos(chestPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("{} erschaffen bei {} (Truhe bei {})", entityType, pumpkinPos, chestPos);
        return true;
    }

    /**
     * Bambusgolem: eigener Spawn-Pfad statt über {@link #trySpawnCropGolem}, da {@link BambooGolem}
     * kein {@link CropGolem} ist (Ernte-/Pflanz-Mechanik unterscheidet sich zu stark — kein
     * Ackerland, kein Nachbepflanz-Zyklus, sondern ein 10x10-Erdblock-Feld mit Bambus-Stapeln).
     *
     * @return true, falls die Truhe genau ein Stück Bambus enthielt und der Golem erschaffen wurde.
     */
    private static boolean trySpawnBambooGolem(ServerLevel server, BlockPos pumpkinPos, BlockPos chestPos) {
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return false;
        if (!ContainerUtil.containsOnly(container, Items.BAMBOO, 1)) return false;

        server.setBlockAndUpdate(pumpkinPos, Blocks.AIR.defaultBlockState());

        BambooGolem golem = new BambooGolem(ModEntities.BAMBOO_GOLEM.get(), server);
        golem.setPos(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5);
        golem.setHomeChestPos(chestPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("Bambusgolem erschaffen bei {} (Truhe bei {})", pumpkinPos, chestPos);
        return true;
    }

    /**
     * Zuckerrohrgolem: eigener Spawn-Pfad wie {@link #trySpawnBambooGolem} (kein {@link CropGolem},
     * kein Nachbepflanz-Zyklus, sondern ein kreisförmiger 20-Block-Suchbereich).
     *
     * @return true, falls die Truhe genau ein Stück Zuckerrohr enthielt und der Golem erschaffen wurde.
     */
    private static boolean trySpawnSugarCaneGolem(ServerLevel server, BlockPos pumpkinPos, BlockPos chestPos) {
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return false;
        if (!ContainerUtil.containsOnly(container, Items.SUGAR_CANE, 1)) return false;

        server.setBlockAndUpdate(pumpkinPos, Blocks.AIR.defaultBlockState());

        SugarCaneGolem golem = new SugarCaneGolem(ModEntities.SUGAR_CANE_GOLEM.get(), server);
        golem.setPos(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5);
        golem.setHomeChestPos(chestPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("Zuckerrohrgolem erschaffen bei {} (Truhe bei {})", pumpkinPos, chestPos);
        return true;
    }

    /**
     * Kürbisgolem: eigener Spawn-Pfad wie {@link #trySpawnBambooGolem} (kein {@link CropGolem},
     * kein Nachbepflanz-Zyklus, sondern ein kreisförmiger 100-Block-Suchbereich). Auslöse-Item ist
     * Kürbiskerne, nicht Kürbis selbst — analog zu Weizen-/Rote-Bete-Golem, deren Auslöse-Item
     * ebenfalls der Samen statt des Ertrags ist.
     *
     * @return true, falls die Truhe genau ein Stück Kürbiskerne enthielt und der Golem erschaffen wurde.
     */
    private static boolean trySpawnPumpkinGolem(ServerLevel server, BlockPos pumpkinPos, BlockPos chestPos) {
        if (!(server.getBlockEntity(chestPos) instanceof Container container)) return false;
        if (!ContainerUtil.containsOnly(container, Items.PUMPKIN_SEEDS, 1)) return false;

        server.setBlockAndUpdate(pumpkinPos, Blocks.AIR.defaultBlockState());

        PumpkinGolem golem = new PumpkinGolem(ModEntities.PUMPKIN_GOLEM.get(), server);
        golem.setPos(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5);
        golem.setHomeChestPos(chestPos);
        server.addFreshEntity(golem);

        MoreGolemsMod.LOG.info("Kürbisgolem erschaffen bei {} (Truhe bei {})", pumpkinPos, chestPos);
        return true;
    }
}
