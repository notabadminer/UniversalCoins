package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.Achievements;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemFifthCoin;
import universalcoins.items.ItemFourthCoin;
import universalcoins.items.ItemSecondCoin;
import universalcoins.items.ItemThirdCoin;
import universalcoins.items.ItemUCCard;

public class CommonProxy {
	public static Item iron_coin = new ItemCoin().setUnlocalizedName("iron_coin");
	public static Item gold_coin = new ItemSecondCoin().setUnlocalizedName("gold_coin");
	public static Item emerald_coin = new ItemThirdCoin().setUnlocalizedName("emerald_coin");
	public static Item diamond_coin = new ItemFourthCoin().setUnlocalizedName("diamond_coin");
	public static Item obsidian_coin = new ItemFifthCoin().setUnlocalizedName("obsidian_coin");
	public static Item uc_card = new ItemUCCard().setUnlocalizedName("uc_card");
	public static Item ender_card = new ItemEnderCard().setUnlocalizedName("ender_card");
	public static Block tradestation = new BlockTradeStation().setUnlocalizedName("tradestation");
	public static Block safe = new BlockSafe().setUnlocalizedName("safe");

	public void registerBlocks() {
		GameRegistry.registerBlock(tradestation, tradestation.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(safe, safe.getUnlocalizedName().substring(5));
	}

	public void registerItems() {
		GameRegistry.registerItem(iron_coin, iron_coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(gold_coin, gold_coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(emerald_coin, emerald_coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(diamond_coin, diamond_coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(obsidian_coin, obsidian_coin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(uc_card, uc_card.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(ender_card, ender_card.getUnlocalizedName().substring(5));
	}

	public void registerAchievements() {
		Achievements.init();
		Achievements.achCoin.registerStat();
		Achievements.achThousand.registerStat();
		Achievements.achMillion.registerStat();
		Achievements.achBillion.registerStat();
		Achievements.achMaxed.registerStat();
	}

	public void registerRenderers() {
		// blank since we don't do anything on the server
	}

}
