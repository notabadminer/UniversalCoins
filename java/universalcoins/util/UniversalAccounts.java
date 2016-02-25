package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class UniversalAccounts {

	private static final UniversalAccounts instance = new UniversalAccounts();

	public static UniversalAccounts getInstance() {
		return instance;
	}

	private UniversalAccounts() {

	}

	public int getAccountBalance(String accountNumber) {
		if (hasKey(accountNumber)) {
			return getWorldInt(accountNumber);
		} else
			return -1;
	}

	public boolean debitAccount(String accountNumber, int amount) {
		if (hasKey(accountNumber)) {
			int balance = getWorldInt(accountNumber);
			if (amount <= balance) {
				balance -= amount;
				setWorldData(accountNumber, balance);
				return true;
			}
		}
		return false;
	}

	public boolean creditAccount(String accountNumber, int amount) {
		if (hasKey(accountNumber)) {
			int balance = getWorldInt(accountNumber);
			if ((double) balance + (double)amount <= Integer.MAX_VALUE) {
				balance += amount;
				setWorldData(accountNumber, balance);
				return true;
			}
		}
		return false;
	}

	public String getPlayerAccount(String playerUID) {
		// returns an empty string if no account found
		return getWorldString(playerUID);
	}

	public String getOrCreatePlayerAccount(String playerUID) {
		String accountNumber = getWorldString(playerUID);
		if (accountNumber == "") {
			while (!hasKey(playerUID)) {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, 0);
				}
			}
		}
		return accountNumber;
	}

	public boolean addPlayerAccount(String playerUID) {
		if (getWorldString(playerUID) == "") {
			String accountNumber = "";
			while (getWorldString(accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, 0);
					return true;
				}
			}
		}
		return false;
	}

	public String getCustomAccount(String playerUID) {
		return getWorldString("�" + playerUID);
	}

	public boolean addCustomAccount(String customName, String playerUID) {
		// custom accounts are added as a relation of playername to customname
		// customnames are then associated with an account number
		if (getWorldString("�" + playerUID) == "" && getWorldString(customName) == "") {
			String customAccountNumber = "";
			while (getWorldString(customAccountNumber) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(customAccountNumber) == "") {
					setWorldData("�" + playerUID, customName);
					setWorldData(customName, customAccountNumber);
					setWorldData(customAccountNumber, 0);
					return true;
				}
			}
		}
		return false;
	}

	public void transferCustomAccount(String playerUID, String customAccountName) {
		String oldName = getWorldString("�" + playerUID);
		String oldAccount = getWorldString(oldName);
		int oldBalance = getAccountBalance(oldAccount);
		delWorldData("�" + playerUID);
		delWorldData(oldName);
		delWorldData(oldAccount);
		if (getWorldString("�" + playerUID) == "") {
			String customAccountNumber = "none";
			while (getWorldString(customAccountNumber) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(customAccountNumber) == "") {
					setWorldData("�" + playerUID, customAccountName);
					setWorldData(customAccountName, customAccountNumber);
					setWorldData(customAccountNumber, oldBalance);
				}
				if (getWorldString(oldAccount) != "") {
					delWorldData(oldAccount);
					delWorldData(oldName);
				}
			}
		}
	}

	public void transferPlayerAccount(String playerUID) {
		String oldAccount = getWorldString(playerUID);
		int oldBalance = getAccountBalance(oldAccount);
		delWorldData(playerUID);
		if (getWorldString(playerUID) == "") {
			String accountNumber = "none";
			while (getWorldString(accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(accountNumber) == "") {
					setWorldData(playerUID, accountNumber);
					setWorldData(accountNumber, oldBalance);
				}
			}
		}
		delWorldData(oldAccount);
	}

	private int generateAccountNumber() {
		return (int) (Math.floor(Math.random() * 99999999) + 11111111);
	}

	private World getWorld() {
				return MinecraftServer.getServer().worldServers[0];
			}

	private boolean hasKey(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		return wdTag.hasKey(tag);
	}

	private void setWorldData(String tag, String data) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		wdTag.setString(tag, data);
		wData.markDirty();
	}

	private void setWorldData(String tag, int data) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		wdTag.setInteger(tag, data);
		wData.markDirty();
	}

	private int getWorldInt(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getInteger(tag);
	}

	private String getWorldString(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getString(tag);
	}

	private void delWorldData(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		wdTag.removeTag(tag);
		wData.markDirty();
	}

}
