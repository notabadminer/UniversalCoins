package universalcoins.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			tooltip.add(I18n.translateToLocal("item.link_card.stored"));
		} else {
			tooltip.add(I18n.translateToLocal("item.link_card.blank"));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.FAIL;
		RayTraceResult movingobjectposition = this.rayTrace(worldIn, player, true);

		if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos cursorPos = movingobjectposition.getBlockPos();
			if (worldIn.getTileEntity(cursorPos) instanceof TileEntityChest) {
				// notify player we have a chest
				player.sendMessage(new TextComponentString(I18n.translateToLocal("item.link_card.message.stored")
						+ cursorPos.getX() + " " + cursorPos.getY() + " " + cursorPos.getZ()));
				player.getActiveItemStack().getTagCompound().setIntArray("storageLocation",
						new int[] { cursorPos.getX(), cursorPos.getY(), cursorPos.getZ() });
			}
			if (worldIn.getTileEntity(cursorPos) instanceof TileVendor) {
				TileVendor te = (TileVendor) worldIn.getTileEntity(cursorPos);
				if (player.getActiveItemStack().hasTagCompound()) {
					int[] storageLocation = player.getActiveItemStack().getTagCompound().getIntArray("storageLocation");
					player.sendMessage(new TextComponentString(I18n.translateToLocal("item.link_card.message.set")));
					te.setRemoteStorage(storageLocation);
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
}
