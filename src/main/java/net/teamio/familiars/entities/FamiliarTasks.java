package net.teamio.familiars.entities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class FamiliarTasks {
	
	public boolean follow = true;
	public boolean collect = true;
	public BlockPos dropOff;
	
	public void writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("follow", follow);
		tag.setBoolean("collect", collect);
		
		if(dropOff != null) {
			NBTTagCompound dropoff = new NBTTagCompound();
			
			dropoff.setInteger("x", dropOff.getX());
			dropoff.setInteger("y", dropOff.getY());
			dropoff.setInteger("z", dropOff.getZ());
			
			tag.setTag("dropOff", dropoff);
		}
		
	}
	
	public void readFromNBT(NBTTagCompound tag) {
		follow = tag.getBoolean("follow");
		collect = tag.getBoolean("collect");
		
		NBTTagCompound dropoff = tag.getCompoundTag("dropOff");
		
		if(dropoff == null) {
			dropOff = null;
		} else {
			dropOff = new BlockPos(dropoff.getInteger("x"), dropoff.getInteger("y"), dropoff.getInteger("z"));
		}
	}
}
