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

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import universalcoins.UniversalCoins;

public class UCItemPricer {

	private static final UCItemPricer instance = new UCItemPricer();

	private static Map<String, Integer> ucPriceMap = new HashMap<String, Integer>(0);
	private static String configPath = FMLInjectionData.data()[6] + "/config/universalcoins/";
	private Random random = new Random();

	public static UCItemPricer getInstance() {
		return instance;
	}

	private UCItemPricer() {

	}

	public void loadConfigs() {
		if (!new File(configPath).exists()) {
			// FMLLog.log.info("Universal Coins: Loading default prices");
			updateItems();
			try {
				loadDefaults();
			} catch (IOException e) {
				FMLLog.log.warn("Universal Coins: Failed to load default configs");
				e.printStackTrace();
			}
			updatePotions();
			updateEnchantments();
			updateOreDictionary();
			priceCoins();
			priceCraftedItems();
			priceSmeltedItems();
			pricePotions();
			writePriceLists();
		} else {
			try {
				loadPricelists();
			} catch (IOException e) {
				FMLLog.log.warn("Universal Coins: Failed to load config files");
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
				// FMLLog.log.info("Universal Coins: Loading Pricelist: " +
				// configList[i]);
				BufferedReader br = new BufferedReader(new FileReader(configList[i]));
				String tempString = "";
				while ((tempString = br.readLine()) != null) {
					if (tempString.startsWith("//") || tempString.startsWith("#")) {
						continue; // we have a comment. skip it
					}
					String[] tempData = tempString.split("=");
					if (tempData.length < 2) {
						// something is wrong with this line
						FMLLog.log.warn("Universal Coins: ERROR: line containing " + tempString + " in "
								+ configList[i].getName() + " is invalid");
						continue;
					}
					int itemPrice = -1;
					try {
						itemPrice = Integer.valueOf(tempData[1]);
					} catch (NumberFormatException e) {
						FMLLog.log.warn("Universal Coins: ERROR: line containing " + tempString + " in "
								+ configList[i].getName() + " is invalid");
					}
					ucPriceMap.put(tempData[0], itemPrice);
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
			FMLLog.log.warn("UniversalCoins: Failed to create config file");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void priceListWriter() throws IOException {
		// FMLLog.log.info("Starting priceListWriter");
		long startTime = System.currentTimeMillis();

		Set<Entry<String, Integer>> set = ucPriceMap.entrySet();
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getKey().compareTo(o1.getKey());
			}
		});

		Path pathToFile = Paths.get(configPath + list.get(0).getKey().split("\\W", 2)[0]);
		Files.createDirectories(pathToFile.getParent());

		File csvFile = new File(configPath + list.get(0).getKey().split("\\W", 2)[0] + ".csv");
		// FMLLog.log.info("csvFile: " + csvFile);
		if (csvFile.exists()) {
			csvFile.delete();
			csvFile.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(csvFile));
		String previousModName = list.get(0).getKey().split("\\W", 2)[0];
		for (Entry<String, Integer> entry : list) {
			String modName = entry.getKey().split("\\W", 2)[0];
			// FMLLog.log.info("working on : " + entry.getKey());
			if (!modName.matches(previousModName)) {
				// FMLLog.log.info("New mod name");
				previousModName = modName;
				out.flush();
				out.close();
				// move to next file
				csvFile = new File(configPath + entry.getKey().split("\\W", 2)[0] + ".csv");
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
		long endTime = System.currentTimeMillis();
		// FMLLog.log.info("File writes took " + (endTime - startTime) + "
		// milliseconds");
	}

	public int getItemPrice(ItemStack itemStack) {
		// FMLLog.log.info("in getItemPrice");
		if (itemStack.isEmpty()) {
			return -1;
		}
		if (itemStack.getItemDamage() == 32767) {
			itemStack.setItemDamage(0);
		}
		int itemPrice = -1;
		String itemName = null;
		try {
			if (itemStack.isItemStackDamageable()) {
				itemName = itemStack.getItem().getRegistryName() + ".0";
			} else {
				itemName = itemStack.getItem().getRegistryName() + "." + itemStack.getItemDamage();
			}
		} catch (Exception e) {
			// FMLLog.log.info("failed to set itemName");
			return -1;
		}
		// FMLLog.log.info("Checking price of " + itemName);
		if (ucPriceMap.get(itemName) != null) {
			itemPrice = ucPriceMap.get(itemName);
			// FMLLog.log.info(itemName + "=" + itemPrice);
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
		// check for enchantments/potions last so we can cancel if it's not
		// priced
		if (itemStack.hasTagCompound()) {
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			String potionName = tagCompound.getString("Potion");
			// FMLLog.log.info("potion string: " + potionName);
			if (potionName != "") {
				if (ucPriceMap.get(potionName) != null) {
					int potionPrice = ucPriceMap.get(potionName);
					if (potionPrice == -1) {
						return -1;
					}
					itemPrice += potionPrice;
				} else {
					itemPrice = -1;
				}
			}
			if (itemStack.isItemEnchanted()) {
				ArrayList<String> enchantments = getEnchantmentList(itemStack);
				for (String enchant : enchantments) {
					if (ucPriceMap.get(enchant) != null) {
						int enchantPrice = ucPriceMap.get(enchant);
						itemPrice += enchantPrice;
					} else {
						itemPrice = -1;
					}

				}
			}
		}
		return itemPrice;
	}

	public boolean setItemPrice(ItemStack itemStack, int price) {
		if (itemStack == null) {
			return false;
		}
		if (price == 0 || price < -1)
			return false;
		int itemStackMeta = itemStack.getMetadata();
		if (itemStack.isItemStackDamageable()) {
			// override for damaged items
			itemStackMeta = 0;
		}
		if (hasEnchantment(itemStack) || hasPotion(itemStack))
			return false;
		String itemName = itemStack.getItem().getRegistryName() + "." + itemStack.getItemDamage();
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
		updateEnchantments();
		priceCoins();
		priceCraftedItems();
		priceSmeltedItems();
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
				// TODO deal with potions
				if (keyName.contains("potion") || keyName.contains("splash_potion")
						|| keyName.contains("lingering_potion")) {
					String[] splitKeyName = keyName.split("\\W", 2);
					Item potionItem = Item.REGISTRY.getObject(new ResourceLocation(splitKeyName[0]));
					if (potionItem != null) {
						stack = new ItemStack(potionItem, 1);
						PotionUtils.addPotionToItemStack(stack, PotionType.getPotionTypeForName(splitKeyName[1]));
					}
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

	private void priceCraftedItems() {
		// FMLLog.log.info("in autoPriceCraftedItems");
		Set<ResourceLocation> recipeResourceLocations = CraftingManager.REGISTRY.getKeys();
		boolean priceUpdate = false;

		// we rerun multiple times if needed since recipe components might be
		// priced in previous runs
		int loopCount = 0;
		do {
			loopCount++;
			// FMLLog.log.info("priceUpdate loop: " + loopCount);
			priceUpdate = false;
			for (ResourceLocation rloc : recipeResourceLocations) {
				IRecipe irecipe = CraftingManager.getRecipe(rloc);
				int itemCost = 0;
				boolean validRecipe = true;
				ItemStack output = irecipe.getRecipeOutput();
				// FMLLog.log.info("Recipe output: " + output.getDisplayName());
				if (output == null || output.getItem() == Items.AIR) {
					continue;
				}
				if (UCItemPricer.getInstance().getItemPrice(output) != -1) {
					// FMLLog.log.info("recipe output price already set.");
					continue;
				}
				// FMLLog.log.info("Starting pricing for " +
				// output.getDisplayName());
				NonNullList<Ingredient> recipeItems = irecipe.getIngredients();
				for (int i = 0; i < recipeItems.size(); i++) {
					ItemStack stack = null;
					// FMLLog.log.info("Ingredient: " + recipeItems.get(i));
					if (recipeItems.get(i) instanceof OreIngredient) {
						OreIngredient test = (OreIngredient) recipeItems.get(i);
						if (test.getMatchingStacks().length > 0)
							stack = test.getMatchingStacks()[0];
						// TODO iterate and check for priced items
					} else {
						if (recipeItems.get(i).getMatchingStacks().length > 0) {
							stack = recipeItems.get(i).getMatchingStacks()[0];
							// TODO do we need to iterate here?
						}
					}
					if (stack == null)
						continue;
					// FMLLog.log.info("recipe ingredient " + i + " " +
					// stack.getDisplayName());
					// FMLLog.log.info("price: " +
					// UCItemPricer.getInstance().getItemPrice(stack));
					if (UCItemPricer.getInstance().getItemPrice(stack) != -1) {
						itemCost += UCItemPricer.getInstance().getItemPrice(stack);
					} else {
						validRecipe = false;
						// FMLLog.log.info("can't price " +
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
						// FMLLog.log.info("Setting price of " +
						// output.getDisplayName() + " to " + itemCost);
						UCItemPricer.getInstance().setItemPrice(output, itemCost);
					} catch (Exception e) {
						FMLLog.log.warn("Universal Coins Autopricer: Failed to set item price.");
					}
				}
			}
		} while (priceUpdate == true);
	}

	private void priceCoins() {
		Item[] coins = new Item[] { UniversalCoins.Items.iron_coin, UniversalCoins.Items.gold_coin,
				UniversalCoins.Items.emerald_coin, UniversalCoins.Items.diamond_coin,
				UniversalCoins.Items.obsidian_coin };
		for (int i = 0; i < coins.length; i++) {
			int itemPrice = UniversalCoins.coinValues[i];
			ucPriceMap.put(coins[i].getRegistryName() + ".0", itemPrice);
		}
	}

	private void pricePotions() { // TODO makey it workey
		List<IBrewingRecipe> recipes = BrewingRecipeRegistry.getRecipes();
		for (IBrewingRecipe recipe : recipes) {
			// FMLLog.log.info("Potion recipe: " + recipe);
		}
	}

	private void priceSmeltedItems() {
		// FMLLog.log.info("In priceSmeltedItems");
		Map<ItemStack, ItemStack> recipes = (Map<ItemStack, ItemStack>) FurnaceRecipes.instance().getSmeltingList();
		for (Entry<ItemStack, ItemStack> recipe : recipes.entrySet()) {
			ItemStack input = recipe.getKey();
			ItemStack output = recipe.getValue();
			String inputName = "";
			String outputName = "";
			try {
				inputName = input.getItem().getRegistryName().toString();
				outputName = output.getItem().getRegistryName().toString();
			} catch (Exception e) {
				continue;
			}
			if (input.getItemDamage() == 32767) {
				input.setItemDamage(0);
			}
			if (ucPriceMap.get(inputName + "." + input.getItemDamage()) != null
					&& ucPriceMap.get(outputName + "." + output.getItemDamage()) != null) {
				int inputValue = ucPriceMap.get(inputName + "." + input.getItemDamage());
				int outputValue = ucPriceMap.get(outputName + "." + output.getItemDamage());
				if (inputValue != -1 && outputValue == -1) {
					// FMLLog.log.info("updating: " + outputName + "." +
					// output.getItemDamage() + "=" + inputValue + 2);
					ucPriceMap.put(outputName + "." + output.getItemDamage(), inputValue + 2);
				}
			}
		}
	}

	private void updateItems() {
		// FMLLog.log.info("Starting updateItems()");
		for (Item item : Item.REGISTRY) {
			if (item == null) {
				continue;
			}
			if (item.getHasSubtypes()) {
				// FMLLog.log.info(item.getUnlocalizedName() + " has subtypes");
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
							addItemToPriceMap(stack);
						}
					} catch (Exception e) {
						break;
					}
				}
			}

			ItemStack stack = new ItemStack(item, 1);

			Block testBlock = Block.getBlockFromItem(item);
			addItemToPriceMap(stack);
		}
	}

	private void updateOreDictionary() {
		// FMLLog.log.info("in updateOreDictionary");
		for (String ore : OreDictionary.getOreNames()) {
			// FMLLog.log.info("checking oredictionary:" + ore);
			// check ore to see if any of the types has a price, use it if
			// true
			List<ItemStack> test = OreDictionary.getOres(ore);
			int itemValue = -1;
			for (int j = 0; j < test.size(); j++) {
				// FMLLog.log.info("ore item:" + test.get(j).getItem().getRegistryName());
				int subItemValue = getItemPrice(test.get(j));
				// FMLLog.log.info("sub item price:" + subItemValue);
				if (subItemValue > 0) {
					itemValue = subItemValue;
					break;
				}
			}
			ucPriceMap.put("oredictionary:" + ore, itemValue);
		}
	}

	private void updatePotions() {
		for (PotionType potiontype : PotionType.REGISTRY) {
			String potionName = potiontype.getRegistryName().toString();
			if (!ucPriceMap.containsKey(potionName)) {
				ucPriceMap.put(potionName, -1);
			}
		}
	}

	private void updateEnchantments() {
		for (ResourceLocation enchantment : Enchantment.REGISTRY.getKeys()) {
			int maxLevel = Enchantment.getEnchantmentByLocation(enchantment.toString()).getMaxLevel();
			for (int i = 1; i <= maxLevel; i++) {
				String fullEnchantName = enchantment.toString() + "." + i;

				if (!ucPriceMap.containsKey(fullEnchantName)) {
					ucPriceMap.put(fullEnchantName, -1);
				}
			}
		}
	}

	private void addItemToPriceMap(ItemStack stack) {
		if (!ucPriceMap.containsKey(stack.getItem().getRegistryName() + "." + stack.getItemDamage())) {
			// check ore dictionary and see if we can price this item
			int itemPrice = -1;
			int[] id = null;
			try {
				id = OreDictionary.getOreIDs(stack);
			} catch (Exception e) {
				// fail quietly
			}
			if (id != null && id.length > 0) {
				String oreItemName = OreDictionary.getOreName(id[0]);
				if (ucPriceMap.get("oredictionary:" + oreItemName) != null) {
					itemPrice = ucPriceMap.get("oredictionary:" + oreItemName);
				}
			}
			ucPriceMap.put(stack.getItem().getRegistryName() + "." + stack.getItemDamage(), itemPrice);
		}
	}

	private ArrayList<String> getEnchantmentList(ItemStack stack) {
		ArrayList<String> enchantments = new ArrayList<String>();
		NBTTagCompound tagCompound = stack.getTagCompound();
		// Books
		NBTTagList enchList = tagCompound.getTagList("StoredEnchantments", Constants.NBT.TAG_COMPOUND);
		// Tools + Weapons
		if (enchList.hasNoTags()) {
			enchList = stack.getTagCompound().getTagList("ench", Constants.NBT.TAG_COMPOUND);
		}
		if (enchList != null) {
			for (int i = 0; i < enchList.tagCount(); i++) {
				NBTTagCompound enchant = ((NBTTagList) enchList).getCompoundTagAt(i);
				short enchId = enchant.getShort("id");
				short lvl = enchant.getShort("lvl");
				enchantments.add(Enchantment.getEnchantmentByID(enchId).getName() + "." + lvl);
			}
		}
		return enchantments;
	}

	public boolean hasEnchantment(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tagCompound = stack.getTagCompound();
			return tagCompound.hasKey("ench", 9) || tagCompound.hasKey("StoredEnchantments");
		}
		return false;
	}

	public boolean hasPotion(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tagCompound = stack.getTagCompound();
			return tagCompound.hasKey("Potion");
		}
		return false;
	}
}
