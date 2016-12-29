package universalcoins.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

public class ItemUCSign extends ItemSign {

	public ItemUCSign() {
		this.maxStackSize = 16;
		this.setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.setTextureName("sign");
	}

	/**
	 * Callback for item usage. If the item does something special on right
	 * clicking, he will have one of those. Return True if something happen and
	 * false if it don't. This is for ITEMS, not BLOCKS
	 */
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int par4, int par5, int par6,
			int par7, float par8, float par9, float par10) {
		if (par7 == 0) {
			return false;
		} else if (!world.getBlock(par4, par5, par6).getMaterial().isSolid()) {
			return false;
		} else {
			if (par7 == 1) {
				++par5;
			}

			if (par7 == 2) {
				--par6;
			}

			if (par7 == 3) {
				++par6;
			}

			if (par7 == 4) {
				--par4;
			}

			if (par7 == 5) {
				++par4;
			}

			if (!player.canPlayerEdit(par4, par5, par6, par7, itemStack)) {
				return false;
			} else if (!Blocks.standing_sign.canPlaceBlockAt(world, par4, par5, par6)) {
				return false;
			}
			if (par7 == 1) {
				int i1 = MathHelper.floor_double((double) ((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
				world.setBlock(par4, par5, par6, UniversalCoins.proxy.standing_ucsign, i1, 3);
			} else {
				world.setBlock(par4, par5, par6, UniversalCoins.proxy.wall_ucsign, par7, 3);
			}

			--itemStack.stackSize;
			TileEntity te = world.getTileEntity(par4, par5, par6);
			if (te != null && te instanceof TileUCSign) {
				if (itemStack.hasTagCompound()) {
					NBTTagCompound tagCompound = itemStack.getTagCompound();
					((TileUCSign) te).blockIcon = tagCompound.getString("BlockIcon");
				}
			}
			((TileUCSign) te).blockOwner = player.getDisplayName();
			player.openGui(UniversalCoins.instance, 1, world, par4, par5, par6);
		}
		return true;
	}
}
