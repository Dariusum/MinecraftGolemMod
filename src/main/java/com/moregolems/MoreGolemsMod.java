package com.moregolems;

import com.moregolems.entity.BambooGolem;
import com.moregolems.entity.CropGolem;
import com.moregolems.entity.MiningGolem;
import com.moregolems.entity.PumpkinGolem;
import com.moregolems.entity.SugarCaneGolem;
import com.moregolems.entity.WoolGolem;
import com.moregolems.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MoreGolemsMod.MODID)
public class MoreGolemsMod {
    public static final String MODID = "moregolems";
    public static final Logger LOG = LoggerFactory.getLogger(MODID);

    public MoreGolemsMod(IEventBus modBus, ModContainer container) {
        ModEntities.register(modBus);

        modBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(ModEntities.WOOL_GOLEM.get(), WoolGolem.createAttributes().build());
            event.put(ModEntities.CARROT_GOLEM.get(), CropGolem.createAttributes().build());
            event.put(ModEntities.POTATO_GOLEM.get(), CropGolem.createAttributes().build());
            event.put(ModEntities.BEETROOT_GOLEM.get(), CropGolem.createAttributes().build());
            event.put(ModEntities.WHEAT_GOLEM.get(), CropGolem.createAttributes().build());
            event.put(ModEntities.BAMBOO_GOLEM.get(), BambooGolem.createAttributes().build());
            event.put(ModEntities.SUGAR_CANE_GOLEM.get(), SugarCaneGolem.createAttributes().build());
            event.put(ModEntities.PUMPKIN_GOLEM.get(), PumpkinGolem.createAttributes().build());
            // Alle MiningGolem-Typen teilen sich dieselben Attribute (siehe MiningGolem.createAttributes).
            event.put(ModEntities.EARTH_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.STONE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.GRANITE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.DIORITE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.ANDESITE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.DEEPSLATE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.TUFF_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.CALCITE_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.SAND_GOLEM.get(), MiningGolem.createAttributes().build());
            event.put(ModEntities.GRAVEL_GOLEM.get(), MiningGolem.createAttributes().build());
        });

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            com.moregolems.client.MoreGolemsClient.init(modBus);
        }

        LOG.info("More Golems initialised");
    }
}
