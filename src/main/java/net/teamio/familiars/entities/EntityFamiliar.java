package net.teamio.familiars.entities;

import java.util.List;

import javax.vecmath.Vector3d;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.teamio.familiars.FamiliarsMain;
import net.teamio.familiars.gui.SavableInventory;

public class EntityFamiliar extends EntityLiving {

	public static final int SLOTCOUNT = 10;
	
	public final SavableInventory inventory;
	
	public EntityFamiliar(World worldIn) {
		super(worldIn);
		inventory = new SavableInventory(getDisplayName(), SLOTCOUNT);
	}
	
	@Override
	public boolean allowLeashing() {
		return true;
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() != null) {
				this.entityDropItem(stack, 0.5f);
			}
		}
		inventory.clear();
	}

	@Override
	protected boolean interact(EntityPlayer player) {
		player.openGui(FamiliarsMain.instance, 0, this.worldObj, this.getEntityId(), 0, 0);
		
		return true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompund) {
		
		NBTTagCompound customData = getEntityData();
		
		NBTTagList inv = new NBTTagList();
		
		inventory.writeToNBT(inv);
		
		customData.setTag("inventory", inv);
		
		super.writeToNBT(tagCompund);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompund) {
		super.readFromNBT(tagCompund);
		
		NBTTagCompound customData = getEntityData();
		
		NBTTagList inv = customData.getTagList("inventory", NBT.TAG_COMPOUND);
		
		if(inv != null) {
			inventory.readFromNBT(inv);
		}
	}
	
	private EntityItem target;
	
	private int targetTimeout = 0;
	
	public static final int TARGET_TIMOUT = 5;
	public static final float DISTANCE_VACUUM = 3;
	public static final float DISTANCE_LOOK = 30;
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
		if(target == null) {
			followPlayer();
		} else {
			followTarget();
		}
		
	}
	
	private void findNewTarget() {
		System.out.println("New Target");
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(posX - DISTANCE_LOOK, posY - DISTANCE_LOOK, posZ - DISTANCE_LOOK, posX + DISTANCE_LOOK, posY + DISTANCE_LOOK, posZ + DISTANCE_LOOK));
		for(EntityItem item : items) {
			if(canFitInInventory(item.getEntityItem())) {
				if(this.navigator.getPathToEntityLiving(item) != null) {
					target = item;
					break;
				}
			}
		}
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
		
		//TODO: Get Player from "ownership"
		EntityPlayer player = worldObj.getClosestPlayerToEntity(this, DISTANCE_LOOK);
		if(player == null) {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			this.navigator.clearPathEntity();
		} else {
			System.out.println("Follow Player");
//			if(this.navigator.noPath()) {
//				System.out.println("New Path to Player");
				this.navigator.tryMoveToEntityLiving(player, 0.3);
//				Vec3 pso = this.navigator.getPath().getPosition(this);
//				System.out.println(pso + " " + this.posX);
//			}
//			this.navigator.getPath().
//			motion.x = player.posX - posX;
//			motion.y = player.posY - posY;
//			motion.z = player.posZ - posZ;
//			motion.normalize();
//			motion.scale(0.1);
//			this.motionX = motion.x;
//			this.motionY = motion.y;
//			this.motionZ = motion.z;
		}
	}
	
	private Vector3d motion = new Vector3d();
	
	
	
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

			System.out.println("Follow Target " + target);
			if(!this.navigator.tryMoveToEntityLiving(target, 0.3)) {
				target = null;
			}
			
//			motion.x = target.posX - posX;
//			motion.y = target.posY - posY;
//			motion.z = target.posZ - posZ;
//			motion.normalize();
//			motion.scale(0.1);
//			this.motionX = motion.x;
//			this.motionY = motion.y;
//			this.motionZ = motion.z;
		}
	}
	
	private void vacuumItems() {
		System.out.println("Vacuuming");
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(posX - DISTANCE_VACUUM, posY - DISTANCE_VACUUM, posZ - DISTANCE_VACUUM, posX + DISTANCE_VACUUM, posY + DISTANCE_VACUUM, posZ + DISTANCE_VACUUM));
		for(EntityItem item : items) {
			tryFitInInventory(item);
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
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack inInventory = inventory.getStackInSlot(i);
			if(inInventory != null && inInventory.getItem() != null && inInventory.isItemEqual(stack) && inInventory.stackSize < inInventory.getMaxStackSize()) {
				int sizeToTransfer = Math.min(inInventory.getMaxStackSize() - inInventory.stackSize, stack.stackSize);
				inInventory.stackSize += sizeToTransfer;
				stack.stackSize -= sizeToTransfer;
				if(stack.stackSize == 0) {
					ei.setDead();
					return;
				}
			}
		}
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack inInventory = inventory.getStackInSlot(i);
			if(inInventory == null || inInventory.getItem() == null) {
				inventory.setInventorySlotContents(i, stack.copy());
				ei.setDead();
				return;
			}	
		}
	}
	
}
