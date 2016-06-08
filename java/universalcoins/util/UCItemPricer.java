package universalcoins.util;

import net.minecraft.item.ItemStack;

import java.util.Collection;


/**
 * Created by campb on 6/8/2016.
 */
public interface UCItemPricer {
  void loadConfigs();

  int getItemPrice(ItemStack itemStack, int q);

  int getItemPrice(ItemStack itemStack);

  boolean setItemPrice(ItemStack itemStack, int price);

  void updatePriceLists();

  void savePriceLists();

  void resetDefaults();

  ItemStack getRandomPricedStack();

  Collection<String> getPricedItems();

  int getAffordable(ItemStack itemStack, long availableCoins);

  void reportAddToMarket(ItemStack itemStack, int q);

}
