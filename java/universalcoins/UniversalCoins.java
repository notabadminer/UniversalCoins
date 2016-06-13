package universalcoins;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
import universalcoins.net.ATMWithdrawalMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCPackagerServerMessage;
import universalcoins.net.UCSignServerMessage;
import universalcoins.net.UCTileSignMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.proxy.CommonProxy;
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
import universalcoins.util.*;
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

@Mod(modid = UniversalCoins.MODID, name = UniversalCoins.NAME, version = UniversalCoins.VERSION, acceptedMinecraftVersions = "@MC_VERSION@")
public class UniversalCoins {
	@Instance("universalcoins")
	public static UniversalCoins instance;
	public static final String MODID = "universalcoins";
	public static final String NAME = "Universal Coins";
	public static final String VERSION = "@VERSION@";

	public static CreativeTabs tabUniversalCoins = new UCTab("tabUniversalCoins");

	public static UCItemPricer itemPricer;

	@SidedProxy(clientSide = "universalcoins.proxy.ClientProxy", serverSide = "universalcoins.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static int[] coinValues;
	public static Boolean blockProtection;
	public static Boolean autoModeEnabled;
	public static Boolean tradeStationRecipesEnabled;
	public static Boolean vendorRecipesEnabled;
	public static Boolean vendorFrameRecipesEnabled;
	public static Boolean atmRecipeEnabled;
	public static Boolean enderCardRecipeEnabled;
	public static Boolean signalRecipeEnabled;
	public static Boolean linkCardRecipeEnabled;
	public static Boolean tradeStationBuyEnabled;
	public static Boolean packagerRecipeEnabled;
	public static Boolean mobsDropCoins;
	public static Boolean coinsInMineshaft;
	public static Boolean powerBaseRecipeEnabled;
	public static Boolean powerReceiverRecipeEnabled;
	public static Boolean coinsInDungeon;
	public static Integer mobDropMax;
	public static Integer mobDropChance;
	public static Integer enderDragonMultiplier;
	public static Double itemSellRatio;
	public static Integer smallPackagePrice;
	public static Integer medPackagePrice;
	public static Integer largePackagePrice;
	public static Integer rfWholesaleRate;
	public static Integer rfRetailRate;
	public static Integer bankGenWeight;
	public static Integer shopGenWeight;
	public static Integer tradeGenWeight;
	public static Integer shopMinPrice;
	public static Integer shopMaxPrice;
	public static String pricerType;
	public static int pricerThreshold;

	public static SimpleNetworkWrapper snw;

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

		// recipes
		Property recipes = config.get("Recipes", "Trade Station Recipes", true);
		recipes.setComment("Set to false to disable crafting recipes for selling catalog and trade station.");
		tradeStationRecipesEnabled = recipes.getBoolean(true);
		Property vendorRecipes = config.get("Recipes", "Vending Block Recipes", true);
		vendorRecipes.setComment("Set to false to disable crafting recipes for vending blocks.");
		vendorRecipesEnabled = vendorRecipes.getBoolean(true);
		Property vendorFrameRecipe = config.get("Recipes", "Vending Frame Recipe", true);
		vendorFrameRecipe.setComment("Set to false to disable crafting recipes for Vending Frame.");
		vendorFrameRecipesEnabled = vendorFrameRecipe.getBoolean(true);
		Property atmRecipe = config.get("Recipes", "ATM Recipe", true);
		atmRecipe.setComment("Set to false to disable crafting recipes for ATM.");
		atmRecipeEnabled = atmRecipe.getBoolean(true);
		Property enderCardRecipe = config.get("Recipes", "Ender Card Recipe", true);
		enderCardRecipe.setComment("Set to false to disable crafting recipes for Ender Card and Safe.");
		enderCardRecipeEnabled = enderCardRecipe.getBoolean(true);
		Property signalRecipe = config.get("Recipes", "Redstone Signal Generator Recipe", true);
		signalRecipe.setComment("Set to false to disable crafting recipes for Redstone Signal Generator.");
		signalRecipeEnabled = signalRecipe.getBoolean(true);
		Property linkCardRecipe = config.get("Recipes", "Remote Storage Linking Card Recipe", true);
		linkCardRecipe.setComment("Set to false to disable crafting recipes for Linking Card.");
		linkCardRecipeEnabled = linkCardRecipe.getBoolean(true);
		Property packagerRecipe = config.get("Recipes", "Packager Recipe", true);
		packagerRecipe.setComment("Set to false to disable crafting recipes for Packager.");
		packagerRecipeEnabled = packagerRecipe.getBoolean(true);

		//Pricer
		Property itemPricer = config.get("Pricer", "PricerType", "static");
		itemPricer.setComment("How the prices for Items in the Trade Station are determined. \n" +
						"'static' uses the configured price all the time.\n" +
						"'economic' starts at the configured price and scales it up or down based on how much is bought and sold.\n" +
						"economic pricer should be considered to be in Alpha and is almost certainly illogical and abuseable around edge cases. use with caution.");
		String[] validPricers =  {"static", "economic"};
		itemPricer.setValidValues(validPricers);
		pricerType = itemPricer.getString();

		Property threshold = config.get("Pricer", "PricerThreshold", 256);
		threshold.setComment("Number of items sold after witch an Item is worthless to the market. Used to determine demand curve slope");
		pricerThreshold = threshold.getInt();

		// loot
		Property mobDrops = config.get("Loot", "Mob Drops", true,
				"Set to false to disable mobs dropping coins on death.");
		mobsDropCoins = mobDrops.getBoolean(true);
		Property dropAmount = config.get("Loot", "Mob Drop Max", 39,
				"Max mob drop stacksize. Minimum 1. Maximum 64. Default 39.");
		mobDropMax = Math.max(1, Math.min(dropAmount.getInt(39), 64));
		Property dropChance = config.get("Loot", "Mob Drop Chance", 3,
				"Chance of a mob dropping coins. Lower number means higher chance. Minimum 0 (always drop). Default 3 (1 in 4 chance).");
		mobDropChance = Math.max(0, Math.min(dropChance.getInt(3), 100));
		Property dragonMultiplier = config.get("Loot", "Ender Dragon Multiplier", 1000,
				"Drop multiplier for ender dragon kills. Minimum 1. Default 1,000. Max 100,000");
		enderDragonMultiplier = Math.max(1, Math.min(dragonMultiplier.getInt(1000), 100000));

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
		Property rfBaseEnabled = config.get("RF Utility", "Power Base enabled", true);
		rfBaseEnabled.setComment("Set to false to disable the power base block.");
		powerBaseRecipeEnabled = rfBaseEnabled.getBoolean(true);
		Property rfReceiverEnabled = config.get("RF Utility", "RF Blocks enabled", true);
		rfReceiverEnabled.setComment("Set to false to disable the power receiver block.");
		powerReceiverRecipeEnabled = rfReceiverEnabled.getBoolean(true);
		Property rfWholesale = config.get("RF Utility", "Wholesale rate", 12);
		rfWholesale.setComment("Set payment per 10 kRF of power sold. Default: 12");
		rfWholesaleRate = Math.max(0, rfWholesale.getInt(12));
		Property rfRetail = config.get("RF Utility", "Retail rate", 15);
		rfRetail.setComment("Set payment per 10 kRF of power bought. Default: 15");
		rfRetailRate = Math.max(0, rfRetail.getInt(15));

		// world gen
		Property bankGenProperty = config.get("world generation", "Village bank weight", 6);
		bankGenProperty.setComment("Probability of adding bank to villages. min 0, max 20, default 6.");
		bankGenWeight = Math.max(0, Math.min(bankGenProperty.getInt(6), 20));
		Property shopGenProperty = config.get("world generation", "Village shop weight", 6);
		shopGenProperty.setComment("Probably of adding shop to villages. min 0, max 20, default 6.");
		shopGenWeight = Math.max(0, Math.min(shopGenProperty.getInt(6), 20));
		Property tradeGenProperty = config.get("world generation", "Village trade station weight", 6);
		tradeGenProperty.setComment("Probability of adding trade station to villages. min 0, max 20, default 6.");
		tradeGenWeight = Math.max(0, Math.min(tradeGenProperty.getInt(6), 20));

		Property shopMinPriceProperty = config.get("World Generation", "Minimum shop price", 80);
		shopMinPriceProperty
				.setComment("Set the minimum price of items for sale in shops as a percent (min=1,max=100,default=80)");
		shopMinPrice = Math.max(1, Math.min(shopMinPriceProperty.getInt(80), 100));
		Property shopMaxPriceProperty = config.get("World Generation", "Maximum shop price", 120);
		shopMaxPriceProperty.setComment(
				"Set the maximum price of items for sale in shops as a percent (min=80,max=300,default=120)");
		shopMaxPrice = Math.max(80, Math.min(shopMaxPriceProperty.getInt(120), 300));

		config.save();

		if (mobsDropCoins) {
			MinecraftForge.EVENT_BUS.register(new UCMobDropEventHandler());
		}

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
		proxy.registerBlocks();
		proxy.registerItems();
		proxy.registerRenderers();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		GameRegistry.registerTileEntity(TileProtected.class, "TileProtected");
		GameRegistry.registerTileEntity(TileTradeStation.class, "TileTradeStation");
		GameRegistry.registerTileEntity(TileSafe.class, "TileSafe");
		GameRegistry.registerTileEntity(TileSignal.class, "TileSignal");
		GameRegistry.registerTileEntity(TileVendor.class, "TileVendor");
		GameRegistry.registerTileEntity(TileVendorBlock.class, "TileVendorBlock");
		GameRegistry.registerTileEntity(TileVendorFrame.class, "TileVendorFrame");
		GameRegistry.registerTileEntity(TilePackager.class, "TilePackager");
		GameRegistry.registerTileEntity(TileUCSign.class, "TileUCSign");
		GameRegistry.registerTileEntity(TilePowerTransmitter.class, "TilePowerTransmitter");
		GameRegistry.registerTileEntity(TilePowerReceiver.class, "TilePowerReceiver");
		GameRegistry.registerTileEntity(TileATM.class, "TileATM");

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
		switch(pricerType){
			case "static":
				itemPricer = new UCStaticItemPricer();
				break;
			case "economic":
				itemPricer = new UCEconomicItemPricer();
				break;
			default:
				throw new Error("Invalid Pricer Type in universalcoins.cfg");
		}

		itemPricer.loadConfigs();
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
}
