package universalcoins.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
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

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.hasTagCompound()) {
			list.add(StatCollector.translateToLocal("item.linkCard.stored"));
		} else {
			list.add(StatCollector.translateToLocal("item.linkCard.blank"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (world.isRemote)
			return true;
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

		if (movingobjectposition != null
				&& movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			BlockPos cursorPos = movingobjectposition.getBlockPos();
			if (world.getTileEntity(cursorPos) instanceof TileEntityChest) {
				// notify player we have a chest
				player.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("item.linkCard.message.stored")
								+ cursorPos.getX() + " " + cursorPos.getY() + " " + cursorPos.getZ()));
				if (!itemstack.hasTagCompound())
					itemstack.setTagCompound(new NBTTagCompound());
				itemstack.getTagCompound().setIntArray("storageLocation",
						new int[] { cursorPos.getX(), cursorPos.getY(), cursorPos.getZ() });
			}
			if (world.getTileEntity(cursorPos) instanceof TileVendor) {
				TileVendor te = (TileVendor) world.getTileEntity(cursorPos);
				if (itemstack.hasTagCompound()) {
					int[] storageLocation = itemstack.getTagCompound().getIntArray("storageLocation");
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
