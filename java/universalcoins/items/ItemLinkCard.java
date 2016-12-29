package universalcoins.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;

public class ItemLinkCard extends Item {

	public ItemLinkCard() {
		super();
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.maxStackSize = 1;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister
				.registerIcon(UniversalCoins.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.stackTagCompound != null) {
			list.add(StatCollector.translateToLocal("item.linkCard.stored"));
		} else {
			list.add(StatCollector.translateToLocal("item.linkCard.blank"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
			float px, float py, float pz) {
		if (world.isRemote)
			return true;
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

		if (movingobjectposition != null
				&& movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			int i = movingobjectposition.blockX;
			int j = movingobjectposition.blockY;
			int k = movingobjectposition.blockZ;
			if (world.getTileEntity(i, j, k) instanceof TileEntityChest) {
				// notify player we have a chest
				player.addChatMessage(new ChatComponentText(
						StatCollector.translateToLocal("item.linkCard.message.stored") + i + " " + j + " " + k));
				itemstack.stackTagCompound = new NBTTagCompound();
				itemstack.stackTagCompound.setIntArray("storageLocation", new int[] { i, j, k });
			}
			if (world.getTileEntity(i, j, k) instanceof TileVendor) {
				TileVendor te = (TileVendor) world.getTileEntity(i, j, k);
				if (itemstack.hasTagCompound()) {
					int[] storageLocation = itemstack.stackTagCompound.getIntArray("storageLocation");
					player.addChatMessage(
							new ChatComponentText(StatCollector.translateToLocal("item.linkCard.message.set")));
					te.setRemoteStorage(storageLocation);
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}
			}
		}
		return true;
	}
}
