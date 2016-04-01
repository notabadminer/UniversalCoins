package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class UniversalAccounts {

	private static final UniversalAccounts instance = new UniversalAccounts();
	private static World world;

	public static UniversalAccounts getInstance(World world) {
		instance.world = world;
		return instance;
	}

	private UniversalAccounts() {

	}

	public long getAccountBalance(String accountNumber) {
		if (hasKey(accountNumber)) {
			return getWorldLong(accountNumber);
		} else
			return -1;
	}

	public boolean debitAccount(String accountNumber, long amount) {
		if (hasKey(accountNumber)) {
			long balance = getWorldLong(accountNumber);
			if (amount <= balance) {
				balance -= amount;
				setWorldData(accountNumber, balance);
				return true;
			}
		}
		return false;
	}

	public boolean creditAccount(String accountNumber, long amount) {
		if (hasKey(accountNumber)) {
			long balance = getWorldLong(accountNumber);
			if (Long.MAX_VALUE - balance >= amount) {
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

	public void transferPlayerAccount(String playerUID) {
		String oldAccount = getWorldString(playerUID);
		long oldBalance = getAccountBalance(oldAccount);
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

	private boolean hasKey(String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.hasKey(tag);
	}

	private void setWorldData(String tag, String data) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setString(tag, data);
		wData.markDirty();
	}

	private void setWorldData(String tag, long data) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setLong(tag, data);
		wData.markDirty();
	}

	private long getWorldLong(String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getLong(tag);
	}

	private String getWorldString(String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getString(tag);
	}

	private void delWorldData(String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.removeTag(tag);
		wData.markDirty();
	}
}