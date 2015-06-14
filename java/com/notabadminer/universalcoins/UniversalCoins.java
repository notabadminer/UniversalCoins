package com.notabadminer.universalcoins;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import com.notabadminer.universalcoins.items.ItemCoin;
import com.notabadminer.universalcoins.proxy.CommonProxy;

/**
 * UniversalCoins, Sell all your extra blocks and buy more!!! Create a trading economy, jobs, whatever.
 * 
 * @author notabadminer
 * 
 **/

@Mod(modid = UniversalCoins.MODID, name = UniversalCoins.NAME, version = UniversalCoins.VERSION,
acceptedMinecraftVersions = "[1.8.0]", dependencies = "required-after:Forge@[11.14.3.1446,)")


public class UniversalCoins
{
    public static final String MODID = "universalcoins";
    public static final String NAME = "UniversalCoins";
    public static final String VERSION = "1.8.0-100";
    public static SimpleNetworkWrapper snw;
    
    public static CreativeTabs tabUniversalCoins = new UCTab("tabUniversalCoins");
    
    @Instance(UniversalCoins.MODID)
    public static UniversalCoins instance;
    
    @SidedProxy(clientSide="com.notabadminer.universalcoins.proxy.ClientProxy", serverSide="com.notabadminer.universalcoins.proxy.CommonProxy")
	public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		//TODO load variables
		config.save();
		
		proxy.registerItems();
		proxy.registerItems();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	//network packet handling
	    snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    	
	    proxy.registerRenderers();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	
    }
}
