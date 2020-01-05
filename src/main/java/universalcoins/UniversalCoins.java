package universalcoins;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.block.BlockATM;
import universalcoins.block.BlockPackager;
import universalcoins.block.BlockPowerReceiver;
import universalcoins.block.BlockPowerTransmitter;
import universalcoins.block.BlockSafe;
import universalcoins.block.BlockSignal;
import universalcoins.block.BlockTradeStation;
import universalcoins.block.BlockUCStandingSign;
import universalcoins.block.BlockUCWallSign;
import universalcoins.block.BlockVendor;
import universalcoins.block.BlockVendorFrame;
import universalcoins.command.UCBalance;
import universalcoins.command.UCCommand;
import universalcoins.command.UCGive;
import universalcoins.command.UCRebalance;
import universalcoins.command.UCSend;
import universalcoins.item.ItemCatalog;
import universalcoins.item.ItemCoin;
import universalcoins.item.ItemEnderCard;
import universalcoins.item.ItemFifthCoin;
import universalcoins.item.ItemFourthCoin;
import universalcoins.item.ItemLinkCard;
import universalcoins.item.ItemPackage;
import universalcoins.item.ItemSecondCoin;
import universalcoins.item.ItemThirdCoin;
import universalcoins.item.ItemUCCard;
import universalcoins.item.ItemUCSign;
import universalcoins.item.ItemVendorWrench;
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCPackagerServerMessage;
import universalcoins.net.UCSignServerMessage;
import universalcoins.net.UCTileSignMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.render.SignalRenderer;
import universalcoins.render.UCSignRenderer;
import universalcoins.render.VendorBlockRenderer;
import universalcoins.render.VendorFrameRenderer;
import universalcoins.tileentity.TileATM;
import universalcoins.tileentity.TilePackager;
import universalcoins.tileentity.TilePowerReceiver;
import universalcoins.tileentity.TilePowerTransmitter;
import universalcoins.tileentity.TileProtected;
import universalcoins.tileentity.TileSafe;
import universalcoins.tileentity.TileSignal;
import universalcoins.tileentity.TileTradeStation;
import universalcoins.tileentity.TileUCSign;
import universalcoins.tileentity.TileVendor;
import universalcoins.tileentity.TileVendorBlock;
import universalcoins.tileentity.TileVendorFrame;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UCPlayerPickupEventHandler;
import universalcoins.worldgen.VillageGenBank;
import universalcoins.worldgen.VillageGenShop;
import universalcoins.worldgen.VillageGenTrade;

/**
 * UniversalCoins, Sell all your extra blocks and buy more!!! Create a trading
 * economy, jobs, whatever.
 * 
 * @author notabadminer
 * 
 **/

@Mod(modid = UniversalCoins.MODID, name = UniversalCoins.NAME, version = UniversalCoins.VERSION, acceptedMinecraftVersions = "@MC_VERSION@", updateJSON = "https://raw.githubusercontent.com/notabadminer/UniversalCoins/master/version.json", dependencies = "required-after:forge@14.21.0.2368,)")

@Mod.EventBusSubscriber
public class UniversalCoins {
	@Instance("universalcoins")
	public static UniversalCoins instance;
	public static final String MODID = "universalcoins";
	public static final String NAME = "Universal Coins";
	public static final String VERSION = "@VERSION@";

	public static CreativeTabs tabUniversalCoins = new UCTab("tabUniversalCoins");

	public static int[] coinValues;
	public static Boolean blockProtection, autoModeEnabled, tradeStationBuyEnabled, mobsDropCoins, coinsInDungeon;
	public static Integer mobDropMax, mobDropWeight, enderDragonMultiplier, smallPackagePrice, medPackagePrice,
			largePackagePrice, rfWholesaleRate, rfRetailRate, bankGenWeight, shopGenWeight, tradeGenWeight,
			shopMinPrice, shopMaxPrice;
	public static Double itemSellRatio;
	public static SimpleNetworkWrapper snw;

	@ObjectHolder(UniversalCoins.MODID)
	public static class Blocks {
		public static final Block tradestation = null;
		public static final Block safe = null;
		public static final Block signalblock = null;
		public static final Block vendor_block = null;
		public static final Block vendor_frame = null;
		public static final Block packager = null;
		public static final Block standing_ucsign = null;
		public static final Block wall_ucsign = null;
		public static final Block power_transmitter = null;
		public static final Block power_receiver = null;
		public static final Block atm = null;
	};

	@ObjectHolder(UniversalCoins.MODID)
	public static class Items {
		public static final Item iron_coin = null;
		public static final Item gold_coin = null;
		public static final Item emerald_coin = null;
		public static final Item diamond_coin = null;
		public static final Item obsidian_coin = null;
		public static final Item uc_card = null;
		public static final Item uc_package = null;
		public static final Item ender_card = null;
		public static final Item link_card = null;
		public static final Item uc_sign = null;
		public static final Item vendor_wrench = null;
		public static final Item catalog = null;
		public static final Item tradestation = null;
		public static final Item safe = null;
		public static final Item signalblock = null;
		public static final Item vendor_block = null;
		public static final Item vendor_frame = null;
		public static final Item packager = null;
		public static final Item power_transmitter = null;
		public static final Item power_receiver = null;
		public static final Item atm = null;
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(new BlockATM().setRegistryName(MODID, "atm").setUnlocalizedName(MODID + ".atm"),
				new BlockPackager().setRegistryName(MODID, "packager").setUnlocalizedName(MODID + ".packager"),
				new BlockPowerReceiver().setRegistryName(MODID, "power_receiver")
						.setUnlocalizedName(MODID + ".power_receiver"),
				new BlockPowerTransmitter().setRegistryName(MODID, "power_transmitter")
						.setUnlocalizedName(MODID + ".power_transmitter"),
				new BlockSafe().setRegistryName(MODID, "safe").setUnlocalizedName(MODID + ".safe"),
				new BlockSignal().setRegistryName(MODID, "signalblock").setUnlocalizedName(MODID + ".signalblock"),
				new BlockTradeStation().setRegistryName(MODID, "tradestation")
						.setUnlocalizedName(MODID + ".tradestation"),
				new BlockVendor().setRegistryName(MODID, "vendor_block").setUnlocalizedName(MODID + ".vendor_block"),
				new BlockVendorFrame().setRegistryName(MODID, "vendor_frame")
						.setUnlocalizedName(MODID + ".vendor_frame"),
				new BlockUCWallSign(TileUCSign.class).setRegistryName(MODID, "wall_ucsign")
						.setUnlocalizedName(MODID + ".wall_ucsign"),
				new BlockUCStandingSign(TileUCSign.class).setRegistryName(MODID, "standing_ucsign")
						.setUnlocalizedName(MODID + ".standing_ucsign"));

		// register TileEntitys
		GameRegistry.registerTileEntity(TileProtected.class, MODID + ".protected");
		GameRegistry.registerTileEntity(TileTradeStation.class, MODID + ".tradestation");
		GameRegistry.registerTileEntity(TileSafe.class, MODID + ".safe");
		GameRegistry.registerTileEntity(TileSignal.class, MODID + ".signalblock");
		GameRegistry.registerTileEntity(TileVendor.class, MODID + ".vendor");
		GameRegistry.registerTileEntity(TileVendorBlock.class, MODID + ".vendor_block");
		GameRegistry.registerTileEntity(TileVendorFrame.class, MODID + ".vendor_frame");
		GameRegistry.registerTileEntity(TilePackager.class, MODID + ".packager");
		GameRegistry.registerTileEntity(TileUCSign.class, MODID + ".ucsign");
		GameRegistry.registerTileEntity(TilePowerTransmitter.class, MODID + ".power_transmitter");
		GameRegistry.registerTileEntity(TilePowerReceiver.class, MODID + ".power_receiver");
		GameRegistry.registerTileEntity(TileATM.class, MODID + ".atm");
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		// register Items
		event.getRegistry().registerAll(
				new ItemCoin().setRegistryName(MODID, "iron_coin").setUnlocalizedName(MODID + ".iron_coin"),
				new ItemSecondCoin().setRegistryName(MODID, "gold_coin").setUnlocalizedName(MODID + ".gold_coin"),
				new ItemThirdCoin().setRegistryName(MODID, "emerald_coin").setUnlocalizedName(MODID + ".emerald_coin"),
				new ItemFourthCoin().setRegistryName(MODID, "diamond_coin").setUnlocalizedName(MODID + ".diamond_coin"),
				new ItemFifthCoin().setRegistryName(MODID, "obsidian_coin")
						.setUnlocalizedName(MODID + ".obsidian_coin"),
				new ItemUCCard().setRegistryName(MODID, "uc_card").setUnlocalizedName(MODID + ".uc_card"),
				new ItemEnderCard().setRegistryName(MODID, "ender_card").setUnlocalizedName(MODID + ".ender_card"),
				new ItemLinkCard().setRegistryName(MODID, "link_card").setUnlocalizedName(MODID + ".link_card"),
				new ItemCatalog().setRegistryName(MODID, "catalog").setUnlocalizedName(MODID + ".catalog"),
				new ItemPackage().setRegistryName(MODID, "uc_package").setUnlocalizedName(MODID + ".uc_package"),
				new ItemUCSign().setRegistryName(MODID, "uc_sign").setUnlocalizedName(MODID + ".uc_sign"),
				new ItemVendorWrench().setRegistryName(MODID, "vendor_wrench")
						.setUnlocalizedName(MODID + ".vendor_wrench"));

		// register ItemBlocks
		Block[] blocks = { Blocks.atm, Blocks.packager, Blocks.power_receiver, Blocks.power_transmitter, Blocks.safe,
				Blocks.signalblock, Blocks.tradestation, Blocks.vendor_block, Blocks.vendor_frame };
		for (Block block : blocks) {
			event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName())
					.setUnlocalizedName(block.getUnlocalizedName()));
		}
	}

	@SubscribeEvent
	@SideOnly(CLIENT)
	public static void registerModels(ModelRegistryEvent event) throws Exception {
		Field[] fields = Items.class.getFields();
		for (Field fid : fields) {
			Item item = (Item) fid.get(null);
			ModelLoader.setCustomModelResourceLocation(item, 0,
					new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
		ClientRegistry.bindTileEntitySpecialRenderer(TileSignal.class, new SignalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorBlock.class, new VendorBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileVendorFrame.class, new VendorFrameRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileUCSign.class, new UCSignRenderer());

	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		// block protection
		Property blockProtect = config.get("Protection", "Integrated block protection", true,
				"Set to false to disable block protection.");
		blockProtection = blockProtect.getBoolean(true);

		// coin values
		Property coinProperty = config.get("coins", "Coin Values", new int[] { 1, 10, 100, 1000, 10000 },
				"Set coin values. First value is not configureable and will be set to 1");
		coinValues = coinProperty.getIntList();
		coinValues[0] = 1; // Override any user set values.

		// loot
		Property mobDrops = config.get("Loot", "Mob Drops", true,
				"Set to false to disable mobs dropping coins on death.");
		mobsDropCoins = mobDrops.getBoolean(true);
		Property dropAmount = config.get("Loot", "Mob Drop Max", 20,
				"Max mob drop stacksize. Minimum 1. Maximum 64. Default 20.");
		mobDropMax = Math.max(1, Math.min(dropAmount.getInt(20), 64));
		Property dropWeight = config.get("Loot", "Mob Drop Weight", 1,
				"Chance of a mob dropping coins. Higher number means higher chance.");
		mobDropWeight = Math.max(0, Math.min(dropWeight.getInt(1), 100));
		Property dragonMultiplier = config.get("Loot", "Ender Dragon Multiplier", 1000,
				"Obsidian coin stacks dropped for ender dragon kills. Minimum 1. Default 10. Max 1000");
		enderDragonMultiplier = Math.max(1, Math.min(dragonMultiplier.getInt(10), 1000));
		Property dungeonCoins = config.get("Loot", "Dungeon Coins", true,
				"Set to false to disable coins spawning in dungeon chests.");
		coinsInDungeon = dungeonCoins.getBoolean(true);

		// trade station
		Property autoMode = config.get("Trade Station", "Auto mode enabled", true,
				"Set to false to disable the ability to automatically buy or sell items.");
		autoModeEnabled = autoMode.getBoolean(true);
		Property sellRatio = config.get("Trade Station", "Sell Ratio", 0.8,
				"Ratio of sell price to buy price. Set to less than 1.0 to give players a percentage of the full buy price when selling an item. (Range: 0.1 - 1.0)");
		itemSellRatio = Math.max(0.1, Math.min(sellRatio.getDouble(0.8), 1.0));
		Property tsBuyEnabled = config.get("Trade Station", "Trade Station Buy enabled", true,
				"Set to false to disable buying items from trade station.");
		tradeStationBuyEnabled = tsBuyEnabled.getBoolean(true);

		// packager
		Property smallPackage = config.get("Packager", "Small Package Price", 10);
		smallPackage.setComment("Set the price of small package");
		smallPackagePrice = Math.max(1, Math.min(smallPackage.getInt(10), 1000));
		Property medPackage = config.get("Packager", "Medium Package Price", 20);
		medPackage.setComment("Set the price of medium package");
		medPackagePrice = Math.max(1, Math.min(medPackage.getInt(20), 1000));
		Property largePackage = config.get("Packager", "Large Package Price", 40);
		largePackage.setComment("Set the price of large package");
		largePackagePrice = Math.max(1, Math.min(largePackage.getInt(40), 1000));

		// rf utility (power company stuff)
		Property rfWholesale = config.get("RF Utility", "Wholesale rate", 12);
		rfWholesale.setComment("Set payment per 10 kRF of power sold. Default: 12");
		rfWholesaleRate = Math.max(0, rfWholesale.getInt(12));
		Property rfRetail = config.get("RF Utility", "Retail rate", 15);
		rfRetail.setComment("Set payment per 10 kRF of power bought. Default: 15");
		rfRetailRate = Math.max(0, rfRetail.getInt(15));

		// world gen
		Property bankGenProperty = config.get("world generation", "Village bank weight", 8);
		bankGenProperty.setComment("Probability of adding bank to villages. min 0, max 20, default 8.");
		bankGenWeight = Math.max(0, Math.min(bankGenProperty.getInt(8), 20));
		Property shopGenProperty = config.get("world generation", "Village shop weight", 8);
		shopGenProperty.setComment("Probably of adding shop to villages. min 0, max 20, default 8.");
		shopGenWeight = Math.max(0, Math.min(shopGenProperty.getInt(8), 20));
		Property tradeGenProperty = config.get("world generation", "Village trade station weight", 8);
		tradeGenProperty.setComment("Probability of adding trade station to villages. min 0, max 20, default 8.");
		tradeGenWeight = Math.max(0, Math.min(tradeGenProperty.getInt(8), 20));

		Property shopMinPriceProperty = config.get("World Generation", "Minimum shop price", 100);
		shopMinPriceProperty.setComment(
				"Set the minimum price of items for sale in shops as a percent (min=1,max=100,default=100)");
		shopMinPrice = Math.max(1, Math.min(shopMinPriceProperty.getInt(100), 100));
		Property shopMaxPriceProperty = config.get("World Generation", "Maximum shop price", 140);
		shopMaxPriceProperty.setComment(
				"Set the maximum price of items for sale in shops as a percent (min=100,max=300,default=140)");
		shopMaxPrice = Math.max(100, Math.min(shopMaxPriceProperty.getInt(140), 300));

		config.save();

		MinecraftForge.EVENT_BUS.register(new UCPlayerPickupEventHandler());

		// network packet handling
		snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
		snw.registerMessage(UCButtonMessage.class, UCButtonMessage.class, 0, Side.SERVER);
		snw.registerMessage(UCVendorServerMessage.class, UCVendorServerMessage.class, 1, Side.SERVER);
		snw.registerMessage(UCSignServerMessage.class, UCSignServerMessage.class, 2, Side.SERVER);
		snw.registerMessage(UCTileSignMessage.class, UCTileSignMessage.class, 3, Side.CLIENT);
		snw.registerMessage(UCPackagerServerMessage.class, UCPackagerServerMessage.class, 4, Side.SERVER);
		snw.registerMessage(ATMWithdrawalMessage.class, ATMWithdrawalMessage.class, 5, Side.SERVER);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		// worldgen
		if (bankGenWeight > 0) {
			VillageGenBank villageHandler = new VillageGenBank();
			VillagerRegistry.instance().registerVillageCreationHandler(villageHandler);
		}
		if (shopGenWeight > 0) {
			VillageGenShop villageHandler2 = new VillageGenShop();
			VillagerRegistry.instance().registerVillageCreationHandler(villageHandler2);
		}
		if (tradeGenWeight > 0) {
			VillageGenTrade villageHandler3 = new VillageGenTrade();
			VillagerRegistry.instance().registerVillageCreationHandler(villageHandler3);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		UCItemPricer.getInstance().loadConfigs();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		ICommandManager command = event.getServer().getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		manager.registerCommand(new UCCommand());
		manager.registerCommand(new UCBalance());
		manager.registerCommand(new UCRebalance());
		manager.registerCommand(new UCGive());
		manager.registerCommand(new UCSend());
	}

	@SubscribeEvent
	public static void onLootTablesLoaded(LootTableLoadEvent event) {
		if (mobsDropCoins) {
			String[] stringArray = new String[] { "creeper", "enderman", "skeleton", "snowman", "witch", "spider",
					"zombie" };
			int index = 0;
			while (index < stringArray.length) {
				if (event.getName().toString().contains((stringArray[index]))) {
					final LootPool main = event.getTable().getPool("main");
					if (main != null) {
						main.addEntry(new LootEntryItem(UniversalCoins.Items.iron_coin, mobDropWeight, 0,
								new LootFunction[] {
										new SetCount(new LootCondition[0], new RandomValueRange(1, mobDropMax)) },
								new LootCondition[0], "universalcoins:mob_loot"));
					}
				}
				index++;
			}
			if (event.getName().toString().contains("dragon")) {
				final LootPool main = event.getTable().getPool("main");
				if (main != null) {
					int count = 0;
					while (count <= enderDragonMultiplier) {
						main.addEntry(new LootEntryItem(UniversalCoins.Items.obsidian_coin, mobDropWeight, 0,
								new LootFunction[] {
										new SetCount(new LootCondition[0], new RandomValueRange(1, mobDropMax)) },
								new LootCondition[0], "universalcoins:dragon_loot"));
					}
				}
			}
		}

		if (coinsInDungeon) {
			String[] stringArray = new String[] { "abandoned_mineshaft", "desert_pyramid", "end_city_treasure",
					"jungle_temple", "simple_dungeon", "stronghold_corridor", "stronghold_library",
					"stronghold_crossing" };
			int index = 0;
			while (index < stringArray.length) {
				if (event.getName().toString().contains((stringArray[index]))) {
					final LootPool main = event.getTable().getPool("main");
					if (main != null) {
						main.addEntry(new LootEntryItem(UniversalCoins.Items.diamond_coin, 20, 0,
								new LootFunction[] { new SetCount(new LootCondition[0], new RandomValueRange(1, 64)) },
								new LootCondition[0], "universalcoins:chest_loot"));
					}
				}
				index++;
			}
		}
	}
}
