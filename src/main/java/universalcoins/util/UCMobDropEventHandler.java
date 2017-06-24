package universalcoins.util;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import universalcoins.UniversalCoins;

public class UCMobDropEventHandler {

	Random random = new Random();

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		if (event.getSource().getTrueSource() != null
				&& event.getSource().getTrueSource().toString().contains("EntityPlayerMP")) {
			int chance = UniversalCoins.mobDropChance == 0 ? 0 : random.nextInt(UniversalCoins.mobDropChance);
			int randomDropValue = random.nextInt(UniversalCoins.mobDropMax) + 1;

			// get mob max health and adjust drop value
			float health = ((EntityLivingBase) event.getEntity()).getMaxHealth();
			if (health == 0)
				health = 10;
			int dropped = (int) (randomDropValue * health / 20 * (event.getLootingLevel() + 1));

			// multiply drop if ender dragon
			if (event.getEntity() instanceof EntityDragon) {
				dropped = dropped * UniversalCoins.enderDragonMultiplier;
			}

			// drop coins
			if ((event.getEntity() instanceof EntityMob || event.getEntity() instanceof EntityWither
					|| event.getEntity() instanceof EntityDragon) && !event.getEntity().world.isRemote
					&& chance == 0) {
				while (dropped > 0) {
					ItemStack stack = null;
					if (dropped > UniversalCoins.coinValues[4]) {
						stack = new ItemStack(UniversalCoins.Items.obsidian_coin, 1);
						stack.setCount((int) Math.floor(dropped / UniversalCoins.coinValues[4]));
						dropped -= stack.getCount() * UniversalCoins.coinValues[4];
					} else if (dropped > UniversalCoins.coinValues[3]) {
						stack = new ItemStack(UniversalCoins.Items.diamond_coin, 1);
						stack.setCount((int) Math.floor(dropped / UniversalCoins.coinValues[3]));
						dropped -= stack.getCount() * UniversalCoins.coinValues[3];
					} else if (dropped > UniversalCoins.coinValues[2]) {
						stack = new ItemStack(UniversalCoins.Items.emerald_coin, 1);
						stack.setCount((int) Math.floor(dropped / UniversalCoins.coinValues[2]));
						dropped -= stack.getCount() * UniversalCoins.coinValues[2];
					} else if (dropped > UniversalCoins.coinValues[1]) {
						stack = new ItemStack(UniversalCoins.Items.gold_coin, 1);
						stack.setCount((int) Math.floor(dropped / UniversalCoins.coinValues[1]));
						dropped -= stack.getCount() * UniversalCoins.coinValues[1];
					} else if (dropped > UniversalCoins.coinValues[0]) {
						stack = new ItemStack(UniversalCoins.Items.iron_coin, 1);
						stack.setCount((int) Math.floor(dropped / UniversalCoins.coinValues[0]));
						dropped -= stack.getCount() * UniversalCoins.coinValues[0];
					}

					if (stack == null)
						return;

					World world = event.getEntity().world;
					Random rand = new Random();
					float rx = rand.nextFloat() * 0.8F + 0.1F;
					float ry = rand.nextFloat() * 0.8F + 0.1F;
					float rz = rand.nextFloat() * 0.8F + 0.1F;
					EntityItem entityItem = new EntityItem(world, (event.getEntity()).posX + rx,
							(event.getEntity()).posY + ry, (event.getEntity()).posZ + rz, stack);
					world.spawnEntity(entityItem);
				}
			}
		}
	}
}
