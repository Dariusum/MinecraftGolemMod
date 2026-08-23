package com.moregolems.entity;

import com.moregolems.entity.ai.DepositCarriedGoal;
import com.moregolems.entity.ai.HarvestCropGoal;
import com.moregolems.entity.ai.PlantSeedGoal;
import com.moregolems.entity.ai.WithdrawSeedGoal;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Gemeinsame Basis für alle Feld-Golems (Karotte, Kartoffel, Rote Bete, Weizen): entsteht durch
 * einen geschnitzten Kürbis auf einer Kiste, die ausschließlich genau ein Stück {@link #seedItem()}
 * enthält — die Kiste bleibt unverändert, es wird nur der Golem daran gebunden. Bewirtschaftet
 * die 9x9-Feldfläche um seine Kiste (siehe {@link #fieldPositions()}, max. 4 Blöcke Abstand —
 * derselbe Bereich, den auch ein Wasserblock bewässern würde): erntet ausgewachsene
 * {@link #cropBlock()}-Felder und bepflanzt sofort neu ({@link HarvestCropGoal}), bepflanzt
 * leere Ackerfelder ({@link PlantSeedGoal}), holt bei Bedarf Pflanzgut-Nachschub aus der Kiste
 * ({@link WithdrawSeedGoal}) und legt Ernte-Ertrag sowie überzähliges Pflanzgut zurück, sobald
 * nichts mehr zu tun ist ({@link DepositCarriedGoal}). Bepflanzt nur bereits vorhandenes
 * Ackerland, pflügt selbst nichts um. Komplett friedlich.
 *
 * Pflanzgut und Ernte-Ertrag sind nicht immer dasselbe Item — bei Karotte/Kartoffel schon
 * (Karotte pflanzt Karotte), bei Weizen/Roter Bete nicht (Weizensamen pflanzt, Weizen ist nur
 * Ertrag). {@link #addHarvestDrop(ItemStack)} sortiert Ernte-Drops entsprechend in
 * {@link #getCarriedSeeds()} (Pflanzgut, wird sofort wiederverwendet) oder
 * {@link #getCarriedProduct()} (Ertrag, wird nur zur Kiste getragen).
 */
public abstract class CropGolem extends AbstractGolem {

    /**
     * Die Feldfläche deckt exakt den Bereich ab, den auch ein Wasserblock bewässern würde:
     * ein Quadrat mit max. 4 Blöcken Abstand in X/Z (Schachbrett-/Chebyshev-Distanz, wie
     * Vanillas {@code FarmlandBlock}-Bewässerungsprüfung), also 9x9 = 81 Felder.
     */
    private static final int FIELD_RADIUS = 4;

    /**
     * Muss die 9x9-Feldfläche komplett abdecken — die Eckfelder liegen in Luftlinie
     * sqrt(4²+4²)≈5,66 Blöcke entfernt, {@link Mob#setHomeTo} misst Luftlinie, nicht
     * Schachbrett-Distanz. Etwas Rand für bequemes Erreichen.
     */
    private static final int HOME_RADIUS = 7;

    @Nullable
    private BlockPos homeChestPos;

    /** Pflanzgut, das der Golem gerade trägt (nicht sichtbar getragen) — wird zum Bepflanzen verbraucht. */
    private ItemStack carriedSeeds = ItemStack.EMPTY;

    /** Ernte-Ertrag, der kein Pflanzgut ist (z.B. Weizen, Rote Bete) — wird nur zur Kiste getragen. */
    private ItemStack carriedProduct = ItemStack.EMPTY;

    protected CropGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    /** Das Pflanzgut: Auslöse-Item beim Erschaffen, wird zum Bepflanzen verbraucht und aus der Truhe nachgeholt. */
    public abstract Item seedItem();

    /** Der Feld-Block (z.B. {@code Blocks.CARROTS}), dessen Reife geprüft und der neu gepflanzt wird. */
    public abstract Block cropBlock();

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HarvestCropGoal(this, 1.0));
        this.goalSelector.addGoal(2, new PlantSeedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new WithdrawSeedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new DepositCarriedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
        // Vanillas eingebaute Heimatbindung (auch von IronGolem/Wölfen genutzt) sorgt dafür,
        // dass WaterAvoidingRandomStrollGoal (und generell alle Pfadfindungs-Ziele) den
        // Golem nicht aus seinem Feld herauswandern lassen — ohne das lief er, sobald ihm
        // die Arbeit ausging (z.B. Warten auf nachwachsendes Pflanzgut), einfach unbegrenzt weit weg.
        this.setHomeTo(this.homeChestPos, HOME_RADIUS);
    }

    /**
     * Die 81 Ackerland-Positionen im 9x9-Bereich um die Kiste, auf Höhe des Blocks, auf dem die
     * Kiste steht (dieselbe Höhe wie Ackerland daneben). Enthält auch das Feld direkt unter der
     * Kiste (unschädlich: dort kann wegen der Kiste ohnehin nichts wachsen).
     */
    public List<BlockPos> fieldPositions() {
        BlockPos home = homeChestPos;
        if (home == null) return List.of();
        List<BlockPos> positions = new ArrayList<>((2 * FIELD_RADIUS + 1) * (2 * FIELD_RADIUS + 1));
        int fieldY = home.getY() - 1;
        for (int dx = -FIELD_RADIUS; dx <= FIELD_RADIUS; dx++) {
            for (int dz = -FIELD_RADIUS; dz <= FIELD_RADIUS; dz++) {
                positions.add(new BlockPos(home.getX() + dx, fieldY, home.getZ() + dz));
            }
        }
        return positions;
    }

    public int getCarriedSeedCount() {
        return carriedSeeds.isEmpty() ? 0 : carriedSeeds.getCount();
    }

    public ItemStack getCarriedSeeds() {
        return carriedSeeds;
    }

    public void setCarriedSeeds(ItemStack stack) {
        this.carriedSeeds = stack;
    }

    public boolean isCarryingProduct() {
        return !carriedProduct.isEmpty();
    }

    public ItemStack getCarriedProduct() {
        return carriedProduct;
    }

    public void setCarriedProduct(ItemStack stack) {
        this.carriedProduct = stack;
    }

    /** Ordnet einen Ernte-Drop dem Pflanzgut (wenn er {@link #seedItem()} entspricht) oder dem Ertrag zu. */
    public void addHarvestDrop(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (stack.is(seedItem())) {
            if (carriedSeeds.isEmpty()) {
                carriedSeeds = stack.copy();
            } else {
                carriedSeeds.grow(stack.getCount());
            }
            return;
        }
        if (carriedProduct.isEmpty()) {
            carriedProduct = stack.copy();
        } else if (ItemStack.isSameItemSameComponents(carriedProduct, stack)) {
            carriedProduct.grow(stack.getCount());
        }
        // Andernfalls (zweite, andersartige Ertrags-Sorte) wird der Drop ignoriert — bei
        // Vanilla-Ernten kommt pro Feld-Typ ohnehin nur eine Ertrags-Sorte vor.
    }

    /** Verbraucht ein getragenes Stück Pflanzgut (zum Neubepflanzen); true, falls eines vorhanden war. */
    public boolean tryConsumeSeed() {
        if (carriedSeeds.isEmpty()) return false;
        carriedSeeds.shrink(1);
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        output.store("CarriedSeeds", ItemStack.OPTIONAL_CODEC, carriedSeeds);
        output.store("CarriedProduct", ItemStack.OPTIONAL_CODEC, carriedProduct);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, HOME_RADIUS);
        }
        carriedSeeds = input.read("CarriedSeeds", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        carriedProduct = input.read("CarriedProduct", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
