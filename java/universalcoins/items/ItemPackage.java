package universalcoins.items;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemPackage extends Item {

	public ItemPackage() {
		super();
		this.maxStackSize = 1;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		NBTTagList tagList = stack.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			// byte slot = tag.getByte("Slot");
			int itemCount = ItemStack.loadItemStackFromNBT(tag).stackSize;
			String itemName = ItemStack.loadItemStackFromNBT(tag).getDisplayName();
			list.add(itemCount + " " + itemName);
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (itemstack.getTagCompound() != null) {
			// TODO unpack items and destroy package
			NBTTagList tagList = itemstack.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
				if (player.inventory.getFirstEmptyStack() != -1) {
					player.inventory.addItemStackToInventory(ItemStack.loadItemStackFromNBT(tag));
				} else {
					// spawn in world
					EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(),
							ItemStack.loadItemStackFromNBT(tag));
					world.spawnEntityInWorld(entityItem);
				}
			}
			// destroy package
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		}

		return false;
	}

}
