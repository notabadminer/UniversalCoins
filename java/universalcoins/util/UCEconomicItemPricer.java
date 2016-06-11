package universalcoins.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalcoins.UniversalCoins;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Blake Curry(Faxn) on 6/8/2016.
 */
public class UCEconomicItemPricer extends UCStaticItemPricer implements UCItemPricer {

  //Quantity is market surplus, number sold - number bought. It can be negative.
  private Map<String, Integer> ucQuantityMap = new HashMap<String, Integer>();

  private int qMarket(ItemStack itemStack){
    Integer qi = ucQuantityMap.get(itemStack.getUnlocalizedName());
    return qi == null ? 0 : qi.intValue();
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

  @Override
  public void reportAddToMarket(ItemStack itemStack, int q) {
    int qMarket = qMarket(itemStack);
    qMarket += q;
    ucQuantityMap.put(itemStack.getUnlocalizedName(), qMarket);
  }

}
