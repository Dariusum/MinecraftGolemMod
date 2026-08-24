package com.moregolems;

import com.moregolems.entity.BambooGolem;
import com.moregolems.entity.CropGolem;
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
        });

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            com.moregolems.client.MoreGolemsClient.init(modBus);
        }

        LOG.info("More Golems initialised");
    }
}
