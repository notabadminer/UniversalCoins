package universalcoins.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;

public abstract class UCCommandBase extends CommandBase {
	
	protected void givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
		ItemStack stack = null;
		while (coinsLeft > 0) {
			if (coinsLeft > UniversalCoins.coinValues[4]) {
				stack = new ItemStack(UniversalCoins.Items.obsidian_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[4]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[4];
			} else if (coinsLeft > UniversalCoins.coinValues[3]) {
				stack = new ItemStack(UniversalCoins.Items.diamond_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[3]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[3];
			} else if (coinsLeft > UniversalCoins.coinValues[2]) {
				stack = new ItemStack(UniversalCoins.Items.emerald_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[2]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[2];
			} else if (coinsLeft > UniversalCoins.coinValues[1]) {
				stack = new ItemStack(UniversalCoins.Items.gold_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[1]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[1];
			} else if (coinsLeft > UniversalCoins.coinValues[0]) {
				stack = new ItemStack(UniversalCoins.Items.iron_coin, 1);
				stack.setCount((int) Math.floor(coinsLeft / UniversalCoins.coinValues[0]));
				coinsLeft -= stack.getCount() * UniversalCoins.coinValues[0];
			}

			if (stack == null)
				return;

			// add a stack to the recipients inventory
			if (recipient.inventory.getFirstEmptyStack() != -1) {
				recipient.inventory.addItemStackToInventory(stack);
			} else {
				for (int i = 0; i < recipient.inventory.getSizeInventory(); i++) {
					ItemStack istack = recipient.inventory.getStackInSlot(i);
					if (istack != null && istack.getItem() == stack.getItem()) {
						int amountToAdd = (int) Math.min(stack.getCount(),
								istack.getMaxStackSize() - istack.getCount());
						istack.setCount(istack.getCount() + amountToAdd);
						stack.setCount(stack.getCount() - amountToAdd);
					}
				}
				// at this point, we're going to throw extra to the world since
				// the player inventory must be full.
				World world = ((EntityPlayerMP) recipient).world;
				Random rand = new Random();
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;
				EntityItem entityItem = new EntityItem(world, ((EntityPlayerMP) recipient).posX + rx,
						((EntityPlayerMP) recipient).posY + ry, ((EntityPlayerMP) recipient).posZ + rz, stack);
				world.spawnEntity(entityItem);
			}
		}
	}

}
