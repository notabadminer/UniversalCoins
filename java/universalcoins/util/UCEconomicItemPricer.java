package universalcoins.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Blake Curry(Faxn) on 6/8/2016.
 */
public class UCEconomicItemPricer extends UCStaticItemPricer implements UCItemPricer {


  private String getKey(ItemStack itemStack){
    return itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
  }

  //Quantity is market surplus, number sold - number bought. It can be negative.
  private int qMarket(ItemStack itemStack){
    UCWorldData wd = UCWorldData.getInstance();
    return wd.getInteger(getKey(itemStack));
  }

  @Override
  public int getItemPrice(ItemStack itemStack) {
    int basePrice = super.getItemPrice(itemStack);

    double slope = (basePrice+0.0) / UniversalCoins.pricerThreshold;
    double price = basePrice - slope * qMarket(itemStack);

    return (int) price;
  }

  @Override
  public int getItemPrice(ItemStack itemStack, int q){
    int basePrice = super.getItemPrice(itemStack);
    int qMarket = qMarket(itemStack);
    double slope = (basePrice+0.0) / UniversalCoins.pricerThreshold;

    double price1 = basePrice - slope * (qMarket);
    double price2 = basePrice - slope * (qMarket + q);
    double p = (price1 + price2)/2;

    return (int) p * Math.abs(q);

  }

  @Override
  public int getAffordable(ItemStack itemStack, long funds) {
    return 0;
  }

  //TODO: Race condition? 2 TradeStations buying or selling at once may result in q being recorded wrong. This is probably abuseable.
  //TODO: Quantity and ItemPrice entries aren't guaranteed to be 1:1
  //TODO: Quantity should address oreDictionary.
  @Override
  public void reportAddToMarket(ItemStack itemStack, int q) {
    UCWorldData wd = UCWorldData.getInstance();

    int qMarket = qMarket(itemStack);
    qMarket += q;
    wd.setData(getKey(itemStack), qMarket);

  }

  @Override
  public void loadConfigs() {
    super.loadConfigs();
  }

  @Override
  public void savePriceLists() {
    super.savePriceLists();
  }
}
