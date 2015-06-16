package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.blocks.BlockBandit;
import universalcoins.blocks.BlockBase;
import universalcoins.blocks.BlockCardStation;
import universalcoins.blocks.BlockPackager;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockSignal;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemLargeCoinBag;
import universalcoins.items.ItemLargeCoinStack;
import universalcoins.items.ItemPackage;
import universalcoins.items.ItemSeller;
import universalcoins.items.ItemSmallCoinBag;
import universalcoins.items.ItemSmallCoinStack;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemVendorWrench;

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
	//public static Item itemUCSign;
	//public static Item itemLinkCard;
	public static Item itemPackage;
	
	public static Block blockTradeStation;
	//public static Block blockVendor;
	//public static Block blockVendorFrame;
	public static Block blockCardStation;
	public static Block blockBase;
	public static Block blockSafe;
	//public static Block standing_ucsign;
	//public static Block wall_ucsign;
	public static Block blockBandit;
	public static Block blockSignal;
	public static Block blockPackager;
	
	
	public void registerBlocks() {
		blockTradeStation = new BlockTradeStation().setUnlocalizedName("blockTradeStation");
		//blockVendor = new BlockVendor(Vending.supports).setUnlocalizedName("blockVendor");
		//blockVendorFrame = new BlockVendorFrame().setUnlocalizedName("blockVendorFrame");
		blockCardStation = new BlockCardStation().setUnlocalizedName("blockCardStation");
		blockBase = new BlockBase().setUnlocalizedName("blockBase");
		blockSafe = new BlockSafe().setUnlocalizedName("blockSafe");
		//standing_ucsign = new BlockUCSign(TileUCSign.class, true).setUnlocalizedName("standing_ucsign");
		//wall_ucsign = new BlockUCSign(TileUCSign.class, false).setUnlocalizedName("wall_ucsign");
		blockBandit = new BlockBandit().setUnlocalizedName("blockBandit");
		blockSignal = new BlockSignal().setUnlocalizedName("blockSignal");
		blockPackager = new BlockPackager().setUnlocalizedName("blockPackager");
		
		GameRegistry.registerBlock(blockTradeStation, blockTradeStation.getUnlocalizedName().substring(5));
		//GameRegistry.registerBlock(blockVendor, ItemBlockVendor.class, blockVendor.getUnlocalizedName().substring(5));
		//GameRegistry.registerBlock(blockVendorFrame, blockVendorFrame.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockCardStation, blockCardStation.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockBase, blockBase.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockSafe, blockSafe.getUnlocalizedName().substring(5));
		//GameRegistry.registerBlock(standing_ucsign, standing_ucsign.getUnlocalizedName().substring(5));
		//GameRegistry.registerBlock(wall_ucsign, wall_ucsign.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockBandit, blockBandit.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockSignal, blockSignal.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockPackager, blockPackager.getUnlocalizedName().substring(5));
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
		//itemUCSign = new ItemUCSign().setUnlocalizedName("itemUCSign");
		//itemLinkCard = new ItemLinkCard().setUnlocalizedName("itemLinkCard");
		itemPackage = new ItemPackage().setUnlocalizedName("itemPackage");
		
		GameRegistry.registerItem(itemCoin, itemCoin.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemSmallCoinStack, itemSmallCoinStack.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemLargeCoinStack, itemLargeCoinStack.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemSmallCoinBag, itemSmallCoinBag.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemLargeCoinBag, itemLargeCoinBag.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemUCCard, itemUCCard.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemEnderCard, itemEnderCard.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemSeller, itemSeller.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemVendorWrench, itemVendorWrench.getUnlocalizedName().substring(5));
		//GameRegistry.registerItem(itemUCSign, itemUCSign.getUnlocalizedName().substring(5));
		//GameRegistry.registerItem(itemLinkCard, itemLinkCard.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(itemPackage, itemPackage.getUnlocalizedName().substring(5));
	}

	public void registerRenderers() {
		//blank since we don't do anything on the server
	}

}
