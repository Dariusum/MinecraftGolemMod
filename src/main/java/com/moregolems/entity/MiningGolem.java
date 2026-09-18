package com.moregolems.entity;

import com.moregolems.entity.ai.DepositMiningGoal;
import com.moregolems.entity.ai.HarvestMiningGoal;
import com.moregolems.entity.ai.StandByChestGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Gemeinsame Basis für alle Golems, die dauerhaft Gelände abtragen statt eine nachwachsende
 * Ressource zu ernten (Erd-, Stein-, Granit-, Diorit-, Andesit-, Tiefenschiefer-, Tuff-, Calcit-,
 * Sand- und Kiesgolem) — analog dazu, wie {@link CropGolem} die gemeinsame Basis der vier
 * Feld-Golems ist. Sieht aus wie ein mit seinem jeweiligen Material texturierter Eisengolem und
 * wird auch analog zu diesem gebaut: drei Blöcke des Materials im selben T-Muster wie die drei
 * Eisenblöcke eines echten Eisengolems (Arme/Torso-Reihe), mit einer Truhe statt eines vierten
 * Blocks an der Beine-Position, darauf ein geschnitzter Kürbis (siehe
 * {@code event.WorldEventHandler}). Beim Erschaffen verschwinden die drei Materialblöcke und der
 * Kürbis, die Truhe bleibt erhalten — der Golem steht darauf.
 *
 * Baut {@link #isTargetBlock(BlockState) passende Blöcke} in seinem Arbeitsbereich ab (siehe
 * {@link HarvestMiningGoal} für die genauen Regeln — nur auf/über Truhen-Höhe, nur direkt
 * exponierte Blöcke, keine Stufen über 1 Block, immer der nächste/nördlichste/nordöstlichste Block
 * zuerst), trägt jeweils genau einen Block sichtbar vor sich her (wie ein Enderman, siehe
 * {@code client.CarriedMinedBlockLayer}) — je nach {@link #resultBlockState(BlockState)} ggf. ein
 * anderer Block als der abgebaute (z.B. Stein → Kopfsteinpflaster, wie beim Spieler-Abbau ohne
 * Verzauberung) — und legt ihn in der Truhe ab ({@link DepositMiningGoal}). Bleibt bei voller Truhe
 * oder ohne aktuelles Ziel bei seiner Truhe stehen ({@link StandByChestGoal}) statt ziellos
 * umherzuwandern. Komplett friedlich.
 */
public abstract class MiningGolem extends AbstractGolem implements HasHomeChest {

    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRIED_BLOCK =
            SynchedEntityData.defineId(MiningGolem.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);

    /**
     * Deckt den quadratischen Arbeitsbereich (siehe {@link HarvestMiningGoal#WORK_RADIUS}) plus
     * reichlich Höhenspielraum ab — anders als bei den Feld-/Ernte-Golems dieses Mods ist hier
     * (anders als deren einzelne, feste Feld-Ebene) auch der senkrechte Abstand relevant, da
     * abgetragenes Gelände beliebig hoch über der Truhe liegen kann. {@link Mob#setHomeTo} misst
     * Luftlinie in 3D.
     */
    private static final int HOME_RADIUS = 48;

    @Nullable
    private BlockPos homeChestPos;

    protected MiningGolem(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    /** Welche Blöcke dieser Golem-Typ abbaut (z.B. Erdgolem: Erde ODER Grasblock). */
    public abstract boolean isTargetBlock(BlockState state);

    /**
     * Welcher Block nach dem Abbau von [minedState] sichtbar getragen und in der Truhe abgelegt
     * wird — standardmäßig der abgebaute Block selbst; golem-spezifisch überschrieben für Blöcke,
     * die beim Spieler-Abbau ohne Verzauberung einen anderen Block droppen (Stein →
     * Kopfsteinpflaster, Tiefenschiefer → Kopfsteintiefenschiefer).
     */
    public BlockState resultBlockState(BlockState minedState) {
        return minedState.getBlock().defaultBlockState();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARRIED_BLOCK, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HarvestMiningGoal(this, 1.0));
        this.goalSelector.addGoal(2, new DepositMiningGoal(this, 1.0));
        this.goalSelector.addGoal(3, new StandByChestGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    /** Registriert über {@code EntityAttributeCreationEvent} in {@link com.moregolems.MoreGolemsMod}. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                // Rein zur Mobilität ueber die Stufen, die die eigene Abbau-Regel (max. 1 Block
                // Hoehenunterschied) erlaubt - kein Kampf-Attribut, der Golem ist komplett friedlich.
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    @Nullable
    public BlockPos getHomeChestPos() {
        return homeChestPos;
    }

    public void setHomeChestPos(BlockPos pos) {
        this.homeChestPos = pos.immutable();
        // Vanillas eingebaute Heimatbindung sorgt dafuer, dass der Golem seinen Arbeitsbereich
        // nicht verlaesst - siehe dieselbe Begruendung in CropGolem.
        this.setHomeTo(this.homeChestPos, HOME_RADIUS);
    }

    public boolean isCarryingBlock() {
        return this.entityData.get(DATA_CARRIED_BLOCK).isPresent();
    }

    @Nullable
    public BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRIED_BLOCK).orElse(null);
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.entityData.set(DATA_CARRIED_BLOCK, Optional.ofNullable(state));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (homeChestPos != null) {
            output.store("HomeChest", BlockPos.CODEC, homeChestPos);
        }
        BlockState carried = getCarriedBlock();
        if (carried != null) {
            output.store("CarriedBlockState", BlockState.CODEC, carried);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        homeChestPos = input.read("HomeChest", BlockPos.CODEC).orElse(null);
        if (homeChestPos != null) {
            this.setHomeTo(homeChestPos, HOME_RADIUS);
        }
        setCarriedBlock(input.read("CarriedBlockState", BlockState.CODEC)
                .filter(state -> !state.isAir())
                .orElse(null));
    }
}
