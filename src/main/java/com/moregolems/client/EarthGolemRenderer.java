package com.moregolems.client;

import com.moregolems.MoreGolemsMod;
import com.moregolems.entity.EarthGolem;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Nutzt Vanillas eigenes {@link IronGolemModel} (dieselbe Geometrie/Animation, über
 * {@link ModelLayers#IRON_GOLEM} — bereits von Vanilla gebacken, keine eigene Layer-Registrierung
 * nötig) mit einer eigenen, erd-texturierten Textur. Anders als {@link ColoredCopperGolemRenderer}
 * nicht generisch über mehrere Golems/Texturen, da es nur einen Erdgolem gibt. Registriert weder
 * {@code IronGolemCrackinessLayer} noch {@code IronGolemFlowerLayer} (Eisengolem-spezifische
 * Kosmetik, hier nicht gewünscht), stattdessen {@link CarriedDirtBlockLayer} für den sichtbar
 * getragenen Block.
 */
public class EarthGolemRenderer extends MobRenderer<EarthGolem, EarthGolemRenderState, IronGolemModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MoreGolemsMod.MODID, "textures/entity/earth_golem/earth_golem.png");
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public EarthGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new CarriedDirtBlockLayer(this));
    }

    @Override
    public EarthGolemRenderState createRenderState() {
        return new EarthGolemRenderState();
    }

    @Override
    public void extractRenderState(EarthGolem entity, EarthGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        BlockState carried = entity.getCarriedBlock();
        if (carried != null) {
            this.blockModelResolver.update(state.carriedBlock, carried, BLOCK_DISPLAY_CONTEXT);
        } else {
            state.carriedBlock.clear();
        }
    }

    @Override
    public Identifier getTextureLocation(EarthGolemRenderState state) {
        return TEXTURE;
    }
}
