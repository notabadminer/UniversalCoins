package universalcoins.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class UCItemPricer {

	private static final UCItemPricer instance = new UCItemPricer();

	private static Map<String, Integer> ucPriceMap = new HashMap<String, Integer>(0);
	private static Map<String, String> ucModnameMap = new HashMap<String, String>(0);
	private static String configDir = FMLInjectionData.data()[6] + "/config/universalcoins/";
	private Random random = new Random();

	public static UCItemPricer getInstance() {
		return instance;
	}

	private UCItemPricer() {

	}

	public void loadConfigs() {
		if (!new File(configDir).exists()) {
			// FMLLog.info("Universal Coins: Loading default prices");
			buildPricelistHashMap();
			try {
				loadDefaults();
			} catch (IOException e) {
				FMLLog.warning("Universal Coins: Failed to load default configs");
				e.printStackTrace();
			}
			updateOreDictionaryPrices();
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
			priceResource = UCItemPricer.class.getResourceAsStream(configList[i]);
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
			if (ucPriceMap.get(tempData[0]) != null && tempData.length == 2) {
				ucPriceMap.put(tempData[0], Integer.valueOf(tempData[1]));
			}
		}
	}

	private void buildPricelistHashMap() {
		ArrayList<String> itemsDiscovered = new ArrayList();
		Iterable<String> list = (Iterable<String>) Item.itemRegistry.getKeys();

		for (String itemKey : list) {
			String[] tempModName = itemKey.split("\\W", 3);
			// pass the first value as modname
			String modName = tempModName[0];
			Item testItem = (Item) Item.itemRegistry.getObject(itemKey);
			if (testItem != null && testItem.getHasSubtypes()) {
				ItemStack testStack = new ItemStack(testItem, 1, 0);
				ItemStack baseStack = new ItemStack(testItem, 1, 0);
				ItemStack previousStack = new ItemStack(testItem, 1, 0);
				for (int itemDamage = 0; itemDamage < 16; itemDamage++) {
					testStack = new ItemStack(testItem, 1, itemDamage);
					String testName = "";
					String baseName = "";
					String previousName = "";
					try {
						testName = testStack.getDisplayName();
						baseName = baseStack.getDisplayName();
						previousName = previousStack.getDisplayName();
					} catch (Exception e) {
						break;
					}
					if (itemDamage == 0 || testName != null && !testName.matches("") && !baseName.equals(testName)
							&& !previousName.equals(testName)) {
						previousStack = testStack;
						itemsDiscovered.add(testStack.getUnlocalizedName() + "." + itemDamage);
						continue;
					} else {
						break;
					}
				}
			} else {
				if (!itemsDiscovered.contains(testItem.getUnlocalizedName() + ".0")) {
					itemsDiscovered.add(testItem.getUnlocalizedName() + ".0");
				}
			}

			// iterate through the items and update the hashmaps
			for (String name1 : itemsDiscovered) {
				// update ucModnameMap with items found
				ucModnameMap.put(name1, modName);
				// update ucPriceMap with initial values
				if (!ucPriceMap.containsKey(name1)) {
					ucPriceMap.put(name1, -1);
				}
			}
			// clear this variable so we can use it next round
			itemsDiscovered.clear();
		}

	}

	private void updateOreDictionaryPrices() {
		// parse oredictionary
		for (String ore : OreDictionary.getOreNames()) {
			ucModnameMap.put(ore, "oredictionary");
			if (!ucPriceMap.containsKey(ore)) {
				// check ore to see if any of the types has a price, use it
				// if true
				ArrayList test = OreDictionary.getOres(ore);
				int itemValue = -1;
				for (int j = 0; j < test.size(); j++) {
					ItemStack itemStack = ((ItemStack) test.get(j));
					if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						itemStack.setItemDamage(0);
					}
					int subItemValue = getItemPrice(itemStack);
					if (subItemValue > 0) {
						itemValue = subItemValue;
					}
				}
				ucPriceMap.put(ore, itemValue);
			}
		}
	}

	private void loadPricelists() throws IOException {
		// search config file folder for files
		File folder = new File(configDir);
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

	private boolean writePriceLists() {
		// writing pricelists takes a while so we start a thread and let it work
		// in the background.
		try {
			priceListWriter();
		} catch (IOException e) {
			FMLLog.warning("Universal Coins: Failed to create config file");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void priceListWriter() throws IOException {
		// long startTime = System.currentTimeMillis();

		Set<Entry<String, String>> set = ucModnameMap.entrySet();
		List<Entry<String, String>> list = new ArrayList<Entry<String, String>>(set);
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		Path pathToFile = Paths.get(configDir + list.get(0).getValue());
		Files.createDirectories(pathToFile.getParent());

		File csvFile = new File(configDir + list.get(0).getValue() + ".cfg");
		if (csvFile.exists()) {
			csvFile.delete();
			csvFile.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(csvFile));
		String previousModName = list.get(0).getValue();
		for (Entry<String, String> entry : list) {
			String modName = entry.getValue();
			if (!modName.matches(previousModName)) {
				previousModName = modName;
				out.flush();
				out.close();
				// move to next file
				csvFile = new File(configDir + entry.getValue() + ".cfg");
				if (csvFile.exists()) {
					csvFile.delete();
					csvFile.createNewFile();
				}
				out = new BufferedWriter(new FileWriter(csvFile));
			}
			int price = ucPriceMap.get(entry.getKey());
			out.write(entry.getKey() + "=" + price);
			out.newLine();
		}
		out.flush();
		out.close();
		// long endTime = System.currentTimeMillis();
		// FMLLog.info("File writes took " + (endTime - startTime) + "
		// milliseconds");
	}

	public static int getItemPrice(ItemStack itemStack) {
		if (itemStack == null) {
			return -1;
		}
		int itemPrice = -1;
		String itemName = null;
		if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
			itemStack.setItemDamage(0);
		}
		try {
			if (itemStack.isItemStackDamageable()) {
				itemName = itemStack.getUnlocalizedName() + ".0";

			} else {
				itemName = itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
			}
		} catch (Exception e) {
			FMLLog.warning("Universal Coins: Failed to retrieve price for " + itemStack);
		}
		if (ucPriceMap.get(itemName) != null) {
			itemPrice = ucPriceMap.get(itemName);
		}
		// lookup item in oreDictionary if not priced
		if (itemPrice == -1) {
			int[] id = OreDictionary.getOreIDs(itemStack);
			if (id.length > 0) {
				itemName = OreDictionary.getOreName(id[0]);
				if (ucPriceMap.get(itemName) != null) {
					itemPrice = ucPriceMap.get(itemName);
				}
			}
		}
		return itemPrice;
	}

	public boolean setItemPrice(ItemStack itemStack, int price) {
		if (itemStack == null) {
			return false;
		}
		String itemName = itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
		// get modName to add to mapping
		String itemRegistryKey = Item.itemRegistry.getNameForObject(itemStack.getItem());
		String[] tempModName = itemRegistryKey.split("\\W", 3);
		// pass the first value as modname
		String modName = tempModName[0];
		ucModnameMap.put(itemName, modName);
		// update price
		ucPriceMap.put(itemName, price);
		return true;
	}

	public void updatePriceLists() {
		// delete old configs
		File folder = new File(configDir);
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
		updateOreDictionaryPrices();
		autoPriceCraftedItems();
		autoPriceSmeltedItems();
		// write new configs
		writePriceLists();
	}

	public boolean savePriceLists() {
		// delete old configs
		File folder = new File(configDir);
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
		return writePriceLists();
	}

	public boolean resetDefaults() {
		try {
			loadDefaults();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

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
				// oredictionary entries do not include metadata value
				// we need to check for this
				if (!Character.isDigit(keyName.charAt(keyName.length() - 1))) {
					ArrayList test = OreDictionary.getOres(keyName);
					int itemValue = -1;
					for (int j = 0; j < test.size(); j++) {
						ItemStack oreDictionaryStack = (ItemStack) test.get(j);
						int subItemValue = getItemPrice(oreDictionaryStack);
						if (subItemValue > 0) {
							itemValue = subItemValue;
							return oreDictionaryStack;
						}
					}
				} else {
					// split string into item name and meta
					String itemName = keyName.substring(0, keyName.length() - 2);
					int itemMeta = Integer.valueOf(keyName.substring(keyName.length() - 1));
					Item item = (Item) Item.itemRegistry.getObject(itemName);
					if (item != null) {
						stack = new ItemStack(item, 1, itemMeta);
					}
				}
			}
		}
		return stack;
	}

	private void autoPriceCraftedItems() {
		List<IRecipe> allrecipes = new ArrayList<IRecipe>(CraftingManager.getInstance().getRecipeList());
		boolean priceUpdate = false;

		// we rerun multiple times if needed since recipe components might be
		// priced in previous runs
		do {
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
						break;
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
		} while (priceUpdate == true);
	}

	public static ArrayList<ItemStack> getRecipeInputs(IRecipe recipe) {
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
							ItemStack stack = (ItemStack) test.get(j);
							if (getItemPrice(stack) > 0) {
								recipeInputs.add(stack);
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
						ItemStack stack = (ItemStack) test.get(j);
						if (getItemPrice(stack) > 0) {
							recipeInputs.add(stack);
							arrayListHasPricedItem = true;
							break;
						}
					}
					// everything is invalid, just add one
					if (!arrayListHasPricedItem && test.size() > 0) {
						recipeInputs.add((ItemStack) test.get(0));
					}
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
		Map<ItemStack, ItemStack> recipes = (Map<ItemStack, ItemStack>) FurnaceRecipes.smelting().getSmeltingList();
		for (Entry<ItemStack, ItemStack> recipe : recipes.entrySet()) {
			ItemStack input = recipe.getKey();
			ItemStack output = recipe.getValue();
			String inputName = "";
			String outputName = "";
			try {
				inputName = input.getUnlocalizedName();
				outputName = output.getUnlocalizedName();
			} catch (Exception e) {
				continue;
			}
			if (ucPriceMap.get(inputName + "." + input.getItemDamage()) != null
					&& ucPriceMap.get(outputName + "." + input.getItemDamage()) != null) {
				int inputValue = ucPriceMap.get(inputName + "." + input.getItemDamage());
				int outputValue = ucPriceMap.get(outputName + "." + input.getItemDamage());
				if (inputValue != -1 && outputValue == -1) {
					ucPriceMap.put(outputName + "." + input.getItemDamage(), inputValue + 2);
				} else if (outputValue != -1 && inputValue == -1) {
					ucPriceMap.put(inputName + "." + output.getItemDamage(), outputValue - 2);
				}
			}
		}
	}

	public static Map<String, Integer> getUcPriceMap() {
		return ucPriceMap;
	}
}
