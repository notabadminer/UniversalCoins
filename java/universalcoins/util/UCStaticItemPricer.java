package universalcoins.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class UCStaticItemPricer implements UCItemPricer {

	private static Map<String, Integer> ucPriceMap = new HashMap<String, Integer>(0);
	private static Map<String, String> ucModnameMap = new HashMap<String, String>(0);
	private static String configPath = FMLInjectionData.data()[6] + "/config/universalcoins/";
	private Random random = new Random();



	public UCStaticItemPricer() {

	}

	@Override
	public void loadConfigs() {
		if (!new File(configPath).exists()) {
			// FMLLog.info("Universal Coins: Loading default prices");
			buildPricelistHashMap();
			try {
				loadDefaults();
			} catch (IOException e) {
				FMLLog.warning("Universal Coins: Failed to load default configs");
				e.printStackTrace();
			}
			autoPriceCraftedItems();
			autoPriceSmeltedItems();
			writePriceLists();
		} else {
			try {
				loadPricelists();
			} catch (IOException e) {
				FMLLog.warning("Universal Coins: Failed to load config files");
				e.printStackTrace();
			}
		}
	}

	private void loadDefaults() throws IOException {
		String[] configList = { "pricelists/minecraft.cfg" };
		InputStream priceResource;
		// load those files into hashmap(ucPriceMap)
		for (int i = 0; i < configList.length; i++) {
			priceResource = UCStaticItemPricer.class.getResourceAsStream(configList[i]);
			if (priceResource == null) {
				return;
			}
			String priceString = convertStreamToString(priceResource);
			processDefaultConfigs(priceString);
		}
	}

	private String convertStreamToString(java.io.InputStream is) {
		// Thanks to Pavel Repin on StackOverflow.
		java.util.Scanner scanner = new java.util.Scanner(is);
		java.util.Scanner s = scanner.useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		scanner.close();
		return result;
	}

	private void processDefaultConfigs(String priceString) {
		StringTokenizer tokenizer = new StringTokenizer(priceString, "\n\r", false);
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			String[] tempData = token.split("=");
			// FMLLog.info("Universal Coins: Updating UCPricelist: " +
			// tempData[0] + "=" +
			// Integer.valueOf(tempData[1]));
			// We'll update the prices of all the items and not add all the
			// default prices to the config folder if the
			// mods are not present
			if (ucPriceMap.get(tempData[0]) != null && tempData.length == 2) {
				ucPriceMap.put(tempData[0], Integer.valueOf(tempData[1]));
			}
		}
	}

	private void buildPricelistHashMap() {
		ArrayList<ItemStack> itemsDiscovered = new ArrayList<ItemStack>();
		Object[] itemSet = Item.REGISTRY.getKeys().toArray();

		for (Object itemObject : itemSet) {
			String item = itemObject.toString();
			// pass the itemkey to a temp variable after splitting on
			// non-alphanumeric values
			String[] tempModName = item.split("\\W", 3);
			// pass the first value as modname
			String modName = tempModName[0];
			if (item != null) {
				Item test = (Item) Item.REGISTRY.getObject(new ResourceLocation(item));
				// check for meta values so we catch all items
				// Iterate through damage values and add them if unique
				ItemStack baseStack = new ItemStack(test, 1, 0);
				ItemStack previousStack = new ItemStack(test, 1, 0);
				boolean newItem = true;
				int itemDamage = 0;
				while (newItem == true) {
					ItemStack testStack = new ItemStack(test, 1, itemDamage);
					String baseName=null, testName=null, previousName=null;
					try{
						baseName = baseStack.getDisplayName();
						previousName = previousStack.getDisplayName();
						testName = testStack.getDisplayName();
					}catch (java.lang.IndexOutOfBoundsException e){
						// Some mods manage to throw an exception here.
						// Lets try to be compatible
						e.printStackTrace();
						itemDamage++;
						break;
					}

					if (itemDamage == 0 || !baseName.equals(testName)
							&& !previousName.equals(testName)) {
						previousStack = testStack;
						try {
							String name = testName + "." + itemDamage;
							if (!itemsDiscovered.contains(name)) {
								itemsDiscovered.add(testStack);
							}
						} catch (Throwable ex) {
							// set false to exit as something has gone wrong.
							newItem = false;
						}
					} else {
						newItem = false;
					}
					// Botania will make infinite numbers of these if we try.
					// botania:pavement1, botania:pavement2, botania:pavement3
					if(testName.contains(""+itemDamage)){
						newItem = false;
					}

					itemDamage++;
				}
			}

			// parse oredictionary
			for (String ore : OreDictionary.getOreNames()) {
				ucModnameMap.put(ore, "oredictionary");
				if (!ucPriceMap.containsKey(ore)) {
					// check ore to see if any of the types has a price, use it
					// if true
					List<ItemStack> test = OreDictionary.getOres(ore);
					int itemValue = -1;
					for (int j = 0; j < test.size(); j++) {
						int subItemValue = getItemPrice((ItemStack) test.get(j));
						if (subItemValue > 0) {
							itemValue = subItemValue;
						}
					}
					ucPriceMap.put(ore, itemValue);
				}
			}

			// iterate through the items and update the hashmaps
			for (ItemStack itemstack : itemsDiscovered) {
				try {
					// update ucModnameMap with items found
					ucModnameMap.put(itemstack.getUnlocalizedName() + "." + itemstack.getItemDamage(), modName);
					// update ucPriceMap with initial values
					if (!ucPriceMap.containsKey(itemstack.getUnlocalizedName() + "." + itemstack.getItemDamage())) {
						ucPriceMap.put(itemstack.getUnlocalizedName() + "." + itemstack.getItemDamage(), -1);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			// clear this variable so we can use it next round
			itemsDiscovered.clear();
		}

	}

	private void loadPricelists() throws IOException {
		// search config file folder for files
		File folder = new File(configPath);
		File[] configList = folder.listFiles();
		// load those files into hashmap(UCPriceMap)
		for (int i = 0; i < configList.length; i++) {
			if (configList[i].isFile()) {
				// FMLLog.info("Universal Coins: Loading Pricelist: " +
				// configList[i]);
				BufferedReader br = new BufferedReader(new FileReader(configList[i]));
				String tempString = "";
				String[] modName = configList[i].getName().split("\\.");
				while ((tempString = br.readLine()) != null) {
					if (tempString.startsWith("//") || tempString.startsWith("#")) {
						continue; // we have a comment. skip it
					}
					String[] tempData = tempString.split("=");
					if (tempData.length < 2) {
						// something is wrong with this line
						FMLLog.warning("Universal Coins: Error detected in pricelist: " + configList[i].getName() + " "
								+ tempString + " is invalid input");
						continue;
					}
					int itemPrice = -1;
					try {
						itemPrice = Integer.valueOf(tempData[1]);
					} catch (NumberFormatException e) {
						FMLLog.warning("Universal Coins: Error detected in pricelist: " + configList[i].getName() + " "
								+ tempString + " is invalid input");
					}
					ucPriceMap.put(tempData[0], itemPrice);
					ucModnameMap.put(tempData[0], modName[0]);
				}
				br.close();
			}
		}
	}

	private void writePriceLists() {
		// write config set from item hashmap
		Set set = ucPriceMap.entrySet();
		Iterator i = set.iterator();
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			String keyname = (String) me.getKey();
			String modname = ucModnameMap.get(keyname) + ".cfg";
			Path pathToFile = Paths.get(configPath + modname);
			try {
				Files.createDirectories(pathToFile.getParent());
			} catch (IOException e) {
				FMLLog.warning("Universal Coins: Failed to create config file folders");
			}
			File modconfigfile = new File(configPath + modname);
			if (!modconfigfile.exists()) {
				try {
					modconfigfile.createNewFile();
				} catch (IOException e) {
					FMLLog.warning("Universal Coins: Failed to create config file");
				}
			}
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(modconfigfile, true)));
				out.println(me.getKey() + "=" + me.getValue());
				out.close();
			} catch (IOException e) {
				FMLLog.warning("Universal Coins: Failed to append to config file");
			}

		}
	}

	@Override
	public int getItemPrice(ItemStack itemStack, int q) {
		if (itemStack == null) {
			// FMLLog.warning("itemstack is null");
			return -1;
		}
		int ItemPrice = -1;
		String itemName = null;
		try {
			itemName = itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
		} catch (Exception e) {
			// fail silently
		}
		if (ucPriceMap.get(itemName) != null) {
			ItemPrice = ucPriceMap.get(itemName);
		}
		// lookup item in oreDictionary if not priced
		if (ItemPrice == -1) {
			int[] id = OreDictionary.getOreIDs(itemStack);
			if (id.length > 0) {
				itemName = OreDictionary.getOreName(id[0]);
				if (ucPriceMap.get(itemName) != null) {
					ItemPrice = ucPriceMap.get(itemName);
				}
			}
		}
		return ItemPrice * q;
	}

  @Override
  public int getItemPrice(ItemStack itemStack) {
    return getItemPrice(itemStack, 1);
  }

  @Override
	public boolean setItemPrice(ItemStack itemStack, int price) {
		if (itemStack == null) {
			return false;
		}
		int itemStackMeta = itemStack.getMetadata();
		if (itemStack.isItemStackDamageable()) {
			// override for damaged items
			itemStackMeta = 0;
		}
		String itemName = itemStack.getUnlocalizedName();
		// get modName to add to mapping
		String itemRegistryKey = Item.REGISTRY.getNameForObject(itemStack.getItem()).toString();
		String[] tempModName = itemRegistryKey.split("\\W", 3);
		// pass the first value as modname
		String modName = tempModName[0];
		ucModnameMap.put(itemName + "." + itemStackMeta, modName);
		// update price
		ucPriceMap.put(itemName + "." + itemStackMeta, price);
		return true;
	}

	@Override
	public void updatePriceLists() {
		// delete old configs
		File folder = new File(configPath);
		// cleanup
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}
		// update mod itemlist
		buildPricelistHashMap();
		// update prices
		autoPriceCraftedItems();
		// write new configs
		writePriceLists();
	}

	@Override
	public void savePriceLists() {
		// delete old configs
		File folder = new File(configPath);
		// cleanup
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}
		// write new configs
		writePriceLists();
	}

	@Override
	public void resetDefaults() {
		try {
			loadDefaults();
		} catch (IOException e) {
			// fail quietly
		}
	}

	@Override
	public ItemStack getRandomPricedStack() {
		List keys = new ArrayList(ucPriceMap.keySet());
		ItemStack stack = null;
		while (stack == null) {
			String keyName = keys.get(random.nextInt(keys.size())).toString();
			int price = 0;
			if (ucPriceMap.get(keyName) != null) {
				price = ucPriceMap.get(keyName);
			}
			if (price > 0) {
				if (keyName.startsWith("tile.") || keyName.startsWith("item.")) {
					keyName = keyName.substring(5);
				}
				// split string into item name and meta
				String itemName = keyName.substring(0, keyName.length() - 2);
				int itemMeta = Integer.valueOf(keyName.substring(keyName.length() - 1));
				Item item = (Item) Item.REGISTRY.getObject(new ResourceLocation(itemName));
				if (item != null) {
					stack = new ItemStack(item, 1, itemMeta);
				}
			}
		}
		return stack;
	}

	@Override
	public Collection<String> getPricedItems() {
		return ucPriceMap.keySet();
	}

	@Override
	public int getAffordable(ItemStack itemStack, long availableCoins) {
		int p = getItemPrice(itemStack, 1);
		return (int) availableCoins/p;
	}

	@Override
	public void reportAddToMarket(ItemStack itemStack, int q) {}

	private void autoPriceCraftedItems() {
		List<IRecipe> allrecipes = new ArrayList<IRecipe>(CraftingManager.getInstance().getRecipeList());
		boolean priceUpdate = false;

		while (priceUpdate == true) {
			priceUpdate = false;
			for (IRecipe irecipe : allrecipes) {
				int itemCost = 0;
				boolean validRecipe = true;
				ItemStack output = irecipe.getRecipeOutput();
				if (output == null) {
					continue;
				}
				if (getItemPrice(output) != -1) {
					continue;
				}
				List recipeItems = getRecipeInputs(irecipe);
				for (int i = 0; i < recipeItems.size(); i++) {
					ItemStack stack = (ItemStack) recipeItems.get(i);
					if (getItemPrice(stack) != -1) {
						itemCost += getItemPrice(stack);
					} else {
						validRecipe = false;
					}
				}
				if (validRecipe && itemCost > 0) {
					priceUpdate = true;
					if (output.stackSize > 1) {
						itemCost = itemCost / output.stackSize;
					}
					try {
						setItemPrice(output, itemCost);
					} catch (Exception e) {
						FMLLog.warning("Universal Coins Autopricer: Failed to set item price.");
					}
				}
			}
		}
	}

	public List<ItemStack> getRecipeInputs(IRecipe recipe) {
		ArrayList<ItemStack> recipeInputs = new ArrayList<ItemStack>();
		if (recipe instanceof ShapedRecipes) {
			ShapedRecipes shapedRecipe = (ShapedRecipes) recipe;
			for (int i = 0; i < shapedRecipe.recipeItems.length; i++) {
				if (shapedRecipe.recipeItems[i] instanceof ItemStack) {
					ItemStack itemStack = shapedRecipe.recipeItems[i].copy();
					if (itemStack.stackSize > 1) {
						itemStack.stackSize = 1;
					}
					recipeInputs.add(itemStack);
				}
			}
		} else if (recipe instanceof ShapelessRecipes) {
			ShapelessRecipes shapelessRecipe = (ShapelessRecipes) recipe;
			for (Object object : shapelessRecipe.recipeItems) {
				if (object instanceof ItemStack) {
					ItemStack itemStack = ((ItemStack) object).copy();
					if (itemStack.stackSize > 1) {
						itemStack.stackSize = 1;
					}
					recipeInputs.add(itemStack);
				}
			}
		} else if (recipe instanceof ShapedOreRecipe) {
			ShapedOreRecipe shapedOreRecipe = (ShapedOreRecipe) recipe;
			for (int i = 0; i < shapedOreRecipe.getInput().length; i++) {
				if (shapedOreRecipe.getInput()[i] instanceof ArrayList) {
					ArrayList test = (ArrayList) shapedOreRecipe.getInput()[i];
					if (test.size() > 0) {
						boolean arrayListHasPricedItem = false;
						for (int j = 0; j < test.size(); j++) {
							if (getItemPrice((ItemStack) test.get(j)) > 0) {
								recipeInputs.add((ItemStack) test.get(j));
								arrayListHasPricedItem = true;
								break;
							}
						}
						// everything is invalid, just add one
						if (!arrayListHasPricedItem) {
							recipeInputs.add((ItemStack) test.get(0));
						}
					}
				} else if (shapedOreRecipe.getInput()[i] instanceof ItemStack) {
					ItemStack itemStack = ((ItemStack) shapedOreRecipe.getInput()[i]).copy();
					if (itemStack.stackSize > 1) {
						itemStack.stackSize = 1;
					}
					recipeInputs.add(itemStack);
				}
			}
		} else if (recipe instanceof ShapelessOreRecipe) {
			ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe) recipe;
			for (Object object : shapelessOreRecipe.getInput()) {
				if (object instanceof ArrayList) {
					ArrayList test = (ArrayList) object;
					boolean arrayListHasPricedItem = false;
					for (int j = 0; j < test.size(); j++) {
						if (getItemPrice((ItemStack) test.get(j)) > 0) {
							recipeInputs.add((ItemStack) test.get(j));
							arrayListHasPricedItem = true;
							break;
						}
					}
					// everything is invalid, just add one
					if (!arrayListHasPricedItem && test.size() > 0)
						recipeInputs.add((ItemStack) test.get(0));
				} else if (object instanceof ItemStack) {
					ItemStack itemStack = ((ItemStack) object).copy();
					if (itemStack.stackSize > 1) {
						itemStack.stackSize = 1;
					}
					recipeInputs.add(itemStack);
				}
			}
		}
		return recipeInputs;
	}

	private void autoPriceSmeltedItems() {
		Map<ItemStack, ItemStack> recipes = (Map<ItemStack, ItemStack>) FurnaceRecipes.instance().getSmeltingList();
		for (Entry<ItemStack, ItemStack> recipe : recipes.entrySet()) {
			ItemStack input = recipe.getKey();
			ItemStack output = recipe.getValue();
			if (ucPriceMap.get(input.getUnlocalizedName() + "." + input.getItemDamage()) != null) {
				int inputValue = ucPriceMap.get(input.getUnlocalizedName() + "." + input.getItemDamage());
				int outputValue = ucPriceMap.get(output.getUnlocalizedName() + "." + output.getItemDamage());
				if (inputValue != -1 && outputValue == -1) {
					ucPriceMap.put(output.getUnlocalizedName() + "." + output.getItemDamage(), inputValue + 2);
				}
			}
		}
	}

	public static Map<String, Integer> getUcPriceMap() {
		return ucPriceMap;
	}
}
