package universalcoins.render;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

@SideOnly(Side.CLIENT)
public class UCSignRenderer extends TileEntitySpecialRenderer {
	    private int counter = 0;
	    private boolean showStick = false;

		public void renderTileEntityAt(TileEntity tileEntity, double posX, double posY, double posZ, float p_180535_8_, int p_180535_9_) {
			TileUCSign tentity = null;
			if (tileEntity instanceof TileUCSign) {
				tentity = (TileUCSign) tileEntity;
			} else return;
			ResourceLocation blockTexture = new ResourceLocation("textures/blocks/planks_birch.png");
	        Block block = tileEntity.getBlockType();
	        GlStateManager.pushMatrix();
	        float f1 = 0.6666667F;
	        float f3;
	        
	        if (tentity != null && tentity.blockIcon != null && tentity.blockIcon != "") {
	        	String[] tempIconName = tentity.blockIcon.split(":", 3); //split string
				if (tempIconName.length == 1) {
					//if minecraft, set resourcelocation using last part
					blockTexture = (new ResourceLocation("textures/" + tempIconName[0] + ".png"));
				} else {
					//if mod use mod path
					blockTexture = (new ResourceLocation(tempIconName[0] + ":textures/" + tempIconName[1] + ".png"));
				}
	        }

	        if (block == UniversalCoins.proxy.standing_ucsign)
	        {
	            float f2 = (float)(tileEntity.getBlockMetadata() * 360) / 16.0F;
	            GlStateManager.translate((float)posX + 0.5F,(float)posY + 0.5F,(float)posZ + 0.5F);
	            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
	            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
	            GlStateManager.translate(0.0F, 0.3F, 0.45F);
	            showStick = true;
	        }
	        else
	        {
	            int j = tileEntity.getBlockMetadata();
	            f3 = 0.0F;

	            if (j == 2)
	            {
	                f3 = 180.0F;
	            }

	            if (j == 4)
	            {
	                f3 = 90.0F;
	            }

	            if (j == 5)
	            {
	                f3 = -90.0F;
	            }

	            GlStateManager.translate((float)posX + 0.5F,(float)posY + 0.5F,(float)posZ + 0.5F);
	            GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
	            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
	            showStick = false;
	        }

	        this.bindTexture(blockTexture);
	        Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
	        worldrenderer.startDrawingQuads();
			
			// back
			worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.0F, 0.9F);
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.02F, 0.0F, 0.2F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.02F, 1.0F, 0.2F);
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.02F, 1.0F, 0.9F);

			//top
			worldrenderer.setNormal(0.0F, 1.0F, 0.0F);
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.02F, 0.125, 0);
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0.125, 0.0625);
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.1F, 0.875, 0.0625);
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.875, 0);
			
			//bottom
			worldrenderer.setNormal(0.0F, -1.0F, 0.0F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.0F, 0.82F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.02F, 0.0F, 0.9F);
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.02F, 1.0F, 0.9F);
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.1F, 1.0F, 0.82F);

			//left
			worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0, 0.9F);
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.02F, 0.125, 0.9F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.02F, 0, 0.2F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.125, 0.2F);
			
			//right
			worldrenderer.setNormal(1.0F, 0.0F, 0.0F);
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.02F, 0.875, 0.9F);
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.1F, 0.75, 0.9F);
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.1F, 0.75, 0.2F);
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.02F, 0.875, 0.2F);
			
			// front  //LR, TB, FB		//LR, TB
			worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
			worldrenderer.addVertexWithUV(0.0F, 0.28125F, 0.1F, 0.0F, 0.2F); //BL
			worldrenderer.addVertexWithUV(1.0F, 0.28125F, 0.1F, 1.0F, 0.2F); //BR
			worldrenderer.addVertexWithUV(1.0F, 0.78125F, 0.1F, 1.0F, 0.9F); //TR
			worldrenderer.addVertexWithUV(0.0F, 0.78125F, 0.1F, 0.0F, 0.9F); //TL
			
			if (showStick) {
				//draw stick
				// front
				worldrenderer.setNormal(0.0F, 0.0F, 1.0F);
				worldrenderer.addVertexWithUV(0.45F, -0.35F, 0.1F, 0.62F, 0.1F); //BL
				worldrenderer.addVertexWithUV(0.55F, -0.35F, 0.1F, 0.62F, 0.0F); //BR
				worldrenderer.addVertexWithUV(0.55F, 0.28125F, 0.1F, 0.0F, 0.0F); //TR
				worldrenderer.addVertexWithUV(0.45F, 0.28125F, 0.1F, 0.0F, 0.1F); //TL
				
				//right
				worldrenderer.setNormal(1.0F, 0.0F, 0.0F);
				worldrenderer.addVertexWithUV(0.55F, -0.35F, 0.1F, 0.62F, 0.0F); //BL
				worldrenderer.addVertexWithUV(0.55F, -0.35F, 0.02F, 0.62F, 0.0F); //BR
				worldrenderer.addVertexWithUV(0.55F, 0.28125F, 0.02F, 0.0F, 0.0F); //TR
				worldrenderer.addVertexWithUV(0.55F, 0.28125F, 0.1F, 0.0F, 0.0F); //TL
				
				//left
				worldrenderer.setNormal(-1.0F, 0.0F, 0.0F);
				worldrenderer.addVertexWithUV(0.45F, -0.35F, 0.02F, 0.62F, 0.1F); //BL
				worldrenderer.addVertexWithUV(0.45F, -0.35F, 0.1F, 0.62F, 0.1F); //BR
				worldrenderer.addVertexWithUV(0.45F, 0.28125F, 0.1F, 0.0F, 0.1F); //TR
				worldrenderer.addVertexWithUV(0.45F, 0.28125F, 0.02F, 0.0F, 0.1F); //TL
				
				//back
				worldrenderer.setNormal(0.0F, 0.0F, -1.0F);
				worldrenderer.addVertexWithUV(0.55F, -0.35F, 0.02F, 0.62F, 0.0F); //BL
				worldrenderer.addVertexWithUV(0.45F, -0.35F, 0.02F, 0.62F, 0.1F); //BR
				worldrenderer.addVertexWithUV(0.45F, 0.28125F, 0.02F, 0.0F, 0.1F); //TR
				worldrenderer.addVertexWithUV(0.55F, 0.28125F, 0.02F, 0.0F, 0.0F); //TL
			}
			
			tessellator.draw();
			
	        FontRenderer fontrenderer = this.getFontRenderer();
	        f3 = 0.016666668F * f1;
	        GlStateManager.translate(0.5F, 0.5F, 0.105F);
	        GlStateManager.scale(f3, -f3, f3);
	        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
	        GlStateManager.disableDepth();
	        int[] colors = {0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 
	        		0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55,
	        		0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};

	        for (int i = 0; i < tentity.signText.length; ++i)
	        {
	            String s = tentity.signText[i].getUnformattedText();
	            int colorCode = 0;
	            if (s.startsWith("§r")) s = s.substring(2);
	            if (s.startsWith("&") && s.length() > 1 && String.valueOf(s.charAt(1)).matches("[0-9a-fA-F]+")) {
        			colorCode = Integer.parseInt(s.substring(1, 2), 16);
        			s = s.substring(2);
        		}

	            if (i == tentity.lineBeingEdited)
	            {
	                s = "> " + s + " <";
	                fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - tentity.signText.length * 5, colors[colorCode]);
	            }
	            else  {
	            	if (s.length() <= 16) {
	            		fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, i * 10 - tentity.signText.length * 5, colors[colorCode]);
	            	} else {
	            		//display a subset of string while scrolling through entire string
	            		String subset = "";
	            		if (counter / 10 < s.length() - 8) {
	            			subset = s.substring(Math.min(counter / 10, s.length() - 15), Math.min(counter / 10 + 15, s.length()));
	            		} else  {	            		
	            			subset = s.substring(0, 15);
	            		}
            			fontrenderer.drawString(subset, -fontrenderer.getStringWidth(subset) / 2, i * 10 - tentity.signText.length * 5, colors[colorCode]);
	            		counter++;
	            		if (counter / 10 > s.length() + 8) counter = 0;
	            	}
	            }
	        }

	        GlStateManager.enableDepth();
	        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	        GlStateManager.popMatrix();
	    }

	    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_)
	    {
	        this.renderTileEntityAt((TileUCSign)p_147500_1_, p_147500_2_, p_147500_4_, p_147500_6_, p_147500_8_);
	    }
}
