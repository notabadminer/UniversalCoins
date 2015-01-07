package universalcoins.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class CardStationRenderer extends TileEntitySpecialRenderer {

	//The model of your block
	private final ModelCardStation model;

	public CardStationRenderer() {
	  this.model = new ModelCardStation();
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double posX,
			double posZ, double p_180535_6_, float p_180535_8_, int p_180535_9_) {
	//public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
		
	ResourceLocation textures = (new ResourceLocation("universalcoins:textures/blocks/blockCardStation.png"));
	Minecraft.getMinecraft().renderEngine.bindTexture(textures);
	
	//adjust block rotation based on block meta
	int meta = te.getBlockMetadata();
	if (meta == -1) { //fix for inventory crash on get block meta
		try { meta = te.getBlockMetadata();
		} catch (Throwable ex2) {
            //do nothing
        }
	}
	
	GL11.glPushMatrix();
	GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
	GL11.glScalef(1.0F, -1F, -1F);
	GL11.glRotatef(meta * 90, 0.0F, 1.0F, 0.0F);
	this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
	GL11.glPopMatrix(); 
	}

	//Set the lighting stuff, so it changes it's brightness properly.
	private void adjustLightFixture(World world, int i, int j, int k, Block block) {
	Tessellator tess = Tessellator.instance;
	float brightness = block.getLightValue();
	//int skyLight = world.getLightBrightnessForSkyBlocks(i, j, k, 0);
	//int modulousModifier = skyLight % 65536;
	//int divModifier = skyLight / 65536;
	tess.setColorOpaque_F(brightness, brightness, brightness);
	OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) modulousModifier, divModifier);
	}
}
