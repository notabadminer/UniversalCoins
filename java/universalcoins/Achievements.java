package universalcoins;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public class Achievements {

	public static void init() {
		AchievementPage page1 = new AchievementPage("Universal Coins", achCoin, achThousand, achMillion, achBillion,
				achMaxed);
		AchievementPage.registerAchievementPage(page1);
	}

	public static final Achievement achCoin = new Achievement("achievement.coin", "AchievementCoin", 0, 0,
			UniversalCoins.proxy.itemCoin, null);
	public static final Achievement achThousand = new Achievement("achievement.thousand", "AchievementThousand", 2, 0,
			UniversalCoins.proxy.itemSmallCoinStack, achCoin);
	public static final Achievement achMillion = new Achievement("achievement.million", "AchievementMillion", 4, 0,
			UniversalCoins.proxy.itemLargeCoinStack, achThousand);
	public static final Achievement achBillion = new Achievement("achievement.billion", "AchievementBillion", 6, 0,
			UniversalCoins.proxy.itemSmallCoinBag, achMillion);
	public static final Achievement achMaxed = new Achievement("achievement.maxed", "AchievementMaxed", 8, 0,
			UniversalCoins.proxy.itemLargeCoinBag, achBillion);

}
