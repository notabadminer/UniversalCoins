package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.Achievements;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockSignal;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockVendor;
import universalcoins.blocks.BlockVendorFrame;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemFifthCoin;
import universalcoins.items.ItemFourthCoin;
import universalcoins.items.ItemPackage;
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
	public static Item uc_package = new ItemPackage().setUnlocalizedName("uc_package");
	public static Item ender_card = new ItemEnderCard().setUnlocalizedName("ender_card");
	public static Block tradestation = new BlockTradeStation().setUnlocalizedName("tradestation");
	public static Block safe = new BlockSafe().setUnlocalizedName("safe");
	public static Block signalblock = new BlockSignal().setUnlocalizedName("signalblock");
	public static Block vendor = new BlockVendor().setUnlocalizedName("vendor");
	public static Block vendor_frame = new BlockVendorFrame().setUnlocalizedName("vendor_frame");

	public void registerBlocks() {
		//GameRegistry.register(tradestation, new ResourceLocation("universalcoins:tradestation"));
		GameRegistry.registerBlock(tradestation, tradestation.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(safe, safe.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(signalblock, signalblock.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vendor, vendor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vendor_frame, vendor_frame.getUnlocalizedName().substring(5));
	}

	public void registerItems() {
		GameRegistry.register(iron_coin, new ResourceLocation("universalcoins:iron_coin"));
		GameRegistry.register(gold_coin, new ResourceLocation("universalcoins:gold_coin"));
		GameRegistry.register(emerald_coin, new ResourceLocation("universalcoins:emerald_coin"));
		GameRegistry.register(diamond_coin, new ResourceLocation("universalcoins:diamond_coin"));
		GameRegistry.register(obsidian_coin, new ResourceLocation("universalcoins:obsidian_coin"));
		GameRegistry.register(uc_card, new ResourceLocation("universalcoins:uc_card"));
		GameRegistry.register(ender_card, new ResourceLocation("universalcoins:ender_card"));
		GameRegistry.register(uc_package, new ResourceLocation("universalcoins:uc_package"));
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
