package universalcoins;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import universalcoins.commands.UCBalance;
import universalcoins.commands.UCCommand;
import universalcoins.commands.UCGive;
import universalcoins.commands.UCRebalance;
import universalcoins.commands.UCSend;
import universalcoins.net.UCBanditServerMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCCardStationServerCustomNameMessage;
import universalcoins.net.UCCardStationServerWithdrawalMessage;
import universalcoins.net.UCRecipeMessage;
import universalcoins.net.UCSignServerMessage;
import universalcoins.net.UCTextureMessage;
import universalcoins.net.UCTileSignMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.proxy.CommonProxy;
import universalcoins.tile.TileBandit;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendorBlock;
import universalcoins.tile.TileVendorFrame;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UCMobDropEventHandler;
import universalcoins.util.UCPlayerLoginEventHandler;
import universalcoins.util.UCPlayerPickupEventHandler;
import universalcoins.util.UCRecipeHelper;
import universalcoins.worldgen.VillageGenBank;
import universalcoins.worldgen.VillageGenShop;

/**
 * UniversalCoins, Sell all your extra blocks and buy more!!! Create a trading economy, jobs, whatever.
 * 
 * @author notabadminer, ted_996, AUTOMATIC_MAIDEN
 * 
 **/

@Mod(modid = UniversalCoins.modid, name = UniversalCoins.name, version = UniversalCoins.version, acceptedMinecraftVersions = "[1.8.0]", dependencies = "required-after:Forge@[11.14.3.1446,)")
public class UniversalCoins {
	@Instance("universalcoins")
	public static UniversalCoins instance;
	public static final String modid = "universalcoins";
	public static final String name = "Universal Coins";
	public static final String version = "1.8-2.0.3";

	public static Boolean autoModeEnabled;
	public static Boolean tradeStationRecipesEnabled;
	public static Boolean vendorRecipesEnabled;
	public static Boolean vendorFrameRecipesEnabled;
	public static Boolean atmRecipeEnabled;
	public static Boolean enderCardRecipeEnabled;
	public static Boolean banditRecipeEnabled;
	public static Boolean signalRecipeEnabled;
	public static Boolean linkCardRecipeEnabled;
	public static Boolean tradeStationBuyEnabled;
	public static Boolean packagerRecipeEnabled;
	public static Boolean mobsDropCoins;
	public static Boolean coinsInMineshaft;
	public static Boolean bankGenEnabled;
	public static Boolean shopGenEnabled;
	public static Integer shopMinPrice;
	public static Integer shopMaxPrice;
	public static Integer mineshaftCoinChance;
	public static Boolean coinsInDungeon;
	public static Integer dungeonCoinChance;
	public static Integer mobDropMax;
	public static Integer mobDropChance;
	public static Double itemSellRatio;
	public static Integer fourMatchPayout;
	public static Integer fiveMatchPayout;
	public static Integer smallPackagePrice;
	public static Integer medPackagePrice;
	public static Integer largePackagePrice;

	public static SimpleNetworkWrapper snw;

	public static CreativeTabs tabUniversalCoins = new UCTab("tabUniversalCoins");

	@SidedProxy(clientSide = "universalcoins.proxy.ClientProxy", serverSide = "universalcoins.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		// recipes
		Property recipes = config.get("Recipes", "Trade Station Recipes", true);
		recipes.comment = "Set to false to disable crafting recipes for selling catalog and trade station.";
		tradeStationRecipesEnabled = recipes.getBoolean(true);
		Property vendorRecipes = config.get("Recipes", "Vending Block Recipes", true);
		vendorRecipes.comment = "Set to false to disable crafting recipes for vending blocks.";
		vendorRecipesEnabled = vendorRecipes.getBoolean(true);
		Property vendorFrameRecipe = config.get("Recipes", "Vending Frame Recipe", true);
		vendorFrameRecipe.comment = "Set to false to disable crafting recipes for Vending Frame.";
		vendorFrameRecipesEnabled = vendorFrameRecipe.getBoolean(true);
		Property atmRecipe = config.get("Recipes", "ATM Recipe", true);
		atmRecipe.comment = "Set to false to disable crafting recipes for ATM.";
		atmRecipeEnabled = atmRecipe.getBoolean(true);
		Property enderCardRecipe = config.get("Recipes", "Ender Card Recipe", true);
		enderCardRecipe.comment = "Set to false to disable crafting recipes for Ender Card and Safe.";
		enderCardRecipeEnabled = enderCardRecipe.getBoolean(true);
		Property banditRecipe = config.get("Recipes", "Slot Machine Recipe", true);
		banditRecipe.comment = "Set to false to disable crafting recipes for Slot Machine.";
		banditRecipeEnabled = banditRecipe.getBoolean(true);
		Property signalRecipe = config.get("Recipes", "Redstone Signal Generator Recipe", true);
		signalRecipe.comment = "Set to false to disable crafting recipes for Redstone Signal Generator.";
		signalRecipeEnabled = signalRecipe.getBoolean(true);
		Property linkCardRecipe = config.get("Recipes", "Remote Storage Linking Card Recipe", true);
		linkCardRecipe.comment = "Set to false to disable crafting recipes for Linking Card.";
		linkCardRecipeEnabled = linkCardRecipe.getBoolean(true);
		Property packagerRecipe = config.get("Recipes", "Packager Recipe", true);
		packagerRecipe.comment = "Set to false to disable crafting recipes for Packager.";
		packagerRecipeEnabled = packagerRecipe.getBoolean(true);

		// loot
		Property mobDrops = config.get("Loot", "Mob Drops", true);
		mobDrops.comment = "Set to false to disable mobs dropping coins on death.";
		mobsDropCoins = mobDrops.getBoolean(true);
		Property dropAmount = config.get("Loot", "Mob Drop Max", 39);
		dropAmount.comment = "Max mob drop stacksize. Minimum 1. Maximum 64. Default 39.";
		mobDropMax = Math.max(1, Math.min(dropAmount.getInt(39), 64));
		Property dropChance = config.get("Loot", "Mob Drop Chance", 3);
		dropChance.comment = "Chance of a mob dropping coins. Lower number means higher chance. Minimum 0 (always drop). Default 3 (1 in 4 chance).";
		mobDropChance = Math.max(0, Math.min(dropChance.getInt(3), 100));
		Property mineshaftCoins = config.get("Loot", "Mineshaft CoinBag", true);
		mineshaftCoins.comment = "Set to false to disable coinbag spawning in mineshaft chests.";
		coinsInMineshaft = mineshaftCoins.getBoolean(true);
		Property mineshaftCoinRate = config.get("Loot", "Mineshaft CoinBag Spawnrate", 20);
		mineshaftCoinRate.comment = "Rate of coinbag spawning in mineshaft chests. Higher value equals more common. Default is 20.";
		mineshaftCoinChance = Math.max(1, Math.min(mineshaftCoinRate.getInt(20), 100));
		Property dungeonCoins = config.get("Loot", "Dungeon CoinBag", true);
		dungeonCoins.comment = "Set to false to disable coinbag spawning in dungeon chests.";
		coinsInDungeon = dungeonCoins.getBoolean(true);
		Property dungeonCoinRate = config.get("Loot", "Dungeon CoinBag Spawnrate", 20);
		dungeonCoinRate.comment = "Rate of coinbag spawning in dungeon chests. Higher value equals more common. Default is 20.";
		dungeonCoinChance = Math.max(1, Math.min(dungeonCoinRate.getInt(20), 100));

		// slot machine
		Property fourMatch = config.get("Slot Machine", "Four of a kind payout", 100);
		fourMatch.comment = "Set payout of slot machine when four of a kind is spun. Default: 100";
		fourMatchPayout = Math.max(0, fourMatch.getInt(100));
		Property fiveMatch = config.get("Slot Machine", "Five of a kind payout", 10000);
		fiveMatch.comment = "Set payout of slot machine when five of a kind is spun. Default: 10000";
		fiveMatchPayout = Math.max(0, fiveMatch.getInt(10000));

		// trade station
		Property autoMode = config.get("Trade Station", "Auto mode enabled", true);
		autoMode.comment = "Set to false to disable the ability to automatically buy or sell items.";
		autoModeEnabled = autoMode.getBoolean(true);
		Property sellRatio = config.get("Trade Station", "Sell Ratio", 0.8);
		sellRatio.comment = "Ratio of sell price to buy price. Set to less than 1.0 to give players a percentage of the full buy price when selling an item. (Range: 0.1 - 1.0)";
		itemSellRatio = Math.max(0.1, Math.min(sellRatio.getDouble(0.8), 1.0));
		Property tsBuyEnabled = config.get("Trade Station", "Trade Station Buy enabled", true);
		tsBuyEnabled.comment = "Set to false to disable buying items from trade station.";
		tradeStationBuyEnabled = tsBuyEnabled.getBoolean(true);

		// packager
		Property smallPackage = config.get("Packager", "Small Package Price", 10);
		smallPackage.comment = "Set the price of small package";
		smallPackagePrice = Math.max(1, Math.min(smallPackage.getInt(10), 1000));
		Property medPackage = config.get("Packager", "Medium Package Price", 20);
		medPackage.comment = "Set the price of medium package";
		medPackagePrice = Math.max(1, Math.min(medPackage.getInt(20), 1000));
		Property largePackage = config.get("Packager", "Large Package Price", 40);
		largePackage.comment = "Set the price of large package";
		largePackagePrice = Math.max(1, Math.min(largePackage.getInt(40), 1000));

		// world gen
		Property bankGenProperty = config.get("World Generation", "Village bank enabled", true);
		bankGenProperty.comment = "Set to false to disable chance of adding bank to villages.";
		bankGenEnabled = bankGenProperty.getBoolean(true);
		Property shopGenProperty = config.get("World Generation", "Village shop enabled", true);
		shopGenProperty.comment = "Set to false to disable chance of adding shop to villages.";
		shopGenEnabled = shopGenProperty.getBoolean(true);

		Property shopMinPriceProperty = config.get("World Generation", "Minimum shop price", 80);
		shopMinPriceProperty.comment = "Set the minimum price of items for sale in shops as a percent (min=1,max=100,default=80)";
		shopMinPrice = Math.max(1, Math.min(shopMinPriceProperty.getInt(80), 100));
		Property shopMaxPriceProperty = config.get("World Generation", "Maximum shop price", 120);
		shopMaxPriceProperty.comment = "Set the maximum price of items for sale in shops as a percent (min=80,max=300,default=120)";
		shopMaxPrice = Math.max(80, Math.min(shopMaxPriceProperty.getInt(120), 300));

		config.save();

		if (mobsDropCoins) {
			MinecraftForge.EVENT_BUS.register(new UCMobDropEventHandler());
		}

		MinecraftForge.EVENT_BUS.register(new UCPlayerPickupEventHandler());
		FMLCommonHandler.instance().bus().register(new UCPlayerLoginEventHandler());

		// network packet handling
		snw = NetworkRegistry.INSTANCE.newSimpleChannel(modid);
		snw.registerMessage(UCButtonMessage.class, UCButtonMessage.class, 0, Side.SERVER);
		snw.registerMessage(UCVendorServerMessage.class, UCVendorServerMessage.class, 1, Side.SERVER);
		snw.registerMessage(UCCardStationServerWithdrawalMessage.class, UCCardStationServerWithdrawalMessage.class, 2,
				Side.SERVER);
		snw.registerMessage(UCCardStationServerCustomNameMessage.class, UCCardStationServerCustomNameMessage.class, 3,
				Side.SERVER);
		snw.registerMessage(UCTextureMessage.class, UCTextureMessage.class, 4, Side.SERVER);
		snw.registerMessage(UCBanditServerMessage.class, UCBanditServerMessage.class, 5, Side.SERVER);
		snw.registerMessage(UCSignServerMessage.class, UCSignServerMessage.class, 6, Side.SERVER);
		snw.registerMessage(UCTileSignMessage.class, UCTileSignMessage.class, 7, Side.CLIENT);
		snw.registerMessage(UCRecipeMessage.class, UCRecipeMessage.class, 8, Side.CLIENT);

		// update check using versionchecker
		FMLInterModComms.sendRuntimeMessage(modid, "VersionChecker", "addVersionCheck",
				"https://raw.githubusercontent.com/notabadminer/UniversalCoins/master/version.json");
	}

	@EventHandler
	public void postInitialise(FMLPostInitializationEvent event) {
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerBlocks();
		proxy.registerItems();
		proxy.registerRenderers();

		if (coinsInMineshaft) {
			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(
					new WeightedRandomChestContent(new ItemStack(proxy.itemLargeCoinBag), 2, 64, mineshaftCoinChance));
		}
		if (coinsInDungeon) {
			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(
					new WeightedRandomChestContent(new ItemStack(proxy.itemLargeCoinBag), 2, 64, dungeonCoinChance));
		}

		GameRegistry.registerTileEntity(TileTradeStation.class, "TileTradeStation");
		GameRegistry.registerTileEntity(TileCardStation.class, "TileCardStation");
		GameRegistry.registerTileEntity(TileSafe.class, "TileSafe");
		GameRegistry.registerTileEntity(TileBandit.class, "TileBandit");
		GameRegistry.registerTileEntity(TileSignal.class, "TileSignal");
		GameRegistry.registerTileEntity(TilePackager.class, "TilePackager");
		GameRegistry.registerTileEntity(TileVendorBlock.class, "TileVendorBlock");
		GameRegistry.registerTileEntity(TileVendorFrame.class, "TileVendorFrame");
		GameRegistry.registerTileEntity(TileUCSign.class, "TileUCSign");

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		UCRecipeHelper.addCoinRecipes();
		if (tradeStationRecipesEnabled) {
			UCRecipeHelper.addTradeStationRecipe();
		}
		if (vendorRecipesEnabled) {
			UCRecipeHelper.addVendingBlockRecipes();
		}
		if (vendorFrameRecipesEnabled) {
			UCRecipeHelper.addVendingFrameRecipes();
		}
		if (atmRecipeEnabled) {
			UCRecipeHelper.addCardStationRecipes();
		}
		if (enderCardRecipeEnabled) {
			UCRecipeHelper.addEnderCardRecipes();
			UCRecipeHelper.addBlockSafeRecipe();
		}
		if (banditRecipeEnabled) {
			UCRecipeHelper.addBanditRecipes();
		}
		if (signalRecipeEnabled) {
			UCRecipeHelper.addSignalRecipes();
		}
		if (linkCardRecipeEnabled) {
			UCRecipeHelper.addLinkCardRecipes();
		}
		if (packagerRecipeEnabled) {
			UCRecipeHelper.addPackagerRecipes();
		}
		UCRecipeHelper.addSignRecipes();
		UCRecipeHelper.addPlankTextureRecipes();

		// worldgen
		if (bankGenEnabled) {
			VillageGenBank villageHandler = new VillageGenBank();
			VillagerRegistry.instance().registerVillageCreationHandler(villageHandler);
		}
		if (shopGenEnabled) {
			VillageGenShop villageHandler2 = new VillageGenShop();
			VillagerRegistry.instance().registerVillageCreationHandler(villageHandler2);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// auto pricing the items takes a while so we start a thread
		// and let it work in the background.
		Runnable r = new Runnable() {
			public void run() {
				UCItemPricer.getInstance().loadConfigs();
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		manager.registerCommand(new UCCommand());
		manager.registerCommand(new UCBalance());
		manager.registerCommand(new UCRebalance());
		manager.registerCommand(new UCGive());
		manager.registerCommand(new UCSend());
	}

}
