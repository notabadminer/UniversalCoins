package universalcoins.util;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalcoins.UniversalCoins;

public class UCRecipeHelper {

	private static ItemStack seller = new ItemStack(UniversalCoins.proxy.catalog);

	public static void addTradeStationRecipe() {
		GameRegistry.addShapedRecipe(seller, new Object[] { "LGE", "PPP", 'L', Items.leather, 'G', Items.gold_ingot,
				'E', Items.ender_pearl, 'P', Items.paper });
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.tradestation), new Object[] { "IGI", "ICI",
				"III", 'I', Items.iron_ingot, 'G', Items.gold_ingot, 'C', UniversalCoins.proxy.catalog });
	}

	public static void addVendingBlockRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.vendor), new Object[] { "XXX", "XRX",
				"IGI", 'X', Blocks.glass, 'G', Items.gold_ingot, 'R', Items.redstone, 'I', Items.iron_ingot });
	}

	public static void addVendingFrameRecipes() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(UniversalCoins.proxy.vendor_frame), "SGS", "RPR",
				"SSS", 'S', Items.stick, 'P', "plankWood", 'G', Items.gold_ingot, 'R', Items.redstone));
	}

	public static void addSignRecipes() {

		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.uc_sign),
				new Object[] { new ItemStack(Items.sign) });

		GameRegistry.addShapelessRecipe(new ItemStack(Items.sign),
				new Object[] { new ItemStack(UniversalCoins.proxy.uc_sign) });
	}

	public static void addCardStationRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.atm), new Object[] { "III", "ICI",
				"III", 'I', Items.iron_ingot, 'C', UniversalCoins.proxy.emerald_coin });
	}

	public static void addBlockSafeRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.safe),
				new Object[] { "III", "IEI", "III", 'I', Items.iron_ingot, 'E', UniversalCoins.proxy.ender_card });
	}

	public static void addEnderCardRecipes() {		
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.ender_card),
				new Object[] { "XEX", "ECE", "XEX", 'E', Items.ender_pearl, 'C', UniversalCoins.proxy.uc_card });
	}

	public static void addSignalRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.signalblock),
				new Object[] { "XIX", "IRI", "XIX", 'I', Items.iron_ingot, 'R', Items.redstone });
	}

	public static void addLinkCardRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.link_card),
				new Object[] { Items.paper, Items.paper, Items.ender_pearl });
	}

	public static void addPackagerRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.packager), new Object[] { "IPI", "SRS",
				"IRI", 'I', Items.iron_ingot, 'R', Items.redstone, 'S', Items.string, 'P', Items.paper });
	}
}