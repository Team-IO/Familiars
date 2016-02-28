package net.teamio.familiars;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.teamio.familiars.entities.EntityFamiliar;
import net.teamio.familiars.gui.FamiliarsGuiHandler;

@Mod(modid = Familiars.MOD_ID, version = Familiars.VERSION)
public class FamiliarsMain {

	@SidedProxy(clientSide = "net.teamio.familiars.ClientOnlyProxy", serverSide = "net.teamio.familiars.DedicatedServerProxy")
	public static CommonProxy proxy;

	@Instance
	public static FamiliarsMain instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();

		NetworkRegistry.INSTANCE.registerGuiHandler(FamiliarsMain.instance, new FamiliarsGuiHandler());
		
		int back = (200 << 16) + (40 << 8) + (220);
		int fore = (255 << 16) + (220);

		EntityRegistry.registerModEntity(EntityFamiliar.class, "familiar", 0,
				FamiliarsMain.instance, 128, 5, true, back, fore);

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}
}
