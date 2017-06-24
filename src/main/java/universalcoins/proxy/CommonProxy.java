package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.blocks.BlockATM;
import universalcoins.blocks.BlockPackager;
import universalcoins.blocks.BlockPowerReceiver;
import universalcoins.blocks.BlockPowerTransmitter;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockSignal;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockUCStandingSign;
import universalcoins.blocks.BlockUCWallSign;
import universalcoins.blocks.BlockVendor;
import universalcoins.blocks.BlockVendorFrame;
import universalcoins.items.ItemCatalog;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemFifthCoin;
import universalcoins.items.ItemFourthCoin;
import universalcoins.items.ItemLinkCard;
import universalcoins.items.ItemPackage;
import universalcoins.items.ItemSecondCoin;
import universalcoins.items.ItemThirdCoin;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemUCSign;
import universalcoins.items.ItemVendorWrench;
import universalcoins.tileentity.TileUCSign;

public class CommonProxy {
	public static Item iron_coin = new ItemCoin().setUnlocalizedName("iron_coin");
	public static Item gold_coin = new ItemSecondCoin().setUnlocalizedName("gold_coin");
	public static Item emerald_coin = new ItemThirdCoin().setUnlocalizedName("emerald_coin");
	public static Item diamond_coin = new ItemFourthCoin().setUnlocalizedName("diamond_coin");
	public static Item obsidian_coin = new ItemFifthCoin().setUnlocalizedName("obsidian_coin");
	public static Item uc_card = new ItemUCCard().setUnlocalizedName("uc_card");
	public static Item uc_package = new ItemPackage().setUnlocalizedName("uc_package");
	public static Item ender_card = new ItemEnderCard().setUnlocalizedName("ender_card");
	public static Item link_card = new ItemLinkCard().setUnlocalizedName("link_card");
	public static Item uc_sign = new ItemUCSign().setUnlocalizedName("uc_sign");
	public static Item vendor_wrench = new ItemVendorWrench().setUnlocalizedName("vendor_wrench");
	public static Item catalog = new ItemCatalog().setUnlocalizedName("catalog");
	public static Block tradestation = new BlockTradeStation().setUnlocalizedName("tradestation");
	public static Block safe = new BlockSafe().setUnlocalizedName("safe");
	public static Block signalblock = new BlockSignal().setUnlocalizedName("signalblock");
	public static Block vendor = new BlockVendor().setUnlocalizedName("vendor");
	public static Block vendor_frame = new BlockVendorFrame().setUnlocalizedName("vendor_frame");
	public static Block packager = new BlockPackager().setUnlocalizedName("packager");
	public static Block standing_ucsign = new BlockUCStandingSign(TileUCSign.class)
			.setUnlocalizedName("standing_ucsign");
	public static Block wall_ucsign = new BlockUCWallSign(TileUCSign.class).setUnlocalizedName("wall_ucsign");
	public static Block power_transmitter = new BlockPowerTransmitter().setUnlocalizedName("power_transmitter");
	public static Block power_receiver = new BlockPowerReceiver().setUnlocalizedName("power_receiver");
	public static Block atm = new BlockATM().setUnlocalizedName("atm");

	public void registerBlocks() {
		GameRegistry.register(tradestation, new ResourceLocation("universalcoins:tradestation"));
		GameRegistry.register(safe, new ResourceLocation("universalcoins:safe"));
		GameRegistry.register(signalblock, new ResourceLocation("universalcoins:signalblock"));
		GameRegistry.register(vendor, new ResourceLocation("universalcoins:vendor"));
		GameRegistry.register(vendor_frame, new ResourceLocation("universalcoins:vendor_frame"));
		GameRegistry.register(packager, new ResourceLocation("universalcoins:packager"));
		GameRegistry.register(standing_ucsign, new ResourceLocation("universalcoins:standing_ucsign"));
		GameRegistry.register(wall_ucsign, new ResourceLocation("universalcoins:wall_ucsign"));
		GameRegistry.register(power_transmitter, new ResourceLocation("universalcoins:power_transmitter"));
		GameRegistry.register(power_receiver, new ResourceLocation("universalcoins:power_receiver"));
		GameRegistry.register(atm, new ResourceLocation("universalcoins:atm"));
	}

	public void registerItems() {
		GameRegistry.register(iron_coin, new ResourceLocation("universalcoins:iron_coin"));
		GameRegistry.register(gold_coin, new ResourceLocation("universalcoins:gold_coin"));
		GameRegistry.register(emerald_coin, new ResourceLocation("universalcoins:emerald_coin"));
		GameRegistry.register(diamond_coin, new ResourceLocation("universalcoins:diamond_coin"));
		GameRegistry.register(obsidian_coin, new ResourceLocation("universalcoins:obsidian_coin"));
		GameRegistry.register(uc_card, new ResourceLocation("universalcoins:uc_card"));
		GameRegistry.register(ender_card, new ResourceLocation("universalcoins:ender_card"));
		GameRegistry.register(link_card, new ResourceLocation("universalcoins:link_card"));
		GameRegistry.register(uc_package, new ResourceLocation("universalcoins:uc_package"));
		GameRegistry.register(uc_sign, new ResourceLocation("universalcoins:uc_sign"));
		GameRegistry.register(vendor_wrench, new ResourceLocation("universalcoins:vendor_wrench"));
		GameRegistry.register(catalog, new ResourceLocation("universalcoins:catalog"));
	}

	public void registerRenderers() {
		// blank since we don't do anything on the server
	}

}
