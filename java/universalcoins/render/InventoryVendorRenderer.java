package universalcoins.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendorFrame;

public class InventoryVendorRenderer extends TileEntityItemStackRenderer {
	
	private TileVendorFrame teVendorFrame = new TileVendorFrame();
	private String blockIcon;

	@Override
	public void renderByItem(ItemStack itemStack) {
		Block block = Block.getBlockFromItem(itemStack.getItem());

		if (block == UniversalCoins.proxy.blockVendorFrame) {
			NBTTagCompound tag = itemStack.getTagCompound();
			blockIcon = "";
			if (tag != null) {
				blockIcon = tag.getString("BlockIcon");
				//if itemStack has blockIcon, use it to save cpu cycles
				if (blockIcon == "") {
					NBTTagList tagList = tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
					byte slot = tag.getByte("Texture");
					ItemStack textureStack = ItemStack.loadItemStackFromNBT(tag);
					if (textureStack != null) {
						blockIcon = getBlockIcon(textureStack);
					}
				}
			}
			teVendorFrame.blockIcon = blockIcon;
			
			GlStateManager.rotate(45, 0, -1, 0);
			GlStateManager.scale(1.7F, 1.7F, 1.7F);
			GlStateManager.translate(-0.1F, -0.2F, 0.0F);
			TileEntityRendererDispatcher.instance.renderTileEntityAt(this.teVendorFrame, 0.0D, 0.0D, 0.0D, 0.0F);
		} else {
			super.renderByItem(itemStack);
		}
	}
	
	private String getBlockIcon(ItemStack stack) {
		ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		String blockIcon = imm.getItemModel(stack).getTexture().getIconName();
		//the iconIndex function does not work with BOP so we have to do a bit of a hack here
		if (blockIcon.startsWith("biomesoplenty")){
			String[] iconInfo = blockIcon.split(":");
			String[] blockName = stack.getUnlocalizedName().split("\\.", 3);
			String woodType = blockName[2].replace("Plank", "");
			//hellbark does not follow the same naming convention
			if (woodType.contains("hell")) woodType = "hell_bark";
			blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
			//bamboo needs a hack too
			if (blockIcon.contains("bamboo")) blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
		}
		return blockIcon;
	}
}
