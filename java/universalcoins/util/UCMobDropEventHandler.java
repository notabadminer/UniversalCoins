package universalcoins.util;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import universalcoins.UniversalCoins;

public class UCMobDropEventHandler {

	Random random = new Random();

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		if (event.source.getEntity() != null && event.source.getEntity().toString().contains("EntityPlayerMP")) {
			int chance = UniversalCoins.mobDropChance == 0 ? 0 : random.nextInt(UniversalCoins.mobDropChance);
			int randomDropValue = random.nextInt(UniversalCoins.mobDropMax) + 1;

			// get mob max health and adjust drop value
			float health = ((EntityLivingBase) event.entity).getMaxHealth();
			if (health == 0)
				health = 10;
			int dropped = (int) (randomDropValue * health / 20 * (event.lootingLevel + 1));

			// multiply drop if ender dragon
			if (event.entity instanceof EntityDragon) {
				dropped = dropped * UniversalCoins.enderDragonMultiplier;
			}

			// drop coins
			if ((event.entity instanceof EntityMob || event.entity instanceof EntityWither
					|| event.entity instanceof EntityDragon) && !event.entity.worldObj.isRemote && chance == 0) {
				while (dropped > 0) {
					ItemStack stack = null;
					if (dropped > UniversalCoins.coinValues[4]) {
						stack = new ItemStack(UniversalCoins.proxy.obsidian_coin, 1);
						stack.stackSize = (int) Math.floor(dropped / UniversalCoins.coinValues[4]);
						dropped -= stack.stackSize * UniversalCoins.coinValues[4];
					} else if (dropped > UniversalCoins.coinValues[3]) {
						stack = new ItemStack(UniversalCoins.proxy.diamond_coin, 1);
						stack.stackSize = (int) Math.floor(dropped / UniversalCoins.coinValues[3]);
						dropped -= stack.stackSize * UniversalCoins.coinValues[3];
					} else if (dropped > UniversalCoins.coinValues[2]) {
						stack = new ItemStack(UniversalCoins.proxy.emerald_coin, 1);
						stack.stackSize = (int) Math.floor(dropped / UniversalCoins.coinValues[2]);
						dropped -= stack.stackSize * UniversalCoins.coinValues[2];
					} else if (dropped > UniversalCoins.coinValues[1]) {
						stack = new ItemStack(UniversalCoins.proxy.gold_coin, 1);
						stack.stackSize = (int) Math.floor(dropped / UniversalCoins.coinValues[1]);
						dropped -= stack.stackSize * UniversalCoins.coinValues[1];
					} else if (dropped > UniversalCoins.coinValues[0]) {
						stack = new ItemStack(UniversalCoins.proxy.iron_coin, 1);
						stack.stackSize = (int) Math.floor(dropped / UniversalCoins.coinValues[0]);
						dropped -= stack.stackSize * UniversalCoins.coinValues[0];
					}

					if (stack == null)
						return;

					World world = event.entity.worldObj;
					Random rand = new Random();
					float rx = rand.nextFloat() * 0.8F + 0.1F;
					float ry = rand.nextFloat() * 0.8F + 0.1F;
					float rz = rand.nextFloat() * 0.8F + 0.1F;
					EntityItem entityItem = new EntityItem(world, event.entity.posX + rx, event.entity.posY + ry,
							event.entity.posZ + rz, stack);
					world.spawnEntityInWorld(entityItem);
				}
			}
		}
	}
}
