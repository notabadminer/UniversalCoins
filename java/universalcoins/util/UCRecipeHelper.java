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
		GameRegistry.addShapedRecipe(seller, new Object[] { "LGE", "PPP", 'L', Items.LEATHER, 'G', Items.GOLD_INGOT,
				'E', Items.ENDER_PEARL, 'P', Items.PAPER });
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.tradestation), new Object[] { "IGI", "ICI",
				"III", 'I', Items.IRON_INGOT, 'G', Items.GOLD_INGOT, 'C', UniversalCoins.proxy.catalog });
	}

	public static void addVendingBlockRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.vendor), new Object[] { "XXX", "XRX",
				"IGI", 'X', Blocks.GLASS, 'G', Items.GOLD_INGOT, 'R', Items.REDSTONE, 'I', Items.IRON_INGOT });
	}

	public static void addVendingFrameRecipes() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(UniversalCoins.proxy.vendor_frame), "SGS", "RPR",
				"SSS", 'S', Items.STICK, 'P', "plankWood", 'G', Items.GOLD_INGOT, 'R', Items.REDSTONE));
	}

	public static void addSignRecipes() {

		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.uc_sign),
				new Object[] { new ItemStack(Items.SIGN) });

		GameRegistry.addShapelessRecipe(new ItemStack(Items.SIGN),
				new Object[] { new ItemStack(UniversalCoins.proxy.uc_sign) });
	}

	public static void addCardStationRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.atm), new Object[] { "III", "ICI",
				"III", 'I', Items.IRON_INGOT, 'C', UniversalCoins.proxy.emerald_coin });
	}

	public static void addBlockSafeRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.safe),
				new Object[] { "III", "IEI", "III", 'I', Items.IRON_INGOT, 'E', UniversalCoins.proxy.ender_card });
	}

	public static void addEnderCardRecipes() {		
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.ender_card),
				new Object[] { "XEX", "ECE", "XEX", 'E', Items.ENDER_PEARL, 'C', UniversalCoins.proxy.uc_card });
	}

	public static void addSignalRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.signalblock),
				new Object[] { "XIX", "IRI", "XIX", 'I', Items.IRON_INGOT, 'R', Items.REDSTONE });
	}

	public static void addLinkCardRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.link_card),
				new Object[] { Items.PAPER, Items.PAPER, Items.ENDER_PEARL });
	}

	public static void addPackagerRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.packager), new Object[] { "IPI", "SRS",
				"IRI", 'I', Items.IRON_INGOT, 'R', Items.REDSTONE, 'S', Items.STRING, 'P', Items.PAPER });
	}
}