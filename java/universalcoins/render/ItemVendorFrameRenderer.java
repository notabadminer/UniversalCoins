package universalcoins.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.Constants;

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
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
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
		if (item.hasTagCompound()) {
			NBTTagCompound tag = item.getTagCompound();
			blockIcon = tag.getString("BlockIcon");
			// if itemStack has blockIcon, use it
			if (blockIcon == "") {
				NBTTagList tagList = tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				byte slot = tag.getByte("Texture");
				ItemStack textureStack = ItemStack.loadItemStackFromNBT(tag);
				if (textureStack != null) {
					blockIcon = getBlockIcon(textureStack);
				}
			}
		}
		((VendorFrameRenderer) render).blockIcon = blockIcon;
		this.render.renderTileEntityAt(this.dummytile, 0.0D, 0.0D, 0.0D, 0.0F);
	}

	private String getBlockIcon(ItemStack stack) {
		String blockIcon = stack.getIconIndex().getIconName();
		// the iconIndex function does not work with BOP so we have to do a bit
		// of a hack here
		if (blockIcon.startsWith("biomesoplenty")) {
			String[] iconInfo = blockIcon.split(":");
			String[] blockName = stack.getUnlocalizedName().split("\\.", 3);
			String woodType = blockName[2].replace("Plank", "");
			// hellbark does not follow the same naming convention
			if (woodType.contains("hell"))
				woodType = "hell_bark";
			blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
			// bamboo needs a hack too
			if (blockIcon.contains("bamboo"))
				blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
		}
		return blockIcon;
	}
}
