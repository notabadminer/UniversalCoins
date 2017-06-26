package universalcoins.util;

import net.minecraft.item.ItemStack;
import universalcoins.UniversalCoins;

public class CoinUtils {
	
	public static int getCoinValue(ItemStack stack) {
		int coinValue = 0;
		switch (stack.getUnlocalizedName()) {
		case "item.universalcoins.iron_coin":
			coinValue = UniversalCoins.coinValues[0];
			break;
		case "item.universalcoins.gold_coin":
			coinValue = UniversalCoins.coinValues[1];
			break;
		case "item.universalcoins.emerald_coin":
			coinValue = UniversalCoins.coinValues[2];
			break;
		case "item.universalcoins.diamond_coin":
			coinValue = UniversalCoins.coinValues[3];
			break;
		case "item.universalcoins.obsidian_coin":
			coinValue = UniversalCoins.coinValues[4];
			break;
		}
		return coinValue;
	}

}
