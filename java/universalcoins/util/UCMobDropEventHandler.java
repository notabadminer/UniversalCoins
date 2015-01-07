package universalcoins.util;

import java.util.Random;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import universalcoins.UniversalCoins;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class UCMobDropEventHandler {
	
	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		if (event.source.getDamageType().equals("player")) {
			Random random = new Random();
			int chance;
			if (UniversalCoins.mobDropChance <= 0) {
				chance = 0;
			} else {
				chance = random.nextInt(UniversalCoins.mobDropChance);
			}
			int dropped = random.nextInt(UniversalCoins.mobDropMax) + 1;

			if ((event.entity instanceof EntityZombie || event.entity instanceof EntitySkeleton)
					&& !event.entity.worldObj.isRemote && chance == 0) {
				event.entityLiving.entityDropItem(new ItemStack(
						UniversalCoins.proxy.itemCoin, dropped), 0.0F);
			}

			// endermen drop small coin stacks instead of coins
			if ((event.entity instanceof EntityEnderman)
					&& !event.entity.worldObj.isRemote) {
				event.entityLiving.entityDropItem(new ItemStack(
						UniversalCoins.proxy.itemSmallCoinStack, dropped), 0.0F);
			}
		}
	}
}
