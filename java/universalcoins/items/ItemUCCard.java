package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
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

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister
				.registerIcon(UniversalCoins.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.stackTagCompound != null) {
			list.add(stack.stackTagCompound.getString("Name"));
			list.add(stack.stackTagCompound.getString("Account"));
		} else {
			list.add(StatCollector.translateToLocal("item.card.warning"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
			float px, float py, float pz) {
		if (world.isRemote)
			return true;
		if (itemstack.stackTagCompound == null) {
			createNBT(itemstack, world, player);
		}
		long accountCoins = UniversalAccounts.getInstance()
				.getAccountBalance(itemstack.stackTagCompound.getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
		player.addChatMessage(new ChatComponentText(
				StatCollector.translateToLocal("item.card.balance") + " " + formatter.format(accountCoins)));
		return true;
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer entityPlayer) {
		if (world.isRemote)
			return;
		createNBT(stack, world, entityPlayer);
	}

	protected void createNBT(ItemStack stack, World world, EntityPlayer entityPlayer) {
		String accountNumber = UniversalAccounts.getInstance()
				.getOrCreatePlayerAccount(entityPlayer.getPersistentID().toString());
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setString("Name", entityPlayer.getDisplayName());
		stack.stackTagCompound.setString("Owner", entityPlayer.getPersistentID().toString());
		stack.stackTagCompound.setString("Account", accountNumber);
	}
}
