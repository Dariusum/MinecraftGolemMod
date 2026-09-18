package com.moregolems.client;

import com.moregolems.MoreGolemsMod;
import com.moregolems.registry.ModEntities;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class MoreGolemsClient {
    private MoreGolemsClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(MoreGolemsClient::onRegisterRenderers);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WOOL_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("wool_golem")));
        event.registerEntityRenderer(ModEntities.CARROT_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("carrot_golem")));
        event.registerEntityRenderer(ModEntities.POTATO_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("potato_golem")));
        event.registerEntityRenderer(ModEntities.BEETROOT_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("beetroot_golem")));
        event.registerEntityRenderer(ModEntities.WHEAT_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("wheat_golem")));
        event.registerEntityRenderer(ModEntities.BAMBOO_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("bamboo_golem")));
        event.registerEntityRenderer(ModEntities.SUGAR_CANE_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("sugar_cane_golem")));
        event.registerEntityRenderer(ModEntities.PUMPKIN_GOLEM.get(),
                ctx -> new ColoredCopperGolemRenderer<>(ctx, texture("pumpkin_golem")));
        event.registerEntityRenderer(ModEntities.EARTH_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("earth_golem")));
        event.registerEntityRenderer(ModEntities.STONE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("stone_golem")));
        event.registerEntityRenderer(ModEntities.GRANITE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("granite_golem")));
        event.registerEntityRenderer(ModEntities.DIORITE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("diorite_golem")));
        event.registerEntityRenderer(ModEntities.ANDESITE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("andesite_golem")));
        event.registerEntityRenderer(ModEntities.DEEPSLATE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("deepslate_golem")));
        event.registerEntityRenderer(ModEntities.TUFF_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("tuff_golem")));
        event.registerEntityRenderer(ModEntities.CALCITE_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("calcite_golem")));
        event.registerEntityRenderer(ModEntities.SAND_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("sand_golem")));
        event.registerEntityRenderer(ModEntities.GRAVEL_GOLEM.get(),
                ctx -> new MiningGolemRenderer<>(ctx, texture("gravel_golem")));
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(MoreGolemsMod.MODID, "textures/entity/" + name + "/" + name + ".png");
    }
}
