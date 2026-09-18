package com.moregolems.registry;

import com.moregolems.MoreGolemsMod;
import com.moregolems.entity.AndesiteGolem;
import com.moregolems.entity.BambooGolem;
import com.moregolems.entity.BeetrootGolem;
import com.moregolems.entity.CalciteGolem;
import com.moregolems.entity.CarrotGolem;
import com.moregolems.entity.DeepslateGolem;
import com.moregolems.entity.DioriteGolem;
import com.moregolems.entity.EarthGolem;
import com.moregolems.entity.GraniteGolem;
import com.moregolems.entity.GravelGolem;
import com.moregolems.entity.PotatoGolem;
import com.moregolems.entity.PumpkinGolem;
import com.moregolems.entity.SandGolem;
import com.moregolems.entity.StoneGolem;
import com.moregolems.entity.SugarCaneGolem;
import com.moregolems.entity.TuffGolem;
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

    public static final ResourceKey<EntityType<?>> BAMBOO_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "bamboo_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<BambooGolem>> BAMBOO_GOLEM =
            ENTITY_TYPES.register("bamboo_golem", () -> EntityType.Builder.of(BambooGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(BAMBOO_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> SUGAR_CANE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "sugar_cane_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<SugarCaneGolem>> SUGAR_CANE_GOLEM =
            ENTITY_TYPES.register("sugar_cane_golem", () -> EntityType.Builder.of(SugarCaneGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(SUGAR_CANE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> PUMPKIN_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "pumpkin_golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<PumpkinGolem>> PUMPKIN_GOLEM =
            ENTITY_TYPES.register("pumpkin_golem", () -> EntityType.Builder.of(PumpkinGolem::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build(PUMPKIN_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> EARTH_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "earth_golem"));

    // Groesse exakt wie Vanillas Eisengolem (EntityType.Builder.of(IronGolem::new, ...).sized(1.4F, 2.7F))
    // - der Erdgolem soll optisch/maszlich wie ein Eisengolem wirken, nicht wie die kleineren
    // Kupfer-Golem-foermigen Golems dieses Mods.
    public static final DeferredHolder<EntityType<?>, EntityType<EarthGolem>> EARTH_GOLEM =
            ENTITY_TYPES.register("earth_golem", () -> EntityType.Builder.of(EarthGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(EARTH_GOLEM_KEY));

    // Die folgenden 9 Golems sind wie EARTH_GOLEM aufgebaut (Eisengolem-Groesse/-Statur, siehe
    // dortiger Kommentar) - jeweils nur mit anderem Material.
    public static final ResourceKey<EntityType<?>> STONE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "stone_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<StoneGolem>> STONE_GOLEM =
            ENTITY_TYPES.register("stone_golem", () -> EntityType.Builder.of(StoneGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(STONE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> GRANITE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "granite_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<GraniteGolem>> GRANITE_GOLEM =
            ENTITY_TYPES.register("granite_golem", () -> EntityType.Builder.of(GraniteGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(GRANITE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> DIORITE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "diorite_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<DioriteGolem>> DIORITE_GOLEM =
            ENTITY_TYPES.register("diorite_golem", () -> EntityType.Builder.of(DioriteGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(DIORITE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> ANDESITE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "andesite_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<AndesiteGolem>> ANDESITE_GOLEM =
            ENTITY_TYPES.register("andesite_golem", () -> EntityType.Builder.of(AndesiteGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(ANDESITE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> DEEPSLATE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "deepslate_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<DeepslateGolem>> DEEPSLATE_GOLEM =
            ENTITY_TYPES.register("deepslate_golem", () -> EntityType.Builder.of(DeepslateGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(DEEPSLATE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> TUFF_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "tuff_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<TuffGolem>> TUFF_GOLEM =
            ENTITY_TYPES.register("tuff_golem", () -> EntityType.Builder.of(TuffGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(TUFF_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> CALCITE_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "calcite_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<CalciteGolem>> CALCITE_GOLEM =
            ENTITY_TYPES.register("calcite_golem", () -> EntityType.Builder.of(CalciteGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(CALCITE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> SAND_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "sand_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<SandGolem>> SAND_GOLEM =
            ENTITY_TYPES.register("sand_golem", () -> EntityType.Builder.of(SandGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(SAND_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> GRAVEL_GOLEM_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "gravel_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<GravelGolem>> GRAVEL_GOLEM =
            ENTITY_TYPES.register("gravel_golem", () -> EntityType.Builder.of(GravelGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build(GRAVEL_GOLEM_KEY));

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
