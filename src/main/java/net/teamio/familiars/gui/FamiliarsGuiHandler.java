package net.teamio.familiars.gui;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.teamio.familiars.entities.EntityFamiliar;

public class FamiliarsGuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == 0) {
			Entity ent = world.getEntityByID(x);
			if(ent instanceof EntityFamiliar) {
				return new GuiFamiliar(player.inventory, (EntityFamiliar)ent);
			}
		}
		return null;
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == 0) {
			Entity ent = world.getEntityByID(x);
			if(ent instanceof EntityFamiliar) {
				return new ContainerFamiliar(player.inventory, (EntityFamiliar)ent);
			}
		}
		return null;
	}
}
