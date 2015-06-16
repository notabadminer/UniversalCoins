package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
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
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {		
		if( stack.getTagCompound() != null ) {
			list.add(stack.getTagCompound().getString("Name"));
			list.add(stack.getTagCompound().getString("Account"));
		} else {
			list.add(StatCollector.translateToLocal("item.itemUCCard.warning"));
		}
	}
	
	@Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return true;
		if( itemstack.getTagCompound() == null ) {
			createNBT(itemstack, world, player);
		}
		int accountCoins = UniversalAccounts.getInstance().getAccountBalance(world, itemstack.getTagCompound().getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
					"item.itemUCCard.balance") + " " + formatter.format(accountCoins)));
        return true;
    }
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer entityPlayer) {
		createNBT(stack, world, entityPlayer);
	}
	
	private void createNBT(ItemStack stack, World world, EntityPlayer entityPlayer) {
		String accountNumber = UniversalAccounts.getInstance().getOrCreatePlayerAccount(world, entityPlayer.getPersistentID().toString());
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("Name", entityPlayer.getName());
		stack.getTagCompound().setString("Owner", entityPlayer.getPersistentID().toString());
		stack.getTagCompound().setString("Account", accountNumber);
	}
}
