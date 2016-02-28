package net.teamio.familiars.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.teamio.familiars.FamiliarsMain;
import net.teamio.familiars.gui.SavableInventory;

public class EntityFamiliar extends EntityLiving {

	public static final int SLOTCOUNT = 10;
	
	public final SavableInventory inventory;
	
	public UUID owner;
	
	public final FamiliarTasks tasks;
	
	
	public EntityFamiliar(World worldIn) {
		super(worldIn);
		inventory = new SavableInventory(getDisplayName(), SLOTCOUNT);
		tasks = new FamiliarTasks();
	}
	
	@Override
	public boolean allowLeashing() {
		return true;
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if(!worldObj.isRemote) {
			for(int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if(stack != null && stack.getItem() != null) {
					this.entityDropItem(stack, 0.5f);
				}
			}
		}
		inventory.clear();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		// TODO Auto-generated method stub
		super.writeEntityToNBT(tagCompound);
	}
	
	@Override
	protected boolean interact(EntityPlayer player) {
		if(worldObj.isRemote) {
			return true;
		}
		if(owner == null) {
			owner = player.getPersistentID();
			player.addChatMessage(new ChatComponentTranslation("familiars.hellomaster", player.getDisplayName()));
			return true;
		} else {
			if(player.getPersistentID().equals(owner)) {
				ItemStack stack = player.getCurrentEquippedItem();
				Item item;
				if(stack == null || (item = stack.getItem()) == null) {
					player.openGui(FamiliarsMain.instance, 0, this.worldObj, this.getEntityId(), 0, 0);
				} else {
					if(item == Items.bone) {
						tasks.follow = !tasks.follow;
						if(tasks.follow) {
							player.addChatMessage(new ChatComponentTranslation("familiars.follow_on"));
						} else {
							player.addChatMessage(new ChatComponentTranslation("familiars.follow_off"));
						}
					}
					if(item == Items.string) {
						tasks.collect = !tasks.collect;
						if(tasks.collect) {
							player.addChatMessage(new ChatComponentTranslation("familiars.collect_on"));
						} else {
							player.addChatMessage(new ChatComponentTranslation("familiars.collect_off"));
						}
					}
					if(item == Items.arrow) {
						//Set dropoff
						tasks.dropOff = new BlockPos(this);
						player.addChatMessage(new ChatComponentTranslation("familiars.new_dropoff", DISTANCE_DROPOFF, FILTER_DROPOFF));
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompund) {
		
		NBTTagCompound customData = getEntityData();
		
		NBTTagList inv = new NBTTagList();
		
		inventory.writeToNBT(inv);
		
		customData.setTag("inventory", inv);
		
		if(owner != null) {
			customData.setString("owner", owner.toString());
		}
		

		NBTTagCompound tsk = new NBTTagCompound();
		
		tasks.writeToNBT(tsk);

		customData.setTag("tasks", tsk);
		
		super.writeToNBT(tagCompund);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompund) {
		super.readFromNBT(tagCompund);
		
		NBTTagCompound customData = getEntityData();
		
		NBTTagList inv = customData.getTagList("inventory", NBT.TAG_COMPOUND);
		
		String ownerUUID = customData.getString("owner");
		if(ownerUUID != null) {
			this.owner = UUID.fromString(ownerUUID);
		}

		NBTTagCompound tsk = customData.getCompoundTag("tasks");
		
		if(tsk != null) {
			tasks.readFromNBT(tsk);
		}
		
		if(inv != null) {
			inventory.readFromNBT(inv);
		}
	}
	
	private EntityItem target;
	
	private int targetTimeout = 0;
	
	public static final int TARGET_TIMOUT = 5;
	public static final float DISTANCE_VACUUM = 3;
	public static final float DISTANCE_LOOK = 30;
	public static final float DISTANCE_CHEST = 2;
	public static final int DISTANCE_DROPOFF = 5;
	public static final String FILTER_DROPOFF = "chest";
	
	private boolean droppingOff;
	private List<BlockPos> dropoffTargets;
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
		if(tasks.collect && (target != null || droppingOff)) {
			if(droppingOff) {
				dropOff();
			} else {
				followTarget();
			}
		} else {
			target = null;
			droppingOff = false;
			followPlayer();
		}
	}
	
	private void findNewTarget() {
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(posX - DISTANCE_LOOK, posY - DISTANCE_LOOK, posZ - DISTANCE_LOOK, posX + DISTANCE_LOOK, posY + DISTANCE_LOOK, posZ + DISTANCE_LOOK));
		boolean foundUnfittable = false;
		for(EntityItem item : items) {
			if(canFitInInventory(item.getEntityItem())) {
				if(this.navigator.getPathToEntityLiving(item) != null) {
					foundUnfittable = false;
					target = item;
					break;
				}
			} else {
				foundUnfittable = true;
			}
		}
		if(foundUnfittable) {
			beginDropOff();
		}
	}
	
	private void dropOff() {
		if(dropoffTargets.isEmpty()) {
			if(!isInventoryEmpty()) {
				System.out.println("Dropoff failed, not empty");
				//TODO player.addChatMessage(new ChatComponentTranslation("familiars.dropoffFull"));
			} else {
				System.out.println("Dropoff complete.");
			}
			droppingOff = false;
			return;
		}
		BlockPos next = dropoffTargets.get(0);
		double dist = getDistance(next.getX(), next.getY(), next.getZ());
		if(dist < DISTANCE_CHEST) {
			dropOffInventory(next);
			dropoffTargets.remove(0);
			if(isInventoryEmpty()) {
				dropoffTargets = null;
				droppingOff = false;
			}
		} else {
			if(!this.navigator.tryMoveToXYZ(next.getX(), next.getY(), next.getZ(), 0.3)) {
				dropoffTargets.remove(0);
			}
		}
	}
	
	private void dropOffInventory(BlockPos target) {
		TileEntity te = worldObj.getTileEntity(target);
		
		if(te instanceof IInventory) {
			IInventory inventory = (IInventory) te;
			
			for(int i = 0; i < this.inventory.getSizeInventory(); i++) {
				ItemStack stack = this.inventory.getStackInSlot(i);
				if(stack != null && stack.getItem() != null) {
					tryFitInInventory(inventory, stack);
					if(stack.stackSize == 0) {
						this.inventory.setInventorySlotContents(i, null);
					}
				}
			}
		}
	}
	
	private boolean isInventoryEmpty() {
		for(int i = 0; i < this.inventory.getSizeInventory(); i++) {
			ItemStack stack = this.inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() != null) {
				return false;
			}
		}
		return true;
	}
	
	private void beginDropOff() {
		System.out.println("Beginning Dropoff");
		if(tasks.dropOff == null) {
			System.out.println("No Target, cancelling");
			return;
		}
		dropoffTargets = new ArrayList<BlockPos>();
		
		for(int x = tasks.dropOff.getX() - DISTANCE_DROPOFF; x <= tasks.dropOff.getX() + DISTANCE_DROPOFF; x++) {
			for(int y = tasks.dropOff.getY() - DISTANCE_DROPOFF; y <= tasks.dropOff.getY() + DISTANCE_DROPOFF; y++) {
				for(int z = tasks.dropOff.getZ() - DISTANCE_DROPOFF; z <= tasks.dropOff.getZ() + DISTANCE_DROPOFF; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					TileEntity te = worldObj.getTileEntity(pos);
					
					if(te instanceof IInventory) {
						IInventory inventory = (IInventory) te;
						System.out.println(inventory.getName());
						
						//TODO: Filter.
						
						dropoffTargets.add(pos);
						
					}
					
				}
			}
		}
		
		//TODO: player.addChatMessage(new ChatComponentTranslation("familiars.dropoff", dropoffTargets.size()));
		
		droppingOff = true;
	}
	
	private boolean canFitInInventory(ItemStack stack) {
		if(stack == null) {
			return false;
		}
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack inInventory = inventory.getStackInSlot(i);
			if(inInventory == null || inInventory.getItem() == null) {
				return true;
			} else if(inInventory.isItemEqual(stack) && inInventory.stackSize < inInventory.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}
	
	private void followPlayer() {

		targetTimeout --;

		if(targetTimeout <= 0) {
			findNewTarget();
			targetTimeout = TARGET_TIMOUT;
			if(target != null) {
				this.getNavigator().clearPathEntity();
				return;
			}
		}
		if(tasks.follow) {
			EntityPlayer player;
			if(owner == null) {
				player = worldObj.getClosestPlayerToEntity(this, DISTANCE_LOOK);
			} else {
				player = worldObj.getPlayerEntityByUUID(owner);
			}
			
			if(player == null) {
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
				this.navigator.clearPathEntity();
			} else {
				this.navigator.tryMoveToEntityLiving(player, 0.3);
			}
		}
	}
	
	private void followTarget() {
		if(target == null || !target.isEntityAlive()) {
			target = null;
			return;
		}
		float dist = target.getDistanceToEntity(this);
		if(dist < DISTANCE_VACUUM) {
			vacuumItems();
			findNewTarget();
		} else {

			if(!this.navigator.tryMoveToEntityLiving(target, 0.3)) {
				target = null;
			}
		}
	}
	
	private void vacuumItems() {
		System.out.println("Vacuuming");
		if(!worldObj.isRemote) {
			List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class,
					new AxisAlignedBB(posX - DISTANCE_VACUUM, posY - DISTANCE_VACUUM, posZ - DISTANCE_VACUUM, posX + DISTANCE_VACUUM, posY + DISTANCE_VACUUM, posZ + DISTANCE_VACUUM));
			for(EntityItem item : items) {
				tryFitInInventory(item);
			}
		}
		target = null;
	}
	
	private void tryFitInInventory(EntityItem ei) {
		if(ei == null) {
			return;
		}
		ItemStack stack = ei.getEntityItem();
		if(stack == null) {
			return;
		}
		tryFitInInventory(inventory, stack);
		if(stack.stackSize == 0) {
			ei.setDead();
			return;
		}
	}
	
	private void tryFitInInventory(IInventory target, ItemStack stack) {
		for(int i = 0; i < target.getSizeInventory(); i++) {
			ItemStack inInventory = target.getStackInSlot(i);
			if(inInventory != null && inInventory.getItem() != null && inInventory.isItemEqual(stack) && inInventory.stackSize < inInventory.getMaxStackSize()) {
				int sizeToTransfer = Math.min(inInventory.getMaxStackSize() - inInventory.stackSize, stack.stackSize);
				inInventory.stackSize += sizeToTransfer;
				stack.stackSize -= sizeToTransfer;
				if(stack.stackSize == 0) {
					return;
				}
			}
		}
		for(int i = 0; i < target.getSizeInventory(); i++) {
			ItemStack inInventory = target.getStackInSlot(i);
			if(inInventory == null || inInventory.getItem() == null) {
				target.setInventorySlotContents(i, stack.copy());
				stack.stackSize = 0;
				return;
			}	
		}
	}
	
}
