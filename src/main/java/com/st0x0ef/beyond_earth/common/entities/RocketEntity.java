package com.st0x0ef.beyond_earth.common.entities;

import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.network.NetworkHooks;
import com.st0x0ef.beyond_earth.BeyondEarth;
import com.st0x0ef.beyond_earth.common.blocks.RocketLaunchPad;
import com.st0x0ef.beyond_earth.common.blocks.entities.machines.gauge.GaugeValueHelper;
import com.st0x0ef.beyond_earth.common.blocks.entities.machines.gauge.IGaugeValue;
import com.st0x0ef.beyond_earth.common.blocks.entities.machines.gauge.IGaugeValuesProvider;
import com.st0x0ef.beyond_earth.common.events.forge.SetPlanetSelectionMenuNeededNbtEvent;
import com.st0x0ef.beyond_earth.common.events.forge.SetRocketItemStackEvent;
import com.st0x0ef.beyond_earth.common.keybinds.KeyVariables;
import com.st0x0ef.beyond_earth.common.menus.RocketMenu;
import com.st0x0ef.beyond_earth.common.registries.ItemsRegistry;
import com.st0x0ef.beyond_earth.common.registries.ParticleRegistry;
import com.st0x0ef.beyond_earth.common.registries.SoundRegistry;
import com.st0x0ef.beyond_earth.common.registries.TagRegistry;
import com.st0x0ef.beyond_earth.common.util.FluidUtils;
import com.st0x0ef.beyond_earth.common.util.Methods;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RocketEntity extends IVehicleEntity implements HasCustomInventoryScreen, IGaugeValuesProvider {
	public static final int DEFAULT_FUEL_BUCKETS = 3;
	public static final long DEFAULT_DISTANCE_TRAVELABLE = 38000000;
	public static final int DEFAULT_FUEL_USAGE = 1000000;
	public static final String DEFAULT_SKIN_TEXTURE = "textures/vehicle/rocket_skin/tiny/standard.png";

	public static final EntityDataAccessor<Boolean> ROCKET_START = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> FUEL = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> FUEL_BUCKET_NEEDED = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> FUEL_USAGE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<String> SKIN_TEXTURE_PATH = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.STRING);

	public static final EntityDataAccessor<Long> MAX_DISTANCE_TRAVELABLE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.LONG);
	public static final EntityDataAccessor<Integer> START_TIMER = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);

	public RocketEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.entityData.define(ROCKET_START, false);
		this.entityData.define(FUEL, 0);
		this.entityData.define(FUEL_BUCKET_NEEDED, DEFAULT_FUEL_BUCKETS);
		this.entityData.define(FUEL_USAGE, DEFAULT_FUEL_USAGE);
		this.entityData.define(MAX_DISTANCE_TRAVELABLE, DEFAULT_DISTANCE_TRAVELABLE);
		this.entityData.define(START_TIMER, 0);
		this.entityData.define(SKIN_TEXTURE_PATH, DEFAULT_SKIN_TEXTURE);
	}

	public double getRocketSpeed() {
		return Mth.clamp(0.65 * this.getEntityData().get(FUEL_USAGE) / (DEFAULT_FUEL_USAGE), 0.7, 1.5);
	}

	public double getMaxDistanceTravelable() {
		long maxDistance = DEFAULT_DISTANCE_TRAVELABLE + (long) this.getEntityData().get(FUEL_BUCKET_NEEDED) * this.getEntityData().get(FUEL_USAGE);
		this.getEntityData().set(MAX_DISTANCE_TRAVELABLE, maxDistance);
		return (double) maxDistance;
	}

	public int getBucketsOfFull() {
		return this.getEntityData().get(FUEL_BUCKET_NEEDED);
	}

	public double getPassengersRidingOffset() {
		return super.getPassengersRidingOffset() - 2.35;
	}

	public ItemStack getRocketItem() {
		ItemStack itemStack = new ItemStack(ItemsRegistry.ROCKET_ITEM.get(), 1);
		itemStack.getOrCreateTag().putInt(BeyondEarth.MODID + ":fuel", this.getEntityData().get(FUEL));
		itemStack.getOrCreateTag().putInt("fuelCapacityModifier", this.getEntityData().get(FUEL_BUCKET_NEEDED) - DEFAULT_FUEL_BUCKETS);
		itemStack.getOrCreateTag().putInt("fuelUsageModifier", this.getEntityData().get(FUEL_USAGE) - DEFAULT_FUEL_USAGE);
		itemStack.getOrCreateTag().putString("rocket_skin_texture", this.getEntityData().get(SKIN_TEXTURE_PATH));
		MinecraftForge.EVENT_BUS.post(new SetRocketItemStackEvent(this, itemStack));

		return itemStack;
	}

	public void setSkinTexture(String texture) {
		this.getEntityData().set(SKIN_TEXTURE_PATH, texture);
		this.getPersistentData().putString("rocket_skin_texture", texture);
	}

	public String getSkinTexture() {
		return this.getPersistentData().getString("rocket_skin_texture");
	}

	public void spawnParticle() {
		if (this.level() instanceof ServerLevel level) {
			Vec3 vec = this.getDeltaMovement();

			if (this.entityData.get(START_TIMER) == 200) {
				for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
					level.sendParticles(player, (ParticleOptions) ParticleRegistry.LARGE_FLAME_PARTICLE.get(), true, this.getX() - vec.x, this.getY() - vec.y - 2.2, this.getZ() - vec.z, 20, 0.1, 0.1, 0.1, 0.001);
					level.sendParticles(player, (ParticleOptions) ParticleRegistry.LARGE_SMOKE_PARTICLE.get(), true, this.getX() - vec.x, this.getY() - vec.y - 3.2, this.getZ() - vec.z, 10, 0.1, 0.1, 0.1, 0.04);
				}
			} else {
				for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
					level.sendParticles(player, ParticleTypes.CAMPFIRE_COSY_SMOKE, true, this.getX() - vec.x, this.getY() - vec.y - 0.1, this.getZ() - vec.z, 6, 0.1, 0.1, 0.1, 0.023);
				}
			}
		}
	}

	public int getFuelCapacity() {
		return this.getBucketsOfFull() * FluidUtils.BUCKET_SIZE;
	}

	public IGaugeValue getFuelGauge() {
		int fuel = this.getEntityData().get(FUEL);
		int capacity = this.getFuelCapacity();
		return GaugeValueHelper.getFuel(fuel, capacity);
	}

	@Override
	public List<IGaugeValue> getDisplayGaugeValues() {
		return Collections.singletonList(this.getFuelGauge());
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public void push(Entity entity) {

	}

	@Override
	public void kill() {
		this.dropEquipment();
		this.spawnRocketItem();

		if (!this.level().isClientSide) {
			this.remove(RemovalReason.DISCARDED);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float p_21017_) {
		Entity sourceEntity = source.getEntity();

		if (sourceEntity != null && sourceEntity.isCrouching() && !this.isVehicle()) {

			this.spawnRocketItem();
			this.dropEquipment();

			if (!this.level().isClientSide) {
				this.remove(RemovalReason.DISCARDED);
			}

			return true;
		}

		return false;
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return this.getRocketItem();
	}

	protected void spawnRocketItem() {
		ItemEntity entityToSpawn = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getRocketItem());
		entityToSpawn.setPickUpDelay(10);

		this.level().addFreshEntity(entityToSpawn);
	}

	protected void dropEquipment() {
		for (int i = 0; i < this.inventory.getSlots(); ++i) {
			ItemStack itemstack = this.inventory.getStackInSlot(i);
			if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
				this.spawnAtLocation(itemstack);
			}
		}
	}

	private final ItemStackHandler inventory = new ItemStackHandler(10) {
		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}
	};

	public ItemStackHandler getInventory() {
		return this.inventory;
	}

	private final CombinedInvWrapper combined = new CombinedInvWrapper(this.inventory);

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
		if (this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER && side == null) {
			return LazyOptional.of(() -> this.combined).cast();
		}
		return super.getCapability(capability, side);
	}

	public IItemHandlerModifiable getItemHandler() {
		return (IItemHandlerModifiable) this.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().get();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.put("InventoryCustom", this.inventory.serializeNBT());

		compound.putBoolean("rocket_start", this.getEntityData().get(ROCKET_START));
		compound.putInt("fuel", this.getEntityData().get(FUEL));
		compound.putInt("start_timer", this.getEntityData().get(START_TIMER));
		compound.putInt("fuel_capacity", this.getEntityData().get(FUEL_BUCKET_NEEDED));
		compound.putInt("fuel_usage", this.getEntityData().get(FUEL_USAGE));
		compound.putString("rocket_skin_texture", this.getEntityData().get(SKIN_TEXTURE_PATH));
		compound.putDouble(BeyondEarth.MODID + ":rocket_distance", this.getEntityData().get(MAX_DISTANCE_TRAVELABLE));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		Tag inventoryCustom = compound.get("InventoryCustom");
		if (inventoryCustom instanceof CompoundTag) {
			this.inventory.deserializeNBT((CompoundTag) inventoryCustom);
		}

		this.getEntityData().set(ROCKET_START, compound.getBoolean("rocket_start"));
		this.getEntityData().set(FUEL, compound.getInt("fuel"));
		this.getEntityData().set(START_TIMER, compound.getInt("start_timer"));
		this.getEntityData().set(FUEL_BUCKET_NEEDED, compound.getInt("fuel_capacity"));
		this.getEntityData().set(FUEL_USAGE, compound.getInt("fuel_usage"));
		this.getEntityData().set(SKIN_TEXTURE_PATH, compound.getString("rocket_skin_texture"));
		this.getEntityData().set(MAX_DISTANCE_TRAVELABLE, (long) compound.getDouble(BeyondEarth.MODID + ":rocket_distance"));
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		super.interact(player, hand);
		InteractionResult result = InteractionResult.sidedSuccess(this.level().isClientSide);

		if (!this.level().isClientSide) {
			if (player.isCrouching()) {
				this.openCustomInventoryScreen(player);
				return InteractionResult.CONSUME;
			}

			player.startRiding(this);
			return InteractionResult.CONSUME;
		}

		return result;
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return RocketEntity.this.getName();
				}

				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
					FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
					packetBuffer.writeVarInt(RocketEntity.this.getId());
					return new RocketMenu.GuiContainer(id, inventory, packetBuffer);
				}
			}, buf -> buf.writeVarInt(RocketEntity.this.getId()));
		}
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Vec3[] avector3d = new Vec3[]{getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot()), getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 22.5F), getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 22.5F), getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 45.0F), getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 45.0F)};
		Set<BlockPos> set = Sets.newLinkedHashSet();
		double d0 = this.getBoundingBox().maxY;
		double d1 = this.getBoundingBox().minY - 0.5D;
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

		for(Vec3 vector3d : avector3d) {
			blockpos$mutable.set(this.getX() + vector3d.x, d0, this.getZ() + vector3d.z);

			for(double d2 = d0; d2 > d1; --d2) {
				set.add(blockpos$mutable.immutable());
				blockpos$mutable.move(Direction.DOWN);
			}
		}

		for(BlockPos blockpos : set) {
			if (!this.level().getFluidState(blockpos).is(FluidTags.LAVA)) {
				double d3 = this.level().getBlockFloorHeight(blockpos);
				if (DismountHelper.isBlockFloorValid(d3)) {
					Vec3 vector3d1 = Vec3.upFromBottomCenterOf(blockpos, d3);

					for(Pose pose : livingEntity.getDismountPoses()) {
						if (DismountHelper.isBlockFloorValid(this.level().getBlockFloorHeight(blockpos))) {
							livingEntity.setPose(pose);
							return vector3d1;
						}
					}
				}
			}
		}

		return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
	}

	@Override
	public void tick() {
		super.tick();

		this.rotateRocket();
		this.checkOnBlocks();
		this.fillUpRocket();
		this.rocketExplosion();
		this.burnEntities();

		if (this.entityData.get(ROCKET_START)) {
			this.spawnParticle();
			this.startTimerAndFlyMovement();

			if (this.getY() > 600) {
				this.openPlanetSelectionMenu();
			}
		}
	}

	public void fillUpRocket() {
		ItemStack slotItem0 = this.getInventory().getStackInSlot(0);
		ItemStack slotItem1 = this.getInventory().getStackInSlot(1);

		if (slotItem0.getItem() instanceof BucketItem) {
			if (((BucketItem) slotItem0.getItem()).getFluid().is(TagRegistry.FLUID_VEHICLE_FUEL_TAG) && this.entityData.get(FUEL) + FluidUtils.BUCKET_SIZE <= this.getFuelCapacity()) {
				if (slotItem1.getCount() != slotItem1.getMaxStackSize()) {
					this.getInventory().extractItem(0, 1, false);
					this.getInventory().insertItem(1, new ItemStack(Items.BUCKET), false);

					this.getEntityData().set(FUEL, this.entityData.get(FUEL) + FluidUtils.BUCKET_SIZE);
				}
			}
		}
	}

	public Player getFirstPlayerPassenger() {
		if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
			return player;
		}

		return null;
	}

	public void rotateRocket() {
		Player player = this.getFirstPlayerPassenger();

		if (player != null) {
			if (KeyVariables.isHoldingRight(player) && KeyVariables.isHoldingLeft(player)) {
				return;
			}

			if (KeyVariables.isHoldingRight(player)) {
				Methods.setEntityRotation(this, 1);
			}

			if (KeyVariables.isHoldingLeft(player)) {
				Methods.setEntityRotation(this, -1);
			}
		}
	}

	public void startRocket() {
		Player player = this.getFirstPlayerPassenger();

		if (player != null) {
			SynchedEntityData data = this.getEntityData();

			if (data.get(RocketEntity.FUEL) == this.getFuelCapacity()) {
				if (!data.get(RocketEntity.ROCKET_START)) {
					data.set(RocketEntity.ROCKET_START, true);
					this.level().playSound(null, this, SoundRegistry.ROCKET_SOUND.get(), SoundSource.NEUTRAL, 1, 1);
				}
			} else {
				Methods.sendVehicleHasNoFuelMessage(player);
			}
		}
	}

	public boolean doesDrop(BlockState state, BlockPos pos) {
		if (this.onGround() || this.isInFluidType()) {

			BlockState state2 = this.level().getBlockState(new BlockPos((int)Math.floor(this.getX()), (int)(this.getY() - 0.2), (int)Math.floor(this.getZ())));

			if (!this.level().isEmptyBlock(pos) && ((state2.getBlock() instanceof RocketLaunchPad && !state2.getValue(RocketLaunchPad.STAGE)) || !(state.getBlock() instanceof RocketLaunchPad))) {

				this.dropEquipment();
				this.spawnRocketItem();

				if (!this.level().isClientSide) {
					this.remove(RemovalReason.DISCARDED);
				}

				return true;
			}
		}

		return false;
	}

	protected void checkOnBlocks() {
		AABB aabb = this.getBoundingBox();
		BlockPos blockPos1 = new BlockPos((int)aabb.minX, (int)(aabb.minY - 0.2), (int)aabb.minZ);
		BlockPos blockPos2 = new BlockPos((int)aabb.maxX, (int)aabb.minY, (int)aabb.maxZ);

		if (this.level().hasChunksAt(blockPos1, blockPos2)) {
			for (int i = blockPos1.getX(); i <= blockPos2.getX(); ++i) {
				for (int j = blockPos1.getY(); j <= blockPos2.getY(); ++j) {
					for (int k = blockPos1.getZ(); k <= blockPos2.getZ(); ++k) {
						BlockPos pos = new BlockPos(i, j, k);
						BlockState state = this.level().getBlockState(pos);

						if (this.doesDrop(state, pos)) {
							return;
						}
					}
				}
			}
		}
	}

	public void startTimerAndFlyMovement() {
		if (this.entityData.get(START_TIMER) < 200) {
			this.entityData.set(START_TIMER, this.entityData.get(START_TIMER) + 1);
		}

		if (this.entityData.get(START_TIMER) == 200) {
			if (this.getDeltaMovement().y < this.getRocketSpeed() - 0.1) {
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y + 0.1, this.getDeltaMovement().z);
			} else {
				this.setDeltaMovement(this.getDeltaMovement().x, this.getRocketSpeed(), this.getDeltaMovement().z);
			}
		}
	}

	public void openPlanetSelectionMenu() {
		Player player = this.getFirstPlayerPassenger();

		if (player != null) {

			if (player.hasContainerOpen()) {
				return;
			}

			player.getPersistentData().putDouble(BeyondEarth.MODID + ":rocket_distance", this.getMaxDistanceTravelable());
			player.getPersistentData().putBoolean(BeyondEarth.MODID + ":planet_selection_menu_open", true);

			/** SAVE ITEMS IN THE PLAYER */
			ListTag tag = new ListTag();

			tag.add(new ItemStack(this.getRocketItem().getItem()).save(new CompoundTag()));

			for (int i = 0; i <= this.getInventory().getSlots() - 1; i++) {
				tag.add(this.getInventory().getStackInSlot(i).save(new CompoundTag()));
			}

			player.getPersistentData().put(BeyondEarth.MODID + ":rocket_item_list", tag);
			player.setNoGravity(true);

			/** STOP ROCKET SOUND */
			if (player instanceof ServerPlayer serverPlayer) {
				Methods.stopSound(serverPlayer, SoundRegistry.ROCKET_SOUND.getId(), SoundSource.NEUTRAL);
			}

			MinecraftForge.EVENT_BUS.post(new SetPlanetSelectionMenuNeededNbtEvent(player, this));
		}

		this.destroyRocket(false);
	}

	public void rocketExplosion() {
		if (this.entityData.get(START_TIMER) == 200) {
			if (this.getDeltaMovement().y < -0.07) {
				destroyRocket(true);
			}
		}
	}

	public void burnEntities() {
		if (this.entityData.get(START_TIMER) == 200) {
			AABB aabb = AABB.ofSize(new Vec3(this.getX(), this.getY() - 2, this.getZ()), 2, 2, 2);
			List<LivingEntity> entities = this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, aabb);

			for (LivingEntity entity : entities) {
				if (!Methods.isLivingInNetheriteSpaceSuit(entity) && !Methods.isLivingInJetSuit(entity)) {
					entity.setSecondsOnFire(15);
				}
			}
		}
	}

	private void destroyRocket(boolean explode) {
		if (!this.level().isClientSide) {
			if (explode) {
				this.level().explode(this, this.getX(), this.getBoundingBox().maxY, this.getZ(), 10, true, Level.ExplosionInteraction.TNT);
			}
			this.remove(RemovalReason.DISCARDED);
		}
	}
}