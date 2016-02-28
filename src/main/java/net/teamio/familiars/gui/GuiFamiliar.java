package net.teamio.familiars.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldNameable;
import net.teamio.familiars.entities.EntityFamiliar;

public class GuiFamiliar extends GuiContainer {
	ResourceLocation bg = new ResourceLocation("familiars:textures/gui/familiar.png");

	private EntityFamiliar familiar;
	private InventoryPlayer inventoryPlayer;

	public GuiFamiliar(InventoryPlayer inventoryPlayer, EntityFamiliar familiar) {
		super(new ContainerFamiliar(inventoryPlayer, familiar));
		this.inventoryPlayer = inventoryPlayer;
		this.familiar = familiar;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		this.fontRendererObj.drawString(familiar.getDisplayName().getFormattedText(), 8, 6, 0x404040);
		this.fontRendererObj.drawString(getTranslatedInventoryName(inventoryPlayer), 8, this.ySize - 110 + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(bg);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	public static String getTranslatedInventoryName(IWorldNameable inventory) {
		if (inventory.hasCustomName()) {
			return inventory.getDisplayName().getFormattedText();
		} else {
			return I18n.format(inventory.getDisplayName().getFormattedText(), new Object[0]);
		}
	}

}
