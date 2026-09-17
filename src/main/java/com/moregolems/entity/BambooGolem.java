package com.moregolems.entity;

import com.moregolems.entity.ai.CollectLooseBambooGoal;
import com.moregolems.entity.ai.DepositBambooGoal;
import com.moregolems.entity.ai.HarvestBambooGoal;
import com.moregolems.entity.ai.PlantBambooGoal;
import com.moregolems.entity.ai.WithdrawBambooGoal;
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
 * Sieht aus wie ein grüner Kupfer-Golem, entsteht analog zu diesem (geschnitzter Kürbis auf einer
 * Truhe, die ausschließlich genau ein Stück Bambus enthält, siehe {@code event.WorldEventHandler}).
 * Bewirtschaftet ein 10x10-Feld um seine Truhe (siehe {@link #fieldPositions()}): erntet
 * hochgewachsenen Bambus, lässt dabei das unterste Stück stehen (wächst von dort weiter nach), und
 * bepflanzt leere Erdblöcke neu ({@link HarvestBambooGoal}, {@link PlantBambooGoal}). Holt bei
 * Bedarf Pflanzgut aus der Truhe ({@link WithdrawBambooGoal}), sammelt lose herumliegenden Bambus
 * ein ({@link CollectLooseBambooGoal}) und legt getragenen Bambus in der Truhe ab, sobald er ihn
 * nicht zum Bepflanzen braucht ({@link DepositBambooGoal}). Komplett friedlich.
 */
public class BambooGolem extends AbstractGolem implements HasHomeChest {

    /**
     * 10x10-Feldfläche um die Truhe, auf Höhe des Blocks, auf dem die Truhe steht (dieselbe Höhe
     * wie Erdblöcke daneben) — geradzahlige Kantenlänge, daher leicht asymmetrisch um die Truhe
     * verteilt (X/Z je -5..+4).
     */
    private static final int FIELD_MIN = -5;
    private static final int FIELD_MAX = 4;

    /**
     * Muss die 10x10-Feldfläche komplett abdecken — die Eckfelder liegen in Luftlinie
     * sqrt(5²+5²)≈7,07 Blöcke entfernt, {@link Mob#setHomeTo} misst Luftlinie. Etwas Rand für
     * bequemes Erreichen.
     */
    private static final int HOME_RADIUS = 9;

    @Nullable
    private BlockPos homeChestPos;

    /** Bambus, den der Golem gerade trägt (geerntet oder eingesammelt, nicht sichtbar getragen). */
    private ItemStack carriedBamboo = ItemStack.EMPTY;

    public BambooGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HarvestBambooGoal(this, 1.0));
        this.goalSelector.addGoal(2, new PlantBambooGoal(this, 1.0));
        this.goalSelector.addGoal(3, new WithdrawBambooGoal(this, 1.0));
        this.goalSelector.addGoal(4, new CollectLooseBambooGoal(this, 1.0));
        this.goalSelector.addGoal(5, new DepositBambooGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
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
        // Vanillas eingebaute Heimatbindung sorgt dafür, dass der Golem sein Feld nicht verlässt,
        // sobald ihm gerade die Arbeit ausgeht — siehe dieselbe Begründung in CropGolem.
        this.setHomeTo(this.homeChestPos, HOME_RADIUS);
    }

    /**
     * Die 100 Erdblock-Positionen im 10x10-Bereich um die Truhe, auf Höhe des Blocks, auf dem die
     * Truhe steht (dieselbe Höhe wie die Truhe selbst). Enthält auch das Feld direkt unter der
     * Truhe (unschädlich: dort kann wegen der Truhe ohnehin kein Bambus wachsen).
     */
    public List<BlockPos> fieldPositions() {
        BlockPos home = homeChestPos;
        if (home == null) return List.of();
        List<BlockPos> positions = new ArrayList<>((FIELD_MAX - FIELD_MIN + 1) * (FIELD_MAX - FIELD_MIN + 1));
        int fieldY = home.getY() - 1;
        for (int dx = FIELD_MIN; dx <= FIELD_MAX; dx++) {
            for (int dz = FIELD_MIN; dz <= FIELD_MAX; dz++) {
                positions.add(new BlockPos(home.getX() + dx, fieldY, home.getZ() + dz));
            }
        }
        return positions;
    }

    public int getCarriedBambooCount() {
        return carriedBamboo.isEmpty() ? 0 : carriedBamboo.getCount();
    }

    public ItemStack getCarriedBamboo() {
        return carriedBamboo;
    }

    public void setCarriedBamboo(ItemStack stack) {
        this.carriedBamboo = stack;
    }

    /** Fügt geernteten/eingesammelten Bambus dem getragenen Stapel hinzu. */
    public void addCarriedBamboo(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (carriedBamboo.isEmpty()) {
            carriedBamboo = stack.copy();
        } else {
            carriedBamboo.grow(stack.getCount());
        }
    }

    /** Verbraucht ein getragenes Stück Bambus (zum Bepflanzen); true, falls eines vorhanden war. */
    public boolean tryConsumeBamboo() {
        if (carriedBamboo.isEmpty()) return false;
        carriedBamboo.shrink(1);
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        output.store("CarriedBamboo", ItemStack.OPTIONAL_CODEC, carriedBamboo);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, HOME_RADIUS);
        }
        carriedBamboo = input.read("CarriedBamboo", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
