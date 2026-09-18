package com.moregolems.client;

import com.moregolems.entity.MiningGolem;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gemeinsamer Renderer für alle {@link MiningGolem}-Typen (Erd-, Stein-, Granit-, Diorit-,
 * Andesit-, Tiefenschiefer-, Tuff-, Calcit-, Sand- und Kiesgolem): nutzt Vanillas eigenes
 * {@link IronGolemModel} (bereits gebacken über {@link ModelLayers#IRON_GOLEM}) mit einer pro
 * Golem übergebenen Textur — analog {@link ColoredCopperGolemRenderer} für die kupfer-golem-
 * förmigen Golems dieses Mods. Registriert weder {@code IronGolemCrackinessLayer} noch
 * {@code IronGolemFlowerLayer} (Eisengolem-spezifische Kosmetik, hier nicht gewünscht),
 * stattdessen {@link CarriedMinedBlockLayer} für den sichtbar getragenen Block.
 */
public class MiningGolemRenderer<T extends MiningGolem> extends MobRenderer<T, MiningGolemRenderState, IronGolemModel> {

    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final Identifier texture;
    private final BlockModelResolver blockModelResolver;

    public MiningGolemRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
        this.texture = texture;
        this.blockModelResolver = context.getBlockModelResolver();
        this.addLayer(new CarriedMinedBlockLayer(this));
    }

    @Override
    public MiningGolemRenderState createRenderState() {
        return new MiningGolemRenderState();
    }

    @Override
    public void extractRenderState(T entity, MiningGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        BlockState carried = entity.getCarriedBlock();
        if (carried != null) {
            this.blockModelResolver.update(state.carriedBlock, carried, BLOCK_DISPLAY_CONTEXT);
        } else {
            state.carriedBlock.clear();
        }
    }

    @Override
    public Identifier getTextureLocation(MiningGolemRenderState state) {
        return texture;
    }
}
