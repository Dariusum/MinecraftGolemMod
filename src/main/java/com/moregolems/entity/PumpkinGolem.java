package com.moregolems.entity;

import com.moregolems.entity.ai.DepositPumpkinGoal;
import com.moregolems.entity.ai.HarvestPumpkinGoal;
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
 * Sieht aus wie ein tief-orange eingefärbter Kupfer-Golem, entsteht analog zu diesem (geschnitzter
 * Kürbis auf einer Truhe, die ausschließlich genau ein Stück Kürbiskerne enthält, siehe
 * {@code event.WorldEventHandler}). Durchsucht einen kreisförmigen 100-Block-Umkreis um seine
 * Truhe (siehe {@link #fieldPositions()}) nach ausgewachsenen Kürbissen: schnitzt jeden Fund
 * (wie ein Spieler mit einer Schere — der Block wird zu einem geschnitzten Kürbis, 4 Kürbiskerne
 * fallen ab) und baut ihn direkt im Anschluss ab, sodass sowohl die Kerne als auch der
 * geschnitzte Kürbis selbst in der Truhe landen (siehe {@link HarvestPumpkinGoal},
 * {@link DepositPumpkinGoal}). Rührt bereits geschnitzte Kürbisse und Kürbislaternen nicht an —
 * nur frisch gewachsene, ungeschnitzte Kürbisblöcke gelten als Ernte. Pflanzt selbst nichts neu
 * (die Kürbisranke daneben lässt von allein neue Kürbisse nachwachsen). Komplett friedlich.
 */
public class PumpkinGolem extends AbstractGolem implements HasHomeChest {

    /** Radius des kreisförmigen Suchbereichs um die Truhe, siehe {@link #fieldPositions()}. */
    private static final int FIELD_RADIUS = 100;

    /**
     * Etwas größer als {@link #FIELD_RADIUS}, damit {@link Mob#setHomeTo} (misst Luftlinie) auch
     * die Randfelder des kreisförmigen Bereichs bequem erreichbar lässt.
     */
    private static final int HOME_RADIUS = 105;

    @Nullable
    private BlockPos homeChestPos;

    /** Kürbiskerne aus dem Schnitzen, die der Golem gerade zu seiner Truhe trägt. */
    private ItemStack carriedSeeds = ItemStack.EMPTY;

    /** Geschnitzte Kürbisse aus dem anschließenden Abbau, die der Golem gerade trägt. */
    private ItemStack carriedPumpkins = ItemStack.EMPTY;

    public PumpkinGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HarvestPumpkinGoal(this, 1.0));
        this.goalSelector.addGoal(2, new DepositPumpkinGoal(this, 1.0));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
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
     * Die Bodenblock-Positionen im kreisförmigen 100-Block-Umkreis um die Truhe (Luftlinie in
     * X/Z, nicht Schachbrett-Distanz — "Umkreis"), auf Höhe des Blocks, auf dem die Truhe steht.
     * Ein Kürbis würde jeweils direkt über einer solchen Position stehen (analog
     * {@code HarvestCropGoal}, das ebenfalls den Block über der Feldposition prüft).
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

    public int getCarriedSeedCount() {
        return carriedSeeds.isEmpty() ? 0 : carriedSeeds.getCount();
    }

    public ItemStack getCarriedSeeds() {
        return carriedSeeds;
    }

    public void setCarriedSeeds(ItemStack stack) {
        this.carriedSeeds = stack;
    }

    /** Fügt beim Schnitzen abgeworfene Kürbiskerne dem getragenen Stapel hinzu. */
    public void addCarriedSeeds(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (carriedSeeds.isEmpty()) {
            carriedSeeds = stack.copy();
        } else {
            carriedSeeds.grow(stack.getCount());
        }
    }

    public boolean isCarryingPumpkin() {
        return !carriedPumpkins.isEmpty();
    }

    public ItemStack getCarriedPumpkins() {
        return carriedPumpkins;
    }

    public void setCarriedPumpkins(ItemStack stack) {
        this.carriedPumpkins = stack;
    }

    /** Fügt beim Abbauen des geschnitzten Kürbisses gewonnene Drops dem getragenen Stapel hinzu. */
    public void addCarriedPumpkin(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (carriedPumpkins.isEmpty()) {
            carriedPumpkins = stack.copy();
        } else if (ItemStack.isSameItemSameComponents(carriedPumpkins, stack)) {
            carriedPumpkins.grow(stack.getCount());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        output.store("CarriedSeeds", ItemStack.OPTIONAL_CODEC, carriedSeeds);
        output.store("CarriedPumpkins", ItemStack.OPTIONAL_CODEC, carriedPumpkins);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, HOME_RADIUS);
        }
        carriedSeeds = input.read("CarriedSeeds", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        carriedPumpkins = input.read("CarriedPumpkins", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
