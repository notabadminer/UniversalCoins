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

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

public class UCItemPricer {

	private static final UCItemPricer instance = new UCItemPricer();

	private static Map<String, Integer> ucPriceMap = new HashMap<String, Integer>(0);
	private static Map<String, String> ucModnameMap = new HashMap<String, String>(0);
	private static String configPath = FMLInjectionData.data()[6] + "/config/universalcoins/";
	private Random random = new Random();

	public static UCItemPricer getInstance() {
		return instance;
	}

	private UCItemPricer() {

	}

	public void loadConfigs() {
		if (!new File(configPath).exists()) {
			// FMLLog.info("Universal Coins: Loading default prices");
			updateItems();
			updatePotions();
			updateOreDictionary();
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
		String[] configList = { "pricelists/minecraft.csv" };
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
				String[] modName = configList[i].getName().split("\\.(?=[^\\.]+$)");
				while ((tempString = br.readLine()) != null) {
					if (tempString.startsWith("//") || tempString.startsWith("#")) {
						continue; // we have a comment. skip it
					}
					String[] tempData = tempString.split("=");
					if (tempData.length < 2) {
						// something is wrong with this line
						FMLLog.warning("Universal Coins: ERROR: line containing " + tempString + " in "
								+ configList[i].getName() + " is invalid");
						continue;
					}
					int itemPrice = -1;
					try {
						itemPrice = Integer.valueOf(tempData[1]);
					} catch (NumberFormatException e) {
						FMLLog.warning("Universal Coins: ERROR: line containing " + tempString + " in "
								+ configList[i].getName() + " is invalid");
					}
					ucPriceMap.put(tempData[0], itemPrice);
					ucModnameMap.put(tempData[0], modName[0]);
				}
				br.close();
			}
		}
	}

	private void writePriceLists() {
		// writing pricelists takes a while so we start a thread and let it work
		// in the background.
		Runnable r = new Runnable() {
			public void run() {
				try {
					priceListWriter();
				} catch (IOException e) {
					FMLLog.warning("Universal Coins: Failed to create config file");
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
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

		Path pathToFile = Paths.get(configPath + list.get(0).getValue());
		Files.createDirectories(pathToFile.getParent());

		File csvFile = new File(configPath + list.get(0).getValue() + ".csv");
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
				csvFile = new File(configPath + entry.getValue() + ".csv");
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

	public int getItemPrice(ItemStack itemStack) {
		if (itemStack == null) {
			return -1;
		}
		int ItemPrice = -1;
		String itemName = null;
		try {
			if (itemStack.isItemStackDamageable()) {
				itemName = itemStack.getUnlocalizedName() + ".0";
			} else {
				itemName = itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
			}
		} catch (Exception e) {
			return -1;
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
		return ItemPrice;
	}

	public boolean setItemPrice(ItemStack itemStack, int price) {
		if (itemStack == null) {
			return false;
		}
		int itemStackMeta = itemStack.getMetadata();
		if (itemStack.isItemStackDamageable()) {
			// override for damaged items
			itemStackMeta = 0;
		}
		String itemName = itemStack.getUnlocalizedName() + "." + itemStack.getItemDamage();
		// get modName to add to mapping
		String itemRegistryKey = Item.REGISTRY.getNameForObject(itemStack.getItem()).toString();
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
		updateItems();
		updateOreDictionary();
		updatePotions();
		autoPriceCraftedItems();
		autoPriceSmeltedItems();
		// write new configs
		writePriceLists();
	}

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

	public void resetDefaults() {
		try {
			loadDefaults();
		} catch (IOException e) {
			// fail quietly
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
					List<ItemStack> test = OreDictionary.getOres(keyName);
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
					Item item = (Item) Item.REGISTRY.getObject(new ResourceLocation(itemName));
					if (item != null) {
						stack = new ItemStack(item, 1, itemMeta);
					}
				}
			}
		}
		return stack;
	}

	private void autoPriceCraftedItems() {
		// FMLLog.info("in autoPriceCraftedItems");
		List<IRecipe> allrecipes = GameData.getRecipeRegistry().getValues();
		boolean priceUpdate = false;

		// we rerun multiple times if needed since recipe components might be
		// priced in previous runs
		// int loopCount = 0;
		do {
			// loopCount++;
			// FMLLog.info("priceUpdate loop: " + loopCount);
			priceUpdate = false;
			for (IRecipe irecipe : allrecipes) {
				int itemCost = 0;
				boolean validRecipe = true;
				ItemStack output = irecipe.getRecipeOutput();
				// FMLLog.info("Recipe output: " + output.getDisplayName());
				if (output == null || output.getItem() == Items.AIR) {
					continue;
				}
				if (UCItemPricer.getInstance().getItemPrice(output) != -1) {
					// FMLLog.info("recipe output price already set.");
					continue;
				}
				// FMLLog.info("Starting pricing recipe for " +
				// output.getDisplayName());
				NonNullList<Ingredient> recipeItems = irecipe.getIngredients();
				for (int i = 0; i < recipeItems.size(); i++) {
					ItemStack stack = null;
					// FMLLog.info("Ingredient: " + recipeItems.get(i));
					if (recipeItems.get(i) instanceof OreIngredient) {
						OreIngredient test = (OreIngredient) recipeItems.get(i);
						stack = test.getMatchingStacks()[0]; // TODO iterate and
																// check for
																// priced items
					} else {
						if (recipeItems.get(i).getMatchingStacks().length > 0) {
							stack = recipeItems.get(i).getMatchingStacks()[0]; // TODO
																				// do
																				// we
																				// need
																				// to
																				// iterate
																				// here?
						}
					}
					if (stack == null)
						continue;
					// FMLLog.info("recipe ingredient " + i + " " +
					// stack.getDisplayName());
					// FMLLog.info("price: " +
					// UCItemPricer.getInstance().getItemPrice(stack));
					if (UCItemPricer.getInstance().getItemPrice(stack) != -1) {
						itemCost += UCItemPricer.getInstance().getItemPrice(stack);
					} else {
						validRecipe = false;
						// FMLLog.info("can't price " +
						// output.getDisplayName());
						break;
					}
				}
				if (validRecipe && itemCost > 0) {
					priceUpdate = true;
					if (output.getCount() > 1) {
						itemCost = itemCost / output.getCount();
					}
					try {
						// FMLLog.info("Setting price of " +
						// output.getDisplayName() + " to " + itemCost);
						UCItemPricer.getInstance().setItemPrice(output, itemCost);
					} catch (Exception e) {
						FMLLog.warning("Universal Coins Autopricer: Failed to set item price.");
					}
				}
			}
		} while (priceUpdate == true);
	}

	private void autoPriceSmeltedItems() {
		Map<ItemStack, ItemStack> recipes = (Map<ItemStack, ItemStack>) FurnaceRecipes.instance().getSmeltingList();
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
			if (ucPriceMap.get(input.getUnlocalizedName() + "." + input.getItemDamage()) != null
					&& ucPriceMap.get(outputName + "." + input.getItemDamage()) != null) {
				int inputValue = ucPriceMap.get(input.getUnlocalizedName() + "." + input.getItemDamage());
				int outputValue = ucPriceMap.get(output.getUnlocalizedName() + "." + output.getItemDamage());
				if (inputValue != -1 && outputValue == -1) {
					ucPriceMap.put(output.getUnlocalizedName() + "." + output.getItemDamage(), inputValue + 2);
				}
			}
		}
	}

	private void updateItems() {
		for (Item item : Item.REGISTRY) {
			if (item == null) {
				continue;
			}
			if (item.getHasSubtypes()) {
				String baseName = new ItemStack(item, 1).getDisplayName();
				for (int i = 1; i < Integer.MAX_VALUE; i++) {
					try {
						ItemStack previousStack = new ItemStack(item, 1, i - 1);
						ItemStack stack = new ItemStack(item, 1, i);
						if (stack != null) {
							String previousName = previousStack.getDisplayName();
							String currentName = stack.getDisplayName();
							if (currentName.matches(baseName) || currentName.matches(previousName)
									|| currentName.contains("" + i)) {
								break;
							}
							String[] modNameArray = item.toString().split("\\W", 3);
							String modName = "";
							if (modNameArray[0].matches("net")) {
								modName = modNameArray[1];
							} else {
								modName = modNameArray[0];
							}
							addItemToPriceMap(modName, stack);
						}
					} catch (Exception e) {
						break;
					}
				}
			}
			String[] modNameArray = item.toString().split("\\W", 3);
			String modName = "";
			ItemStack stack = new ItemStack(item, 1);
			if (modNameArray[0].matches("net")) {
				modName = modNameArray[1];
			} else {
				modName = modNameArray[0];
			}
			if (stack.getItem() != Item.getItemFromBlock(Blocks.AIR)) {
				addItemToPriceMap(modName, stack);
			}
		}
	}

	private void updateOreDictionary() {
		for (String ore : OreDictionary.getOreNames()) {
			ucModnameMap.put(ore, "oredictionary");
			if (!ucPriceMap.containsKey(ore)) {
				// check ore to see if any of the types has a price, use it if
				// true
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
	}

	private void updatePotions() {
		for (PotionType potiontype : PotionType.REGISTRY) {
			String potion = potiontype.getRegistryName().toString();
			// FMLLog.info("Potion: " + potion);
			String[] modName = potion.split("\\W", 3);
			ucModnameMap.put(potion, modName[0]);
			if (!ucPriceMap.containsKey(potion)) {
				ucPriceMap.put(potion, -1);
			}
		}
	}

	private void addItemToPriceMap(String modName, ItemStack stack) {
		ucModnameMap.put(stack.getUnlocalizedName() + "." + stack.getItemDamage(), modName);
		if (!ucPriceMap.containsKey(stack.getUnlocalizedName() + "." + stack.getItemDamage())) {
			// check ore dictionary and see if we can price this item
			int itemPrice = -1;
			int[] id = OreDictionary.getOreIDs(stack);
			if (id.length > 0) {
				String oreItemName = OreDictionary.getOreName(id[0]);
				if (ucPriceMap.get(oreItemName) != null) {
					itemPrice = ucPriceMap.get(oreItemName);
				}
			}
			ucPriceMap.put(stack.getUnlocalizedName() + "." + stack.getItemDamage(), itemPrice);
		}
	}
}
