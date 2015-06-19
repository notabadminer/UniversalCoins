package universalcoins.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalcoins.UniversalCoins;

public class UCRecipeHelper {

	private static ItemStack oneSeller = new ItemStack(UniversalCoins.proxy.itemSeller);
	private static ItemStack oneCoin = new ItemStack(UniversalCoins.proxy.itemCoin);
	private static ItemStack oneSStack = new ItemStack(UniversalCoins.proxy.itemSmallCoinStack);
	private static ItemStack oneLStack = new ItemStack(UniversalCoins.proxy.itemLargeCoinStack);
	private static ItemStack oneSSack = new ItemStack(UniversalCoins.proxy.itemSmallCoinBag);
	private static ItemStack oneLSack = new ItemStack(UniversalCoins.proxy.itemLargeCoinBag);

	public static void addCoinRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemCoin, 9), new Object[] { oneSStack });
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemSmallCoinStack, 9),
				new Object[] { oneLStack });
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemLargeCoinStack, 9),
				new Object[] { oneSSack });
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemSmallCoinBag, 9),
				new Object[] { oneLSack });

		GameRegistry.addShapelessRecipe(oneSStack, new Object[] { oneCoin, oneCoin, oneCoin, oneCoin, oneCoin, oneCoin,
				oneCoin, oneCoin, oneCoin });
		GameRegistry.addShapelessRecipe(oneLStack, new Object[] { oneSStack, oneSStack, oneSStack, oneSStack,
				oneSStack, oneSStack, oneSStack, oneSStack, oneSStack });
		GameRegistry.addShapelessRecipe(oneSSack, new Object[] { oneLStack, oneLStack, oneLStack, oneLStack, oneLStack,
				oneLStack, oneLStack, oneLStack, oneLStack });
		GameRegistry.addShapelessRecipe(oneLSack, new Object[] { oneSSack, oneSSack, oneSSack, oneSSack, oneSSack,
				oneSSack, oneSSack, oneSSack, oneSSack });
	}

	public static void addTradeStationRecipe() {
		GameRegistry.addShapedRecipe(oneSeller, new Object[] { "LGE", "PPP", 'L', Items.leather, 'G', Items.gold_ingot,
				'E', Items.ender_pearl, 'P', Items.paper });
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockTradeStation), new Object[] { "IGI",
				"ICI", "III", 'I', Items.iron_ingot, 'G', Items.gold_ingot, 'C', UniversalCoins.proxy.itemSeller });
	}

	
	public static void addVendingBlockRecipes() { 
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockVendor), new Object[]{ 
			"XXX",
			"XRX", 
			"IGI", 'X', Blocks.glass , 'G', Items.gold_ingot, 'R', Items.redstone, 'I', Items.iron_ingot });
	}
	
	public static void addVendingFrameRecipes() { 
		GameRegistry.addRecipe(new RecipeVendingFrame()); 
	
	RecipeSorter.register("universalcoins:endercard", RecipeVendingFrame.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped"); 
	
	GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(UniversalCoins.proxy.blockVendorFrame), 
			"SGS", 
			"RPR",
			"SSS",'S', Items.stick, 'P', "plankWood", 'G', Items.gold_ingot, 'R', Items.redstone)); }
	
	public static void addSignRecipes() { 
		
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemUCSign), new Object[]{ new
			ItemStack(Items.sign) }); 
		
		GameRegistry.addShapelessRecipe(new ItemStack(Items.sign), new Object[]{ new
			ItemStack(UniversalCoins.proxy.itemUCSign) }); }

	public static void addCardStationRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockCardStation), new Object[] { "III", "ICI",
				"III", 'I', Items.iron_ingot, 'C', UniversalCoins.proxy.itemSmallCoinBag });
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockBase), new Object[] { "III", "ICI", "III",
				'I', Items.iron_ingot, 'C', UniversalCoins.proxy.itemCoin });
	}

	public static void addBlockSafeRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockSafe), new Object[] { "III", "IEI", "III",
				'I', Items.iron_ingot, 'E', UniversalCoins.proxy.itemEnderCard });
	}

	public static void addEnderCardRecipes() {
		// we register our custom recipe and then register the normal way
		// the custom takes priority for crafting while the normal allows nei
		// and craftguide to display the recipe
		GameRegistry.addRecipe(new RecipeEnderCard());
		RecipeSorter.register("universalcoins:endercard", RecipeEnderCard.class, RecipeSorter.Category.SHAPED,
				"after:minecraft:shaped");
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.itemEnderCard), new Object[] { "XEX", "ECE",
				"XEX", 'E', Items.ender_pearl, 'C', UniversalCoins.proxy.itemUCCard });
	}

	public static void addBanditRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockBandit), new Object[] { "IGI", "IRI",
				"III", 'I', Items.iron_ingot, 'R', Items.redstone, 'G', Items.gold_ingot });
	}

	public static void addSignalRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockSignal), new Object[] { "XIX", "IRI",
				"XIX", 'I', Items.iron_ingot, 'R', Items.redstone });
	}

	
	public static void addLinkCardRecipes() {
	GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.itemLinkCard), new Object[]{ Items.paper,
		Items.paper, Items.ender_pearl }); }
	

	public static void addPackagerRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.blockPackager), new Object[] { "IPI", "SRS",
				"IRI", 'I', Items.iron_ingot, 'R', Items.redstone, 'S', Items.string, 'P', Items.paper });
	}
	
	public static void addPlankTextureRecipes() { 
		GameRegistry.addRecipe(new RecipePlankTextureChange());
		RecipeSorter.register("universalcoins:plankchange", RecipePlankTextureChange.class, RecipeSorter.Category.SHAPELESS,
			"after:minecraft:shapeless"); }
	
}