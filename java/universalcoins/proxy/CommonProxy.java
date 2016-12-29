package universalcoins.proxy;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import universalcoins.Achievements;
import universalcoins.blocks.BlockATM;
import universalcoins.blocks.BlockPackager;
import universalcoins.blocks.BlockPowerReceiver;
import universalcoins.blocks.BlockPowerTransmitter;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockSignal;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockUCSign;
import universalcoins.blocks.BlockVendor;
import universalcoins.blocks.BlockVendorFrame;
import universalcoins.items.ItemBlockVendor;
import universalcoins.items.ItemCatalog;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemDiamondCoin;
import universalcoins.items.ItemEmeraldCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemGoldCoin;
import universalcoins.items.ItemLinkCard;
import universalcoins.items.ItemObsidianCoin;
import universalcoins.items.ItemPackage;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemUCSign;
import universalcoins.items.ItemVendorWrench;
import universalcoins.tile.TileUCSign;
import universalcoins.util.Vending;

public class CommonProxy {
	public static Block atm = new BlockATM().setBlockName("atm");
	public static Block packager = new BlockPackager().setBlockName("packager");
	public static Block power_receiver = new BlockPowerReceiver().setBlockName("power_receiver");
	public static Block power_transmitter = new BlockPowerTransmitter().setBlockName("power_transmitter");
	public static Block safe = new BlockSafe().setBlockName("safe");
	public static Block signal_block = new BlockSignal().setBlockName("signal_block");
	public static Block standing_ucsign = new BlockUCSign(TileUCSign.class, true).setBlockName("standing_ucsign");
	public static Block trade_station = new BlockTradeStation().setBlockName("trade_station");
	public static Block vendor = new BlockVendor(Vending.supports).setBlockName("vendor");
	public static Block vendor_frame = new BlockVendorFrame().setBlockName("vendor_frame");
	public static Block wall_ucsign = new BlockUCSign(TileUCSign.class, false).setBlockName("wall_ucsign");
	public static Item catalog = new ItemCatalog().setUnlocalizedName("catalog");
	public static Item diamond_coin = new ItemDiamondCoin().setUnlocalizedName("diamond_coin");
	public static Item emerald_coin = new ItemEmeraldCoin().setUnlocalizedName("emerald_coin");
	public static Item ender_card = new ItemEnderCard().setUnlocalizedName("ender_card");
	public static Item gold_coin = new ItemGoldCoin().setUnlocalizedName("gold_coin");
	public static Item iron_coin = new ItemCoin().setUnlocalizedName("iron_coin");
	public static Item link_card = new ItemLinkCard().setUnlocalizedName("link_card");
	public static Item obsidian_coin = new ItemObsidianCoin().setUnlocalizedName("obsidian_coin");
	public static Item uc_card = new ItemUCCard().setUnlocalizedName("uc_card");
	public static Item uc_package = new ItemPackage().setUnlocalizedName("uc_package");
	public static Item uc_sign = new ItemUCSign().setUnlocalizedName("uc_sign");
	public static Item vendor_wrench = new ItemVendorWrench().setUnlocalizedName("vendor_wrench");

	public void registerBlocks() {

		GameRegistry.registerBlock(atm, "atm").getUnlocalizedName();
		GameRegistry.registerBlock(packager, "packager").getUnlocalizedName();
		GameRegistry.registerBlock(power_receiver, "power_receiver").getUnlocalizedName();
		GameRegistry.registerBlock(power_transmitter, "power_transmitter").getUnlocalizedName();
		GameRegistry.registerBlock(safe, "safe").getUnlocalizedName();
		GameRegistry.registerBlock(signal_block, "signal_block").getUnlocalizedName();
		GameRegistry.registerBlock(standing_ucsign, "standing_ucsign").getUnlocalizedName();
		GameRegistry.registerBlock(trade_station, "trade_station").getUnlocalizedName();
		GameRegistry.registerBlock(vendor_frame, "vendor_frame").getUnlocalizedName();
		GameRegistry.registerBlock(vendor, ItemBlockVendor.class, "vendor");
		GameRegistry.registerBlock(wall_ucsign, "wall_ucsign").getUnlocalizedName();
	}

	public void registerItems() {

		GameRegistry.registerItem(catalog, catalog.getUnlocalizedName());
		GameRegistry.registerItem(diamond_coin, diamond_coin.getUnlocalizedName());
		GameRegistry.registerItem(emerald_coin, emerald_coin.getUnlocalizedName());
		GameRegistry.registerItem(ender_card, ender_card.getUnlocalizedName());
		GameRegistry.registerItem(gold_coin, gold_coin.getUnlocalizedName());
		GameRegistry.registerItem(iron_coin, iron_coin.getUnlocalizedName());
		GameRegistry.registerItem(link_card, link_card.getUnlocalizedName());
		GameRegistry.registerItem(obsidian_coin, obsidian_coin.getUnlocalizedName());
		GameRegistry.registerItem(uc_card, uc_card.getUnlocalizedName());
		GameRegistry.registerItem(uc_package, uc_package.getUnlocalizedName());
		GameRegistry.registerItem(uc_sign, uc_sign.getUnlocalizedName());
		GameRegistry.registerItem(vendor_wrench, vendor_wrench.getUnlocalizedName());

	}

	public void registerRenderers() {
		// blank since we don't do anything on the server
	}

	public void registerAchievements() {
		Achievements.init();
		Achievements.achCoin.registerStat();
		Achievements.achThousand.registerStat();
		Achievements.achMillion.registerStat();
		Achievements.achBillion.registerStat();
		Achievements.achMaxed.registerStat();
	}

}
