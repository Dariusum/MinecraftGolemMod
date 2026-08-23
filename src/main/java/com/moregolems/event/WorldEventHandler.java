package com.moregolems.event;

import com.moregolems.MoreGolemsMod;
import com.moregolems.entity.BeetrootGolem;
import com.moregolems.entity.CarrotGolem;
import com.moregolems.entity.CropGolem;
import com.moregolems.entity.PotatoGolem;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.function.BiFunction;

@EventBusSubscriber(modid = MoreGolemsMod.MODID)
public final class WorldEventHandler {

    private WorldEventHandler() {}

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
        } else if (baseState.is(Blocks.CHEST)) {
            boolean spawned = trySpawnCropGolem(server, pumpkinPos, basePos, Items.CARROT, ModEntities.CARROT_GOLEM.get(), CarrotGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.POTATO, ModEntities.POTATO_GOLEM.get(), PotatoGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.BEETROOT_SEEDS, ModEntities.BEETROOT_GOLEM.get(), BeetrootGolem::new)
                    || trySpawnCropGolem(server, pumpkinPos, basePos, Items.WHEAT_SEEDS, ModEntities.WHEAT_GOLEM.get(), WheatGolem::new);
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
}
