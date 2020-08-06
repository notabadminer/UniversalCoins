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
			updateEnchantments();
			updatePotions();
			updateOreDictionary();
			try {
				loadDefaults();
			} catch (IOException e) {
				FMLLog.log.warn("Universal Coins: Failed to load default configs");
				e.printStackTrace();
			}
			priceCoins();
			priceCraftedItems();
			priceSmeltedItems();
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
				// FMLLog.log.info("Universal Coins: Loading Pricelist: " + configList[i]);
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
		System.currentTimeMillis();

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
		System.currentTimeMillis();
	}

	public int getItemPrice(ItemStack itemStack) {
		// FMLLog.log.info("UC: in getItemPrice");
		if (itemStack.isEmpty()) {
			return -1;
		}
		if (itemStack.getItemDamage() == 32767) {
			itemStack.setItemDamage(0);
		}
		int itemPrice = -1;
		String itemName = null;
		try {
			itemStack.getItemDamage();
			if (itemStack.isItemStackDamageable()) {
			}
			if (itemStack.isItemStackDamageable()) {
				itemName = itemStack.getItem().getRegistryName() + ".0";
			} else {
				itemName = itemStack.getItem().getRegistryName() + "." + itemStack.getItemDamage();
			}
		} catch (Exception e) {
			FMLLog.log.warn("getItemPrice: Error item name for " + itemStack);
			return -1;
		}
		if (ucPriceMap.get(itemName) != null) {
			itemPrice = ucPriceMap.get(itemName);
			// FMLLog.log.info("getItemPrice: " + itemName + " price = " + itemPrice);
		}
		// lookup item in oreDictionary if not priced
		if (itemPrice == -1) {
			int[] id = OreDictionary.getOreIDs(itemStack);
			if (id.length > 0) {
				NonNullList<ItemStack> oreStacks = OreDictionary.getOres(OreDictionary.getOreName(id[0]));
				for (ItemStack oreStack : oreStacks) {
					itemName = oreStack.getItem().getRegistryName().toString();
					if (ucPriceMap.get(itemName + ".0") != null) {
						itemPrice = ucPriceMap.get(itemName + ".0");
					}
				}
			}
		}
		// check for enchantments/potions last so we can cancel if it's not
		// priced
		if (itemStack.hasTagCompound()) {
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			String potionName = tagCompound.getString("Potion");
			// FMLLog.log.info("getItemPrice: Item has potion: " + potionName);
			// FMLLog.log.info("getItemPrice: Custom effects: " +
			// tagCompound.hasKey("CustomPotionEffects"));
			if (potionName != "" && !tagCompound.hasKey("CustomPotionEffects")) {
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
			if (hasEnchantment(itemStack)) {
				// FMLLog.log.info("getItemPrice: Item has enchantment");
				ArrayList<String> enchantments = getEnchantmentList(itemStack);
				for (String enchant : enchantments) {
					ResourceLocation enchantRL = getEnchantmentByName(enchant);
					// FMLLog.log.info("getItemPrice: Found enchantment: " + enchant);
					if (enchantRL != null
							&& ucPriceMap.get(enchantRL + enchant.substring(enchant.length() - 2)) != null) {

						int enchantPrice = ucPriceMap.get(enchantRL + enchant.substring(enchant.length() - 2));
						if (enchantPrice == -1) {
							return -1;
						}
						itemPrice += enchantPrice;
					} else {
						return -1;
					}

				}
			}
		}
		return itemPrice;
	}

	public boolean setItemPrice(ItemStack itemStack, int price) {
		if (itemStack == null || price <= 0 || hasEnchantment(itemStack) || hasPotion(itemStack)) {
			// FMLLog.log.info("UC: Can't price this!");
			return false;
		}
		int itemStackDamage = itemStack.getItemDamage();
		if (itemStack.isItemStackDamageable()) {
			// override for damaged items
			itemStackDamage = 0;
		}
		String itemName = itemStack.getItem().getRegistryName() + "." + itemStackDamage;
		// FMLLog.log.info("UC: setItemPrice: " + itemName + " " + price);
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
		List<String> keys = new ArrayList<String>(ucPriceMap.keySet());
		ItemStack stack = null;
		while (stack == null) {
			String keyName = keys.get(random.nextInt(keys.size())).toString();
			int price = 0;
			if (ucPriceMap.get(keyName) != null) {
				price = ucPriceMap.get(keyName);
			}
			if (price > 0) {
				// TODO update potion handling
				if (keyName.contains("potion") || keyName.contains("splash_potion")
						|| keyName.contains("lingering_potion")) {
					String[] splitKeyName = keyName.split("\\W", 2);
					Item potionItem = Item.REGISTRY.getObject(new ResourceLocation(splitKeyName[0]));
					if (potionItem != null) {
						stack = new ItemStack(potionItem, 1);
						PotionUtils.addPotionToItemStack(stack, PotionType.getPotionTypeForName(splitKeyName[1]));
					}
				}
				// oredictionary entries do not include damage value
				// we need to check for this
				if (!Character.isDigit(keyName.charAt(keyName.length() - 1))) {
					List<ItemStack> test = OreDictionary.getOres(keyName);
					for (int j = 0; j < test.size(); j++) {
						ItemStack oreDictionaryStack = (ItemStack) test.get(j);
						int subItemValue = getItemPrice(oreDictionaryStack);
						if (subItemValue > 0) {
							return oreDictionaryStack;
						}
					}
				} else {
					// split string into item name and damage
					String itemName = keyName.substring(0, keyName.length() - 2);
					int itemDamage = Integer.valueOf(keyName.substring(keyName.length() - 1));
					Item item = (Item) Item.REGISTRY.getObject(new ResourceLocation(itemName));
					if (item != null) {
						stack = new ItemStack(item, 1, itemDamage);
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

		do {
			// FMLLog.log.info("priceUpdate loop: " + loopCount);
			priceUpdate = false;
			for (ResourceLocation rloc : recipeResourceLocations) {
				IRecipe irecipe = CraftingManager.getRecipe(rloc);
				int itemCost = 0;
				boolean validRecipe = true;
				ItemStack output = irecipe.getRecipeOutput();
				// FMLLog.log.info("Recipe output: " + output.getDisplayName() + "." +
				// output.getItemDamage());
				// FMLLog.log.info("Recipe output price: " + getItemPrice(output));
				if (output == null || output.getItem() == Items.AIR || hasEnchantment(output) || hasPotion(output)) {
					continue;
				}
				if (getItemPrice(output) != -1) {
					// FMLLog.log.info("recipe output price already set.");
					continue;
				}
				// FMLLog.log.info("Starting pricing for " + output.getDisplayName() + "." +
				// output.getItemDamage() + " " + output);
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
					FMLLog.log.info(
							"recipe ingredient " + i + " " + stack.getDisplayName() + "." + output.getItemDamage());
					// FMLLog.log.info("price: " + getItemPrice(stack));
					if (getItemPrice(stack) > 0) {
						itemCost += getItemPrice(stack);
					} else {
						validRecipe = false;
						// FMLLog.log.info("can't price " + output.getDisplayName() + "." +
						// output.getItemDamage());
						break;
					}
				}
				if (validRecipe && itemCost / output.getCount() > 0) {
					priceUpdate = true;
					if (output.getCount() > 1) {
						itemCost = itemCost / output.getCount();
					}
					try {
						// FMLLog.log.info("Setting price of " + output.getDisplayName() + "." +
						// output.getItemDamage() + " " + output + " to " + itemCost);
						setItemPrice(output, itemCost);
					} catch (Exception e) {
						FMLLog.log.warn(
								"Universal Coins Autopricer: Failed to set item price: " + output.getUnlocalizedName());
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
					FMLLog.log
							.info("Setting price: " + outputName + "." + output.getItemDamage() + "=" + inputValue + 2);
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
			// FMLLog.log.info("Processing: " + item.getUnlocalizedName());
			if (item.getHasSubtypes()) {
				// FMLLog.log.info(item.getUnlocalizedName() + " has subtypes");
				String baseName = new ItemStack(item, 1).getDisplayName();
				for (int i = 1; i < Integer.MAX_VALUE; i++) {
					try {
						ItemStack previousStack = new ItemStack(item, 1, i - 1);
						ItemStack stack = new ItemStack(item, 1, i);
						if (stack != null) {
							// FMLLog.log.info("Checking " + stack.getUnlocalizedName());
							String previousName = previousStack.getDisplayName();
							String currentName = stack.getDisplayName();
							if (currentName.contentEquals(baseName) || currentName.contentEquals(previousName)
									|| currentName.contains("" + i)) {
								break;
							}
							addItemToPriceMap(stack);
							// FMLLog.log.info("Adding subtype: " + stack.getUnlocalizedName());
						}
					} catch (Exception e) {
						break;
					}
				}
			}

			ItemStack stack = new ItemStack(item, 1);

			Block.getBlockFromItem(item);
			addItemToPriceMap(stack);
		}
	}

	private void updateOreDictionary() {
		// parse oredictionary
		// FMLLog.log.info("in updateOreDictionary");

		for (String ore : OreDictionary.getOreNames()) {
			// FMLLog.log.info("checking oredictionary:" + ore);
			if (!ucPriceMap.containsKey(ore)) {
				// check ore to see if any of the types has a price, use it
				// if true
				List<ItemStack> test = OreDictionary.getOres(ore);
				int itemValue = -1;
				for (int j = 0; j < test.size(); j++) {
					// FMLLog.log.info("ore item:" + test.get(j).getItem().getRegistryName());
					ItemStack itemStack = ((ItemStack) test.get(j));
					if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						itemStack.setItemDamage(0);
					}
					int subItemValue = getItemPrice(itemStack);
					// FMLLog.log.info("sub item price:" + subItemValue);
					if (subItemValue > 0) {
						itemValue = subItemValue;
					}
				}
				// FMLLog.log.info("Setting price: " + ore + " " + itemValue);
				ucPriceMap.put("oredictionary:" + ore, itemValue);
			}
		}
	}

	private void updatePotions() {
		for (PotionType potiontype : PotionType.REGISTRY) {
			String potionName = potiontype.getRegistryName().toString();
			if (!ucPriceMap.containsKey(potionName)) {
				// FMLLog.log.info("Setting price: " + potionName + "." + -1);
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
					// FMLLog.log.info("Setting price: " + fullEnchantName + "." + -1);
					ucPriceMap.put(fullEnchantName, -1);
				}
			}
		}
	}

	private ResourceLocation getEnchantmentByName(String name) {
		Map<String, ResourceLocation> enchantMap = new HashMap<String, ResourceLocation>(0);

		for (ResourceLocation enchantment : Enchantment.REGISTRY.getKeys()) {
			enchantMap.put(Enchantment.getEnchantmentByLocation(enchantment.toString()).getName(), enchantment);
		}

		return enchantMap.get(name.substring(0, name.length() - 2));
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
			// FMLLog.log.info("Setting price: " + stack.getItem().getRegistryName() + "." +
			// stack.getItemDamage() + " " + itemPrice);
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
			if (!tagCompound.hasKey("CustomPotionEffects")) {
				return tagCompound.hasKey("Potion");
			}
		}
		return false;
	}

	public String getPotion(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tagCompound = stack.getTagCompound();
			String potion = tagCompound.getString("Potion");
			if (!tagCompound.hasKey("CustomPotionEffects") && !potion.contentEquals("")) {
				return potion;
			}
		}
		return "";
	}
}
