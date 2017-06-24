package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class ItemUCCard extends Item {

	public ItemUCCard() {
		super();
		this.maxStackSize = 1;
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.getTagCompound() != null) {
			tooltip.add(stack.getTagCompound().getString("Name"));
			tooltip.add(stack.getTagCompound().getString("Account"));
		} else {
			tooltip.add(I18n.translateToLocal("item.card.warning"));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.FAIL;
		if (player.getActiveItemStack().getTagCompound() == null) {
			createNBT(player.getActiveItemStack(), worldIn, player);
		}
		long accountCoins = UniversalAccounts.getInstance()
				.getAccountBalance(player.getActiveItemStack().getTagCompound().getString("Account"));
		DecimalFormat formatter = new DecimalFormat("###,###,###,###,###,###,###");
		player.sendMessage(new TextComponentString(
				I18n.translateToLocal("item.card.balance") + " " + formatter.format(accountCoins)));
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer entityPlayer) {
		if (!world.isRemote)
			createNBT(stack, world, entityPlayer);
	}

	protected void createNBT(ItemStack stack, World world, EntityPlayer entityPlayer) {
		String accountNumber = UniversalAccounts.getInstance()
				.getOrCreatePlayerAccount(entityPlayer.getPersistentID().toString());
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("Name", entityPlayer.getName());
		stack.getTagCompound().setString("Owner", entityPlayer.getPersistentID().toString());
		stack.getTagCompound().setString("Account", accountNumber);
	}
}
