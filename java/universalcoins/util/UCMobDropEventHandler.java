package universalcoins.util;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.UniversalCoins;

public class UCMobDropEventHandler {

	Random random = new Random();

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin, UniversalCoins.proxy.itemSmallCoinStack,
				UniversalCoins.proxy.itemLargeCoinStack, UniversalCoins.proxy.itemSmallCoinBag,
				UniversalCoins.proxy.itemLargeCoinBag };
		if (event.source.getEntity() != null && event.source.getEntity().toString().contains("EntityPlayerMP")) {
			int chance;
			if (UniversalCoins.mobDropChance <= 0) {
				chance = 0;
			} else {
				chance = random.nextInt(UniversalCoins.mobDropChance);
			}
			int randomDropValue = random.nextInt(UniversalCoins.mobDropMax) + 1;

			// get mob max health and adjust drop value
			float health = ((EntityLivingBase) event.entity).getMaxHealth();
			if (health == 0)
				health = 10;
			int dropped = (int) (randomDropValue * health / 20 * (event.lootingLevel + 1));

			// multiply drop if ender dragon
			if (event.entity instanceof EntityDragon) {
				dropped = dropped * UniversalCoins.enderDragonMultiplier;
				while (dropped > 0) {
					int logVal = Math.min((int) (Math.log(dropped) / Math.log(9)), 4);
					int stackSize = Math.min((int) (dropped / Math.pow(9, logVal)), 64);
					ItemStack test = new ItemStack(coins[logVal], stackSize);
					event.entity.entityDropItem(test, 0.0F);
					dropped -= Math.pow(9, logVal) * stackSize;
				}
			}

			// drop coins
			if ((event.entity instanceof EntityMob || event.entity instanceof EntityWither)
					&& !event.entity.worldObj.isRemote && chance == 0) {
				while (dropped > 0) {
					int logVal = Math.min((int) (Math.log(dropped) / Math.log(9)), 4);
					int stackSize = Math.min((int) (dropped / Math.pow(9, logVal)), 64);
					ItemStack test = new ItemStack(coins[logVal], stackSize);
					event.entity.entityDropItem(test, 0.0F);
					dropped -= Math.pow(9, logVal) * stackSize;
				}
			}
		}
	}
}
