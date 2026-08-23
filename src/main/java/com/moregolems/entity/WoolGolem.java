package com.moregolems.entity;

import com.moregolems.entity.ai.CollectLooseWoolGoal;
import com.moregolems.entity.ai.DepositWoolGoal;
import com.moregolems.entity.ai.ShearNearbySheepGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
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

/**
 * Sieht aus wie ein weißer Kupfer-Golem, entsteht analog zu diesem (geschnitzter Kürbis auf
 * einem Block — hier weiße Wolle statt Kupfer, siehe {@code event.WorldEventHandler}) und
 * schert selbstständig Schafe im Umkreis seiner Truhe ({@link #shearRadius()}), sammelt die
 * Wolle ein und legt sie dort ab (siehe {@link ShearNearbySheepGoal}, {@link DepositWoolGoal}).
 * Komplett friedlich, wie der Kupfer-Golem — keine Angriffs-/Wachziele.
 */
public class WoolGolem extends AbstractGolem {

    private static final int SHEAR_RADIUS = 10;

    /** Position der an diesen Golem gebundenen weißen Truhe — gesetzt bei der Erschaffung, siehe {@link #setHomeChestPos}. */
    @Nullable
    private BlockPos homeChestPos;

    /** Zuletzt geschorene Wolle, die der Golem gerade zu seiner Truhe trägt (nicht sichtbar getragen). */
    private ItemStack carriedWool = ItemStack.EMPTY;

    public WoolGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DepositWoolGoal(this, 1.0));
        this.goalSelector.addGoal(2, new ShearNearbySheepGoal(this, 1.0));
        this.goalSelector.addGoal(3, new CollectLooseWoolGoal(this, 1.0));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** Registriert über {@code EntityAttributeCreationEvent} in {@link com.moregolems.MoreGolemsMod}. */
    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public int shearRadius() {
        return SHEAR_RADIUS;
    }

    @Nullable
    public BlockPos getHomeChestPos() {
        return homeChestPos;
    }

    public void setHomeChestPos(BlockPos pos) {
        this.homeChestPos = pos.immutable();
        // Vanillas eingebaute Heimatbindung (auch von IronGolem/Wölfen genutzt) sorgt dafür,
        // dass WaterAvoidingRandomStrollGoal (und generell alle Pfadfindungs-Ziele) den
        // Golem nicht aus seinem Arbeitsbereich herauswandern lassen — ohne das lief er, sobald
        // ihm die Arbeit ausging (z.B. warten auf nachwachsende Wolle), einfach unbegrenzt weit weg.
        this.setHomeTo(this.homeChestPos, SHEAR_RADIUS);
    }

    public boolean isCarryingWool() {
        return !carriedWool.isEmpty();
    }

    public ItemStack getCarriedWool() {
        return carriedWool;
    }

    public void setCarriedWool(ItemStack stack) {
        this.carriedWool = stack;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        output.store("CarriedWool", ItemStack.OPTIONAL_CODEC, carriedWool);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, SHEAR_RADIUS);
        }
        carriedWool = input.read("CarriedWool", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
