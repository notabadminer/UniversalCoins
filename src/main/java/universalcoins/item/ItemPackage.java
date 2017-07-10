package universalcoins.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemPackage extends Item {

	public ItemPackage() {
		super();
		this.maxStackSize = 1;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		NBTTagList tagList = stack.getTagCompound().getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			int itemCount = new ItemStack(tag).getCount();
			String itemName = new ItemStack(tag).getDisplayName();
			if (itemCount > 0) {
				tooltip.add(itemCount + " " + itemName);
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.getActiveItemStack().getTagCompound() != null) {
			NBTTagList tagList = player.getActiveItemStack().getTagCompound().getTagList("Inventory",
					Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
				ItemStack stack = new ItemStack(tag);
				if (stack != ItemStack.EMPTY) {
					if (player.inventory.getFirstEmptyStack() != -1) {
						player.inventory.addItemStackToInventory(stack);
					} else {
						// spawn in world
						EntityItem entityItem = new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(),
								stack);
						worldIn.spawnEntity(entityItem);
					}
				}
			}
			// destroy package
			player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
		}
		return EnumActionResult.SUCCESS;
	}
}
