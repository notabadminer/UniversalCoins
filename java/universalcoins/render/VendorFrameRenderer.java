package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;

public class VendorFrameRenderer extends TileEntitySpecialRenderer {

	
	RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
    public String blockIcon = "";


	public VendorFrameRenderer() {
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double posX,
			double posY, double posZ, float p_180535_8_, int p_180535_9_) {
		blockIcon = "";
		TileVendor te = (TileVendor) tileentity;
		//default texture
		ResourceLocation blockTexture = (new ResourceLocation("textures/blocks/planks_birch.png"));
		//change texture based on plank type
		if (((TileVendorFrame) tileentity).blockIcon != "") {
			blockIcon = (((TileVendorFrame) tileentity).blockIcon);
		}
		if (blockIcon != null && blockIcon != "") {
			String[] tempIconName = blockIcon.split(":", 3); //split string
			blockTexture = (new ResourceLocation(tempIconName[0] + ":textures/" + tempIconName[1] + ".png"));
		}
		
		this.bindTexture(blockTexture);
		int	meta = 0;

		// adjust block rotation based on block meta
		try {
		 	meta = tileentity.getBlockMetadata();
		} catch (Throwable ex2) {
			// do nothing
		}
		float correction = 0.0F;
		if (meta == 2) {
			correction = 180.0F;
		}
		if (meta == 4) {
			correction = 90.0F;
		}
		if (meta == 5) {
			correction = -90.0F;
		}

		//render block
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX + 0.5F,(float) posY + 0.5F,(float) posZ + 0.5F);
		GlStateManager.rotate(-correction, 0F, 1F, 0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		worldrenderer.startDrawingQuads();
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		
		// back
		worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0, 0.125, 0.125);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0, 0.125, 0.875);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0, 0.875, 0.875);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0, 0.875, 0.125);

		//top
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0, 0.125, 0);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0.0625, 0.125, 0.0625);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0.0625, 0.875, 0.0625);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0, 0.875, 0);
		
		//bottom
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0.0625, 0.875, 0.9375);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0, 0.875, 0.875);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0, 0.125, 0.875);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0.0625, 0.125, 0.9375);

		//left
		worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0.0625, 0, 0.125);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0, 0.125, 0.125);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0, 0, 0.875);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0.0625, 0.125, 0.875);
		
		//right
		worldrenderer.setNormal(1.0F, 0.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0, 0.875, 0.125);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0.0625, 0.75, 0.125);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0.0625, 0.75, 0.875);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0, 0.875, 0.875);
		
		// front center
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.8125, 0.1875);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.1875, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.1875, 0.1875);
		
		// front left
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.125, 0.875, 0.0625, 0.1875, 0.125);
		worldrenderer.addVertexWithUV(0.125, 0.125, 0.0625, 0.1875, 0.875);
		worldrenderer.addVertexWithUV(0.1875, 0.125, 0.0625, 0.125, 0.875);
		worldrenderer.addVertexWithUV(0.1875, 0.875, 0.0625, 0.125, 0.125);
		
		// front right
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.8125, 0.875, 0.0625, 0.875, 0.125);
		worldrenderer.addVertexWithUV(0.8125, 0.125, 0.0625, 0.875, 0.875);
		worldrenderer.addVertexWithUV(0.875, 0.125, 0.0625, 0.8125, 0.875);
		worldrenderer.addVertexWithUV(0.875, 0.875, 0.0625, 0.8125, 0.125);
		
		// front top
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.875, 0.0625, 0.1875, 0.125);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.1875, 0.1875);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.1875);
		worldrenderer.addVertexWithUV(0.8125, 0.875, 0.0625, 0.8125, 0.125);
		
		// front bottom
		worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.1875, 0.125, 0.0625, 0.8125, 0.875);
		worldrenderer.addVertexWithUV(0.8125, 0.125, 0.0625, 0.1875, 0.875);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.1875, 0.8125);
		
		// inside left
		worldrenderer.setNormal(1.0F, 0.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.8125, 0.1875);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.8125, 0.1875);
		
		// inside right
		worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.8125, 0.1875);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.8125, 0.1875);
		
		// inside top
		worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.0625, 0.1875, 0.1875);
		worldrenderer.addVertexWithUV(0.1875, 0.8125, 0.03125, 0.1875, 0.21875);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.03125, 0.8125, 0.21875);
		worldrenderer.addVertexWithUV(0.8125, 0.8125, 0.0625, 0.8125, 0.1875);
		
		// inside bottom
		worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.03125, 0.8125, 0.78035);
		worldrenderer.addVertexWithUV(0.1875, 0.1875, 0.0625, 0.8125, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.0625, 0.1875, 0.8125);
		worldrenderer.addVertexWithUV(0.8125, 0.1875, 0.03125, 0.1875, 0.78035);
		
		tessellator.draw();
		
		//render trade item or block
		ItemStack itemstack = ((TileVendorFrame) te).getSellItem();
		if (itemstack != null) {
			ItemStack visStack = itemstack.copy();
			visStack.stackSize = 1;
			GlStateManager.translate(0.5F, 0.5F, 0.0635F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			EntityItem entityitem = new EntityItem(null, 0.0D,  0.0D,  0.0D, visStack);
            entityitem.hoverStart = 0.0F;
            renderer.renderItemModel(entityitem.getEntityItem());
        }
		GlStateManager.popMatrix();
	}
}
