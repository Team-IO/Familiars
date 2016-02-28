package net.teamio.familiars.entities;

import net.minecraft.client.model.ModelEnderMite;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderFamiliar extends RenderLiving<EntityFamiliar> {
	private static final ResourceLocation textureLocation = new ResourceLocation("textures/entity/endermite.png");
    
	protected ModelEnderMite modelDragon;
	
	public RenderFamiliar(RenderManager renderManager) {
		super(renderManager, new ModelEnderMite(), 0.25F);
        this.modelDragon = (ModelEnderMite)this.mainModel;
	}

	/**
     * Renders the model in RenderLiving
     */
    protected void renderModel(EntityFamiliar entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_)
    {
        this.bindEntityTexture(entitylivingbaseIn);
        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
    }

	@Override
	protected ResourceLocation getEntityTexture(EntityFamiliar entity) {
		return textureLocation;
	}

}
