/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 12:04 AM (EST)]
 */
package vazkii.quark.content.mobs.entity;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.content.mobs.ai.FindPlaceToSleepGoal;
import vazkii.quark.content.mobs.ai.SleepGoal;
import vazkii.quark.content.mobs.module.FoxhoundModule;
import vazkii.quark.content.tweaks.ai.WantLoveGoal;

public class Foxhound extends Wolf implements Enemy {

	public static final ResourceLocation FOXHOUND_LOOT_TABLE = new ResourceLocation("quark", "entities/foxhound");

	private static final EntityDataAccessor<Boolean> TEMPTATION = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_BLUE = SynchedEntityData.defineId(Foxhound.class, EntityDataSerializers.BOOLEAN);

	private int timeUntilPotatoEmerges = 0;

	public Foxhound(EntityType<? extends Foxhound> type, Level worldIn) {
		super(type, worldIn);
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.LAVA, 1.0F);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 1.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 1.0F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		setCollarColor(DyeColor.ORANGE);
		entityData.define(TEMPTATION, false);
		entityData.define(SLEEPING, false);
		entityData.define(IS_BLUE, false);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 4;
	}

	@Override
	public boolean isPersistenceRequired() {
		return super.isPersistenceRequired();
	}

	@Override
	public boolean requiresCustomPersistence() {
		return isTame();
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTame();
	}

//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}
	
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag) {
		Biome biome = worldIn.getBiome(new BlockPos(position()));
		ResourceLocation res = worldIn.getLevel().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
		
		if(res.equals(Biomes.SOUL_SAND_VALLEY.location()))
			setBlue(true);
		
		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	public void tick() {
		super.tick();

		if (!level.isClientSide && level.getDifficulty() == Difficulty.PEACEFUL && !isTame()) {
			discard();
			return;
		}

		//		if (!world.isRemote && TinyPotato.tiny_potato != null) {
		//			if (timeUntilPotatoEmerges == 1) {
		//				timeUntilPotatoEmerges = 0;
		//				ItemStack stack = new ItemStack(TinyPotato.tiny_potato);
		//				ItemNBTHelper.setBoolean(stack, "angery", true);
		//				entityDropItem(stack, 0f);
		//				playSound(SoundEvents.ENTITY_GENERIC_HURT, 1f, 1f);
		//			} else if (timeUntilPotatoEmerges > 1) {
		//				timeUntilPotatoEmerges--;
		//			}
		//		}

		if (WantLoveGoal.needsPets(this)) {
			Entity owner = getOwner();
			if (owner != null && owner.distanceToSqr(this) < 1 && !owner.isInWater() && !owner.fireImmune() && (!(owner instanceof Player) || !((Player) owner).isCreative()))
				owner.setSecondsOnFire(5);
		}

		Vec3 pos = position();
		if(this.level.isClientSide) {
			SimpleParticleType particle = ParticleTypes.FLAME;
			if(isSleeping())
				particle = ParticleTypes.SMOKE;
			else if(isBlue())
				particle = ParticleTypes.SOUL_FIRE_FLAME;
			
			this.level.addParticle(particle, pos.x + (this.random.nextDouble() - 0.5D) * this.getBbWidth(), pos.y + (this.random.nextDouble() - 0.5D) * this.getBbHeight(), pos.z + (this.random.nextDouble() - 0.5D) * this.getBbWidth(), 0.0D, 0.0D, 0.0D);
		}

		if(isTame()) {
			BlockPos below = blockPosition().below();
			BlockEntity tile = level.getBlockEntity(below);
			if (tile instanceof AbstractFurnaceBlockEntity) {
				AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) tile;
				int cookTime = furnace.cookingProgress;
				if (cookTime > 0 && cookTime % 3 == 0) {
					List<Foxhound> foxhounds = level.getEntitiesOfClass(Foxhound.class, new AABB(blockPosition()),
							(fox) -> fox != null && fox.isTame());
					if(!foxhounds.isEmpty() && foxhounds.get(0) == this)
						furnace.cookingProgress = furnace.cookingProgress == 3 ? 5 :Math.min(furnace.cookingTotalTime - 1, cookTime + 1);
				}
			}
		}
	}

	@Override
	public boolean isInWaterOrRain() {
		return false;
	}

	@Nonnull
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return FOXHOUND_LOOT_TABLE;
	}

	protected SleepGoal sleepGoal;

	@Override
	protected void registerGoals() {
		this.sleepGoal = new SleepGoal(this);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, this.sleepGoal);
		this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new FindPlaceToSleepGoal(this, 0.8D, true));
		this.goalSelector.addGoal(9, new FindPlaceToSleepGoal(this, 0.8D, false));
		this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(11, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Animal.class, false,
				target -> target instanceof Sheep || target instanceof Rabbit));
		this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Player.class, false,
				target -> !isTame()));
//		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
	}

	@Override
	public int getRemainingPersistentAngerTime() {
		if (!isTame() && level.getDifficulty() != Difficulty.PEACEFUL)
			return 0;
		return super.getRemainingPersistentAngerTime();
	}
	
	@Override
	public boolean doHurtTarget(Entity entityIn) {
		if (entityIn.getType().fireImmune()) {
			if (entityIn instanceof Player)
				return false;
			return super.doHurtTarget(entityIn);
		}

		boolean flag = entityIn.hurt(DamageSource.mobAttack(this).setIsFire(),
				((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));

		if (flag) {
			entityIn.setSecondsOnFire(5);
			this.doEnchantDamageEffects(this, entityIn);
		}

		return flag;
	}

	@Override
	public boolean hurt(@Nonnull DamageSource source, float amount) {
		setWoke();
		return super.hurt(source, amount);
	}

	@Nonnull
	@Override
	public InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if(itemstack.getItem() == Items.BONE && !isTame())
			return InteractionResult.PASS;

		if (!this.isTame() && !itemstack.isEmpty()) {
			if (itemstack.getItem() == Items.COAL && (level.getDifficulty() == Difficulty.PEACEFUL || player.isCreative() || player.getEffect(MobEffects.FIRE_RESISTANCE) != null) && !level.isClientSide) {
				if (random.nextDouble() < FoxhoundModule.tameChance) {
					this.tame(player);
					this.navigation.stop();
					this.setTarget(null);
					this.setOrderedToSit(true);
					this.setHealth(20.0F);
					this.level.broadcastEntityEvent(this, (byte)7);
				} else {
					this.level.broadcastEntityEvent(this, (byte)6);
				}

				if (!player.isCreative())
					itemstack.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}

		//		if (itemstack.getItem() == Item.getItemFromBlock(TinyPotato.tiny_potato)) {
		//			this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1F, 0.5F + (float) Math.random() * 0.5F);
		//			if (!player.isCreative())
		//				itemstack.shrink(1);
		//
		//			this.timeUntilPotatoEmerges = 1201;
		//
		//			return true;
		//		}

		if (!level.isClientSide) {
			setWoke();
		}

		return super.mobInteract(player, hand);
	}

	@Override
	public boolean canMate(Animal otherAnimal) {
		return super.canMate(otherAnimal) && otherAnimal instanceof Foxhound;
	}

	@Override // createChild
	public Wolf getBreedOffspring(ServerLevel sworld, AgeableMob otherParent) {
		Foxhound kid = new Foxhound(FoxhoundModule.foxhoundType, this.level);
		UUID uuid = this.getOwnerUUID();

		if (uuid != null) {
			kid.setOwnerUUID(uuid);
			kid.setTame(true);
		}
		
		if(isBlue())
			kid.setBlue(true);

		return kid;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		compound.putInt("OhLawdHeComin", timeUntilPotatoEmerges);
		compound.putBoolean("IsSlep", isSleeping());
		compound.putBoolean("IsBlue", isBlue());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		timeUntilPotatoEmerges = compound.getInt("OhLawdHeComin");
		setInSittingPose(compound.getBoolean("IsSlep"));
		setBlue(compound.getBoolean("IsBlue"));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if (isSleeping()) {
			return null;
		}
		if (this.isAngry()) {
			return QuarkSounds.ENTITY_FOXHOUND_GROWL;
		} else if (this.random.nextInt(3) == 0) {
			return this.isTame() && this.getHealth() < 10.0F ? QuarkSounds.ENTITY_FOXHOUND_WHINE : QuarkSounds.ENTITY_FOXHOUND_PANT;
		} else {
			return QuarkSounds.ENTITY_FOXHOUND_IDLE;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_FOXHOUND_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_FOXHOUND_DIE;
	}

	@Override
	public boolean isSleeping() {
		return entityData.get(SLEEPING);
	}
	
	public boolean isBlue() {
		return entityData.get(IS_BLUE);
	}

	@Override
	public void setInSittingPose(boolean sleeping) {
		entityData.set(SLEEPING, sleeping);
	}
	
	public void setBlue(boolean blue) {
		entityData.set(IS_BLUE, blue);
	}

//	public static boolean canSpawnHere(IServerWorld world, BlockPos pos, Random rand) {
//		if (world.getLightFor(LightType.SKY, pos) > rand.nextInt(32)) {
//			return false;
//		} else {
//			int light = world.getWorld().isThundering() ? world.getNeighborAwareLightSubtracted(pos, 10) : world.getLight(pos);
//			return light <= rand.nextInt(8);
//		}
//	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
		return worldIn.getBlockState(pos.below()).is(FoxhoundModule.foxhoundSpawnableTag) ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
	}
	
	public static boolean spawnPredicate(EntityType<? extends Foxhound> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.below()).is(FoxhoundModule.foxhoundSpawnableTag);
	}

	public SleepGoal getSleepGoal() {
		return sleepGoal;
	}

	private void setWoke() {
		SleepGoal sleep = getSleepGoal();
		if(sleep != null) {
			setInSittingPose(false);
			sleep.setSleeping(false);
		}
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}