package universalcoins.util;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.RecipeSorter;
import universalcoins.UniversalCoins;

public class UCRecipeHelper {

	private static ItemStack catalog = new ItemStack(UniversalCoins.proxy.catalog);
	private static ItemStack iron_coin = new ItemStack(UniversalCoins.proxy.iron_coin);
	private static ItemStack gold_coin = new ItemStack(UniversalCoins.proxy.gold_coin);
	private static ItemStack emerald_coin = new ItemStack(UniversalCoins.proxy.emerald_coin);
	private static ItemStack diamond_coin = new ItemStack(UniversalCoins.proxy.diamond_coin);
	private static ItemStack obsidian_coin = new ItemStack(UniversalCoins.proxy.obsidian_coin);

	public static void addTradeStationRecipe() {
		GameRegistry.addShapedRecipe(catalog, new Object[] { "LGE", "PPP", 'L', Items.leather, 'G', Items.gold_ingot,
				'E', Items.ender_pearl, 'P', Items.paper });
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.trade_station), new Object[] { "IGI", "ICI",
				"III", 'I', Items.iron_ingot, 'G', Items.gold_ingot, 'C', UniversalCoins.proxy.catalog });
	}

	public static void addVendingBlockRecipes() {
		for (int i = 0; i < Vending.supports.length; i++) {
			GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.vendor, 1, i), new Object[] { "XXX", "XRX",
					"*G*", 'X', Blocks.glass, 'G', Items.gold_ingot, 'R', Items.redstone, '*', Vending.reagents[i] });
		}
	}

	public static void addVendingFrameRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.vendor_frame),
				new Object[] { " S ", "SPS", " S ", 'P', Blocks.planks, 'S', Items.stick });
	}

	public static void addSignRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.uc_sign),
				new Object[] { new ItemStack(Items.sign) });
		GameRegistry.addShapelessRecipe(new ItemStack(Items.sign),
				new Object[] { new ItemStack(UniversalCoins.proxy.uc_sign) });
	}

	public static void addCardStationRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.atm),
				new Object[] { "III", "ICI", "III", 'I', Items.iron_ingot, 'C', UniversalCoins.proxy.diamond_coin });
	}

	public static void addBlockSafeRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.safe),
				new Object[] { "III", "IEI", "III", 'I', Items.iron_ingot, 'E', UniversalCoins.proxy.ender_card });
	}

	public static void addEnderCardRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.ender_card),
				new Object[] { " E ", "ECE", " E ", 'C', UniversalCoins.proxy.uc_card, 'E', Items.ender_pearl });
	}

	public static void addSignalRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.signal_block),
				new Object[] { "IXI", "XRX", "IXI", 'I', Items.iron_ingot, 'R', Items.redstone });
	}

	public static void addLinkCardRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(UniversalCoins.proxy.link_card),
				new Object[] { Items.paper, Items.paper, Items.ender_pearl });
	}

	public static void addPackagerRecipes() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.packager), new Object[] { "IPI", "SRS", "IRI",
				'I', Items.iron_ingot, 'R', Items.redstone, 'S', Items.string, 'P', Items.paper });
	}

	public static void addPowerTransmitterRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.power_transmitter), new Object[] { "III", "MRM",
				"III", 'I', Items.iron_ingot, 'R', Blocks.redstone_block, 'M', Items.redstone });
	}

	public static void addPowerReceiverRecipe() {
		GameRegistry.addShapedRecipe(new ItemStack(UniversalCoins.proxy.power_receiver), new Object[] { "III", "MRM",
				"III", 'I', Items.iron_ingot, 'R', Blocks.redstone_block, 'M', new ItemStack(Items.dye, 1, 4) });
	}
}