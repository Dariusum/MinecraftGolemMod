package com.moregolems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Zeichnet den von einem {@code MiningGolem} gehaltenen Block sichtbar vor seinem Körper, analog zu
 * Vanillas {@code CarriedBlockLayer} beim Enderman — dessen Klasse ist aber fest an
 * {@code EndermanRenderState}/{@code EndermanModel} gebunden und daher hier nicht wiederverwendbar.
 *
 * Die genauen Versatz-/Skalierungswerte sind ein Startpunkt (an Endermans Werte angelehnt, aber für
 * die deutlich größere Eisengolem-Statur nach oben/vorne verschoben) und müssen im Spiel visuell
 * nachjustiert werden.
 */
public class CarriedMinedBlockLayer extends RenderLayer<MiningGolemRenderState, IronGolemModel> {

    public CarriedMinedBlockLayer(RenderLayerParent<MiningGolemRenderState, IronGolemModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                        MiningGolemRenderState state, float yRot, float xRot) {
        BlockModelRenderState carriedBlock = state.carriedBlock;
        if (carriedBlock.isEmpty()) return;

        poseStack.pushPose();
        // Hoeher (groesserer Torso) und weiter vorne (breitere Arme) als bei Endermans Werten
        // (0.6875 / -0.75) - noch nicht final abgestimmt, siehe Klassendoc.
        poseStack.translate(0.0F, 1.1F, -0.9F);
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.translate(0.25F, 0.1875F, 0.25F);
        poseStack.scale(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        carriedBlock.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
