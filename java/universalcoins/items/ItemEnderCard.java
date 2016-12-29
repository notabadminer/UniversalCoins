package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class ItemEnderCard extends ItemUCCard {

	public ItemEnderCard() {
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
			return false;
		if (itemstack.getTagCompound() == null) {
			createNBT(itemstack, world, player);
		}
		long accountBalance = UniversalAccounts.getInstance()
				.getAccountBalance(itemstack.getTagCompound().getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		ItemStack[] inventory = player.inventory.mainInventory;
		String accountNumber = itemstack.getTagCompound().getString("Account");
		int coinValue = 0;
		int depositAmount = 0;
		int coinsDeposited = 0;
		for (int i = 0; i < inventory.length; i++) {
			ItemStack instack = player.inventory.getStackInSlot(i);
			if (instack != null) {
				switch (instack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinValue = UniversalCoins.coinValues[0];
					break;
				case "item.gold_coin":
					coinValue = UniversalCoins.coinValues[1];
					break;
				case "item.emerald_coin":
					coinValue = UniversalCoins.coinValues[2];
					break;
				case "item.diamond_coin":
					coinValue = UniversalCoins.coinValues[3];
					break;
				case "item.obsidian_coin":
					coinValue = UniversalCoins.coinValues[4];
					break;
				default:
					coinValue = 0;
				}
				if (accountBalance == -1)
					// get out of here if the card is invalid
					return false;
				if (coinValue == 0)
					continue;
				if (Long.MAX_VALUE - accountBalance > coinValue * instack.stackSize) {
					depositAmount = instack.stackSize;
				} else {
					depositAmount = (int) (Long.MAX_VALUE - accountBalance) / coinValue;
				}
				UniversalAccounts.getInstance().creditAccount(accountNumber, coinValue * depositAmount);
				coinsDeposited += coinValue * depositAmount;
				inventory[i].stackSize -= depositAmount;
				if (inventory[i].stackSize == 0) {
					player.inventory.setInventorySlotContents(i, null);
					player.inventoryContainer.detectAndSendChanges();
				}
			}
		}
		if (coinsDeposited > 0) {
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.card.deposit")
					+ " " + formatter.format(coinsDeposited) + " " + StatCollector.translateToLocal(
							coinsDeposited > 1 ? "general.currency.multiple" : "general.currency.single")));
		}
		player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.card.balance") + " "
				+ formatter.format(UniversalAccounts.getInstance().getAccountBalance(accountNumber))));
		return false;
	}
}
