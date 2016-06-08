package universalcoins.util;

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


  @Override
  public int getItemPrice(ItemStack itemStack) {
    int basePrice = super.getItemPrice(itemStack);
    Integer qi = ucQuantityMap.get(itemStack.getUnlocalizedName());
    int q = qi == null ? 0 : qi.intValue();

    double slope = (basePrice+0.0) / UniversalCoins.pricerThreshold;
    double price = basePrice - slope * q;

    return (int) price;
  }

  @Override
  public int getAffordable(ItemStack itemStack, long availableCoins) {
    return 0;
  }

  @Override
  public void reportAddToMarket(ItemStack itemStack, int q) {}

}
