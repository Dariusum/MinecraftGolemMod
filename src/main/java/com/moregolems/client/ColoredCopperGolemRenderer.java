package com.moregolems.client;

import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.CopperGolemState;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Gemeinsamer Renderer für alle Golems dieses Mods: nutzt Vanillas eigenes
 * {@link CopperGolemModel} (dieselbe Geometrie/Animation, über {@link ModelLayers#COPPER_GOLEM}
 * — bereits von Vanilla gebacken, keine eigene Layer-Registrierung nötig) mit einer pro Golem
 * übergebenen, eingefärbten Textur. Oxidations-/Sitz-/Stern-Zustände des Kupfer-Golems werden
 * bewusst konstant auf "frisch/idle" gehalten — unsere Golems oxidieren nicht.
 */
public class ColoredCopperGolemRenderer<T extends Mob> extends MobRenderer<T, CopperGolemRenderState, CopperGolemModel> {

    private final Identifier texture;

    public ColoredCopperGolemRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new CopperGolemModel(context.bakeLayer(ModelLayers.COPPER_GOLEM)), 0.4F);
        this.texture = texture;
    }

    @Override
    public CopperGolemRenderState createRenderState() {
        return new CopperGolemRenderState();
    }

    @Override
    public void extractRenderState(T entity, CopperGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.weathering = WeatheringCopper.WeatherState.UNAFFECTED;
        state.copperGolemState = CopperGolemState.IDLE;
    }

    @Override
    public Identifier getTextureLocation(CopperGolemRenderState state) {
        return texture;
    }
}
