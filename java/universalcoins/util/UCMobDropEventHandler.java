package universalcoins.util;

import java.util.Random;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.UniversalCoins;

public class UCMobDropEventHandler {

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		if (event.source.getEntity() != null && event.source.getEntity().toString().contains("EntityPlayerMP")) {
			Random random = new Random();
			int chance;
			if (UniversalCoins.mobDropChance <= 0) {
				chance = 0;
			} else {
				chance = random.nextInt(UniversalCoins.mobDropChance);
			}
			int dropped = random.nextInt(UniversalCoins.mobDropMax) + 1;

			// endermen drop small coin stacks instead of coins
			if ((event.entity instanceof EntityEnderman) && !event.entity.worldObj.isRemote) {
				event.entityLiving
						.entityDropItem(new ItemStack(UniversalCoins.proxy.itemSmallCoinStack, dropped), 0.0F);
				// all other mobs drop coins
			} else if ((event.entity instanceof EntityMob) && !event.entity.worldObj.isRemote && chance == 0) {
				event.entityLiving.entityDropItem(new ItemStack(UniversalCoins.proxy.itemCoin, dropped), 0.0F);
			}
		}
	}
}
