package com.moregolems.registry;

import com.moregolems.MoreGolemsMod;
import com.moregolems.entity.BeetrootGolem;
import com.moregolems.entity.CarrotGolem;
import com.moregolems.entity.PotatoGolem;
import com.moregolems.entity.WheatGolem;
import com.moregolems.entity.WoolGolem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final String MODID = MoreGolemsMod.MODID;

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final ResourceKey<EntityType<?>> WOOL_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "wool_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<WoolGolem>> WOOL_GOLEM =
            ENTITY_TYPES.register("wool_golem", () -> EntityType.Builder.of(WoolGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(WOOL_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> CARROT_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "carrot_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<CarrotGolem>> CARROT_GOLEM =
            ENTITY_TYPES.register("carrot_golem", () -> EntityType.Builder.of(CarrotGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(CARROT_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> POTATO_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "potato_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<PotatoGolem>> POTATO_GOLEM =
            ENTITY_TYPES.register("potato_golem", () -> EntityType.Builder.of(PotatoGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(POTATO_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> BEETROOT_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "beetroot_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<BeetrootGolem>> BEETROOT_GOLEM =
            ENTITY_TYPES.register("beetroot_golem", () -> EntityType.Builder.of(BeetrootGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(BEETROOT_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> WHEAT_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "wheat_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<WheatGolem>> WHEAT_GOLEM =
            ENTITY_TYPES.register("wheat_golem", () -> EntityType.Builder.of(WheatGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(WHEAT_GOLEM_KEY));

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
