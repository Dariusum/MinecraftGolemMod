package com.moregolems.entity;

import com.moregolems.entity.ai.CollectLooseSugarCaneGoal;
import com.moregolems.entity.ai.DepositSugarCaneGoal;
import com.moregolems.entity.ai.HarvestSugarCaneGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sieht aus wie ein grün-türkis eingefärbter Kupfer-Golem, entsteht analog zu diesem (geschnitzter
 * Kürbis auf einer Truhe, die ausschließlich genau ein Stück Zuckerrohr enthält, siehe
 * {@code event.WorldEventHandler}). Durchsucht einen kreisförmigen 20-Block-Umkreis um seine Truhe
 * (siehe {@link #fieldPositions()}) nach hochgewachsenem Zuckerrohr und erntet alles oberhalb des
 * untersten Segments — das unterste Segment bleibt stehen und wächst von dort weiter nach, genau
 * wie beim Bambusgolem ({@link HarvestSugarCaneGoal}). Pflanzt selbst nichts neu (Zuckerrohr
 * wächst von allein an Gewässern nach) — sammelt nur die eigene Ernte ein
 * ({@link CollectLooseSugarCaneGoal}) und legt sie in der Truhe ab ({@link DepositSugarCaneGoal}).
 * Komplett friedlich.
 */
public class SugarCaneGolem extends AbstractGolem implements HasHomeChest {

    /** Radius des kreisförmigen Suchbereichs um die Truhe, siehe {@link #fieldPositions()}. */
    private static final int FIELD_RADIUS = 20;

    /**
     * Etwas größer als {@link #FIELD_RADIUS}, damit {@link Mob#setHomeTo} (misst Luftlinie) auch
     * die Randfelder des kreisförmigen Bereichs bequem erreichbar lässt.
     */
    private static final int HOME_RADIUS = 22;

    @Nullable
    private BlockPos homeChestPos;

    /** Zuckerrohr, das der Golem gerade zu seiner Truhe trägt (nicht sichtbar getragen). */
    private ItemStack carriedSugarCane = ItemStack.EMPTY;

    public SugarCaneGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HarvestSugarCaneGoal(this, 1.0));
        this.goalSelector.addGoal(2, new CollectLooseSugarCaneGoal(this, 1.0));
        this.goalSelector.addGoal(3, new DepositSugarCaneGoal(this, 1.0));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** Registriert über {@code EntityAttributeCreationEvent} in {@link com.moregolems.MoreGolemsMod}. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Nullable
    public BlockPos getHomeChestPos() {
        return homeChestPos;
    }

    public void setHomeChestPos(BlockPos pos) {
        this.homeChestPos = pos.immutable();
        // Vanillas eingebaute Heimatbindung sorgt dafür, dass der Golem seinen Suchbereich nicht
        // verlässt, sobald ihm gerade die Arbeit ausgeht — siehe dieselbe Begründung in CropGolem.
        this.setHomeTo(this.homeChestPos, HOME_RADIUS);
    }

    /**
     * Die Erdblock-Positionen im kreisförmigen 20-Block-Umkreis um die Truhe (Luftlinie in X/Z,
     * nicht Schachbrett-Distanz — "Umkreis"), auf Höhe des Blocks, auf dem die Truhe steht
     * (dieselbe Höhe wie der Boden daneben). Zuckerrohr wächst jeweils direkt über einer solchen
     * Position. Enthält auch das Feld direkt unter der Truhe (unschädlich).
     */
    public List<BlockPos> fieldPositions() {
        BlockPos home = homeChestPos;
        if (home == null) return List.of();
        List<BlockPos> positions = new ArrayList<>();
        int fieldY = home.getY() - 1;
        int radiusSq = FIELD_RADIUS * FIELD_RADIUS;
        for (int dx = -FIELD_RADIUS; dx <= FIELD_RADIUS; dx++) {
            for (int dz = -FIELD_RADIUS; dz <= FIELD_RADIUS; dz++) {
                if (dx * dx + dz * dz > radiusSq) continue;
                positions.add(new BlockPos(home.getX() + dx, fieldY, home.getZ() + dz));
            }
        }
        return positions;
    }

    public int getCarriedSugarCaneCount() {
        return carriedSugarCane.isEmpty() ? 0 : carriedSugarCane.getCount();
    }

    public ItemStack getCarriedSugarCane() {
        return carriedSugarCane;
    }

    public void setCarriedSugarCane(ItemStack stack) {
        this.carriedSugarCane = stack;
    }

    /** Fügt geerntetes/eingesammeltes Zuckerrohr dem getragenen Stapel hinzu. */
    public void addCarriedSugarCane(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (carriedSugarCane.isEmpty()) {
            carriedSugarCane = stack.copy();
        } else {
            carriedSugarCane.grow(stack.getCount());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        output.store("CarriedSugarCane", ItemStack.OPTIONAL_CODEC, carriedSugarCane);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, HOME_RADIUS);
        }
        carriedSugarCane = input.read("CarriedSugarCane", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
