package com.eightsidedsquare.angling.common.entity;

import com.eightsidedsquare.angling.common.entity.util.SunfishVariant;
import com.eightsidedsquare.angling.core.AnglingItems;
import com.eightsidedsquare.angling.core.AnglingSounds;
import com.eightsidedsquare.angling.core.AnglingUtil;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Objects;

public class SunfishEntity extends SchoolingFishEntity implements IAnimatable {

    AnimationFactory factory = new AnimationFactory(this);

    private static final TrackedData<SunfishVariant> VARIANT;

    public SunfishEntity(EntityType<? extends SchoolingFishEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(VARIANT, AnglingUtil.getRandomTagValue(world, SunfishVariant.Tag.NATURAL_SUNFISH, random));
    }

    @Override
    protected SoundEvent getFlopSound() {
        return AnglingSounds.ENTITY_SUNFISH_FLOP;
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(AnglingItems.SUNFISH_BUCKET);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return AnglingSounds.ENTITY_SUNFISH_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return AnglingSounds.ENTITY_SUNFISH_DEATH;
    }

    @Nullable
    public SunfishVariant getVariant() {
        return dataTracker.get(VARIANT);
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        super.copyDataToStack(stack);
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putString("BucketVariantTag", SunfishVariant.getId(getVariant()).toString());
    }

    @Override
    public void tick() {
        super.tick();
        if(hasCustomName() && Objects.requireNonNull(getCustomName()).getString().equalsIgnoreCase("diansu")) {
            setVariant(SunfishVariant.DIANSUS_DIANSUR);
        }
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (spawnReason == SpawnReason.BUCKET && entityNbt != null && entityNbt.contains("BucketVariantTag", NbtElement.STRING_TYPE)) {
            this.setVariant(SunfishVariant.fromId(entityNbt.getString("BucketVariantTag")));
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public void setVariant(SunfishVariant variant) {
        dataTracker.set(VARIANT, variant);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Variant", SunfishVariant.getId(dataTracker.get(VARIANT)).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(VARIANT, SunfishVariant.fromId(nbt.getString("Variant")));
    }

    static {
        VARIANT = DataTracker.registerData(SunfishEntity.class, SunfishVariant.TRACKED_DATA_HANDLER);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<SunfishEntity> controller = new AnimationController<>(this, "controller", 2, this::controller);
        animationData.addAnimationController(controller);
    }

    private PlayState controller(AnimationEvent<SunfishEntity> event) {
        if(!touchingWater) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sunfish.flop", true));
        }else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sunfish.idle", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
