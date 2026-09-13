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
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(MoreGolemsMod.MODID, "textures/entity/" + name + "/" + name + ".png");
    }
}
