package net.teamio.familiars.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.teamio.familiars.entities.EntityFamiliar;

public class GuiFamiliar extends GuiContainer {
	ResourceLocation bg = new ResourceLocation("textures/gui/container/hopper.png");

	public GuiFamiliar(InventoryPlayer inventoryPlayer, EntityFamiliar familiar) {
		super(new ContainerFamiliar(inventoryPlayer, familiar));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(bg);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

}
