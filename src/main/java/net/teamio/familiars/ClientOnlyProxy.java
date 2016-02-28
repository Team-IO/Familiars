package net.teamio.familiars;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.teamio.familiars.entities.EntityFamiliar;
import net.teamio.familiars.entities.RenderFamiliar;

public class ClientOnlyProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
		RenderingRegistry.registerEntityRenderingHandler(EntityFamiliar.class, new IRenderFactory<EntityFamiliar>() {
			@Override
			public Render<EntityFamiliar> createRenderFor(RenderManager manager) {
				return new RenderFamiliar(manager);
			}
		});
	}
}
