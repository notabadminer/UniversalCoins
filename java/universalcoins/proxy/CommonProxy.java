package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.blocks.BlockBase;
import universalcoins.blocks.BlockCardStation;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockVendor;
import universalcoins.blocks.BlockVendorFrame;
import universalcoins.items.ItemBlockVendor;
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
	public static Block blockVendorFrame;
	public static Block blockCardStation;
	public static Block blockBase;
	public static Block blockSafe;
	
	
	public void registerBlocks() {
		blockTradeStation = new BlockTradeStation();
		blockVendor = new BlockVendor(Vending.supports);
		blockVendorFrame = new BlockVendorFrame();
		blockCardStation = new BlockCardStation();
		blockBase = new BlockBase();
		blockSafe = new BlockSafe();
	}
	
	public void registerItems() {
		itemCoin = new ItemCoin();
		itemSmallCoinStack = new ItemSmallCoinStack();
		itemLargeCoinStack = new ItemLargeCoinStack();
		itemSmallCoinBag = new ItemSmallCoinBag();
		itemLargeCoinBag = new ItemLargeCoinBag();
		itemUCCard = new ItemUCCard();
		itemEnderCard = new ItemEnderCard();
		itemSeller = new ItemSeller();
		itemVendorWrench = new ItemVendorWrench();
	}

	public void registerRenderers() {
		//blank since we don't do anything on the server
	}

}
