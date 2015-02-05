package universalcoins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ItemVendorFrameRenderer implements IItemRenderer {
	TileEntitySpecialRenderer render;
	private TileEntity dummytile;

	public ItemVendorFrameRenderer(TileEntitySpecialRenderer render, TileEntity dummy) {
		this.render = render;
		this.dummytile = dummy;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (type == IItemRenderer.ItemRenderType.INVENTORY) {
			GL11.glRotatef(45, 0, -1, 0);
			GL11.glScalef(1.5F, 1.5F, 1.5F);
			GL11.glTranslatef(-1F, -1F, -0.5F);
		}
		if (type == IItemRenderer.ItemRenderType.ENTITY) {
			GL11.glRotatef(0, 0, 0, 0);
			GL11.glScalef(1.5F, 1.5F, 1.5F);
			GL11.glTranslatef(0F, -0.5F, -0.5F);
		}
		if (type == IItemRenderer.ItemRenderType.EQUIPPED) {
			GL11.glRotatef(35, 0, -0.5F, 0);
			GL11.glRotatef(70, 0, 0, 1);
			GL11.glScalef(1.5F, 1.5F, 1.5F);
			GL11.glTranslatef(0.6F, -0.7F, -0.2F);
		}
		if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslatef(0F, 0.4F, -1.0F);
		}
		String blockIcon = null;
		if (item.hasTagCompound())blockIcon = item.getTagCompound().getString("blockIcon");
		((VendorFrameRenderer) render).blockIcon = blockIcon;
		this.render.renderTileEntityAt(this.dummytile, 0.0D, 0.0D, 0.0D, 0.0F, 0);
	}
}
