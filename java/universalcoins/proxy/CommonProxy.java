package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.blocks.BlockBase;
import universalcoins.blocks.BlockCardStation;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockVendor;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemLargeCoinBag;
import universalcoins.items.ItemLargeCoinStack;
import universalcoins.items.ItemSeller;
import universalcoins.items.ItemSmallCoinBag;
import universalcoins.items.ItemSmallCoinStack;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemVendorWrench;
import universalcoins.util.Vending;

public class CommonProxy {
	public static Item itemCoin;
	public static Item itemSmallCoinStack;
	public static Item itemLargeCoinStack;
	public static Item itemSmallCoinBag;
	public static Item itemLargeCoinBag;
	public static Item itemSeller;
	public static Item itemUCCard;
	public static Item itemEnderCard;
	public static Item itemVendorWrench;
	
	public static Block blockTradeStation;
	public static Block blockVendor;
	public static Block blockCardStation;
	public static Block blockBase;
	
	
	public void registerBlocks() {
		blockTradeStation = new BlockTradeStation().setUnlocalizedName("blockTradeStation");
		blockVendor = new BlockVendor(Vending.supports).setUnlocalizedName("blockVendor");
		blockCardStation = new BlockCardStation().setUnlocalizedName("blockCardStation");
		blockBase = new BlockBase().setUnlocalizedName("blockBase");
	}
	
	public void registerItems() {
		itemCoin = new ItemCoin().setUnlocalizedName("itemCoin");
		itemSmallCoinStack = new ItemSmallCoinStack().setUnlocalizedName("itemSmallCoinStack");
		itemLargeCoinStack = new ItemLargeCoinStack().setUnlocalizedName("itemLargeCoinStack");
		itemSmallCoinBag = new ItemSmallCoinBag().setUnlocalizedName("itemSmallCoinBag");
		itemLargeCoinBag = new ItemLargeCoinBag().setUnlocalizedName("itemLargeCoinBag");
		itemUCCard = new ItemUCCard().setUnlocalizedName("itemUCCard");
		itemEnderCard = new ItemEnderCard().setUnlocalizedName("itemEnderCard");
		itemSeller = new ItemSeller().setUnlocalizedName("itemSeller");
		itemVendorWrench = new ItemVendorWrench().setUnlocalizedName("itemVendorWrench");
	}

	public void registerRenderers() {
		//blank since we don't do anything on the server
	}

}
