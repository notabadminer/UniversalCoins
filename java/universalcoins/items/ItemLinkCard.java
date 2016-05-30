package universalcoins.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileVendor;

public class ItemLinkCard extends Item {

	public ItemLinkCard() {
		super();
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		this.maxStackSize = 1;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.hasTagCompound()) {
			list.add(I18n.translateToLocal("item.link_card.stored"));
		} else {
			list.add(I18n.translateToLocal("item.link_card.blank"));
		}
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote)
			return EnumActionResult.FAIL;
		RayTraceResult movingobjectposition = this.rayTrace(world, player, true);

		if (movingobjectposition != null
				&& movingobjectposition.typeOfHit ==  RayTraceResult.Type.BLOCK) {
			BlockPos cursorPos = movingobjectposition.getBlockPos();
			if (world.getTileEntity(cursorPos) instanceof TileEntityChest) {
				// notify player we have a chest
				player.addChatMessage(
						new TextComponentString(I18n.translateToLocal("item.link_card.message.stored")
								+ cursorPos.getX() + " " + cursorPos.getY() + " " + cursorPos.getZ()));
				stack.getTagCompound().setIntArray("storageLocation",
						new int[] { cursorPos.getX(), cursorPos.getY(), cursorPos.getZ() });
			}
			if (world.getTileEntity(cursorPos) instanceof TileVendor) {
				TileVendor te = (TileVendor) world.getTileEntity(cursorPos);
				if (stack.hasTagCompound()) {
					int[] storageLocation = stack.getTagCompound().getIntArray("storageLocation");
					player.addChatMessage(
							new TextComponentString(I18n.translateToLocal("item.link_card.message.set")));
					te.setRemoteStorage(storageLocation);
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
}
