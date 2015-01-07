package universalcoins.items;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.util.UCWorldData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemUCCard extends Item {
	
	public ItemUCCard() {
		super();
		this.maxStackSize = 1;
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){
		this.itemIcon = par1IconRegister.registerIcon(UniversalCoins.modid + ":" + this.getUnlocalizedName().substring(5));
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {		
		if( stack.stackTagCompound != null ) {
			list.add("Owner: " + stack.stackTagCompound.getString("Owner"));
			list.add("Account: " + stack.stackTagCompound.getString("Account"));
		}	
	}
	
	@Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz){
		if (world.isRemote) return true;
		int accountCoins = getAccountBalance(world, itemstack.stackTagCompound.getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal(
					"item.itemUCCard.balance") + " " + formatter.format(accountCoins)));
        return true;
    }
	
	private int getAccountBalance(World world, String accountNumber) {
		return getWorldInt(world, accountNumber);
	}

	private int getWorldInt(World world, String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getInteger(tag);
	}
}
