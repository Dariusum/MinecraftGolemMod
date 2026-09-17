package com.moregolems.client;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;

/**
 * Erweitert Vanillas {@code IronGolemRenderState} (nicht nur {@code LivingEntityRenderState}), weil
 * {@code IronGolemModel#setupAnim} generisch an genau diesen Zustandstyp gebunden ist — ohne diese
 * Vererbung ließe sich das Modell nicht wiederverwenden. Die geerbten, Eisengolem-spezifischen
 * Felder (Angriffs-Animation, Blumen-Angebot, Rissigkeit) werden nie gesetzt und bleiben auf ihren
 * harmlosen Vanilla-Standardwerten (0 / NONE) — der komplett friedliche Erdgolem hat keine dieser
 * Mechaniken. Einziges eigenes Feld: der sichtbar getragene Block, analog
 * {@code EndermanRenderState.carriedBlock}.
 */
public class EarthGolemRenderState extends IronGolemRenderState {
    public final BlockModelRenderState carriedBlock = new BlockModelRenderState();
}
