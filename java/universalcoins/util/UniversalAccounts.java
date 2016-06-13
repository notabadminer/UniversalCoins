package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;

public class UniversalAccounts {

	private static final UniversalAccounts instance = new UniversalAccounts();


	public static UniversalAccounts getInstance() {
		return instance;
	}

	private UniversalAccounts() {

	}

	public long getAccountBalance(String accountNumber) {
		UCWorldData WD = UCWorldData.getInstance();
		if (WD.hasKey(accountNumber)) {
			return WD.getLong(accountNumber);
		} else
			return -1;
	}

	//TODO: Possible race condition, check if abuseable.
	public boolean debitAccount(String accountNumber, long amount) {
		UCWorldData WD = UCWorldData.getInstance();
		if (WD.hasKey(accountNumber)) {
			long balance = WD.getLong(accountNumber);
			if (amount <= balance) {
				balance -= amount;
				WD.setData(accountNumber, balance);
				return true;
			}
		}
		return false;
	}

	public boolean creditAccount(String accountNumber, long amount) {
		UCWorldData WD = UCWorldData.getInstance();
		if (WD.hasKey(accountNumber)) {
			long balance = WD.getLong(accountNumber);
			if (Long.MAX_VALUE - balance > amount) {
				balance += amount;
				WD.setData(accountNumber, balance);
				return true;
			}
		}
		return false;
	}

	public String getPlayerAccount(String playerUID) {
		// returns an empty string if no account found
		UCWorldData WD = UCWorldData.getInstance();
		return WD.getString(playerUID);
	}

	public String getOrCreatePlayerAccount(String playerUID) {
		UCWorldData WD = UCWorldData.getInstance();
		String accountNumber = WD.getString(playerUID);
		if (accountNumber == "") {
			while (!WD.hasKey(playerUID)) {
				accountNumber = String.valueOf(generateAccountNumber());
				if (WD.getString(accountNumber) == "") {
					WD.setData(playerUID, accountNumber);
					WD.setData(accountNumber, 0);
				}
			}
		}
		return accountNumber;
	}

	public boolean addPlayerAccount(String playerUID) {
		UCWorldData WD = UCWorldData.getInstance();
		if (WD.getString(playerUID) == "") {
			String accountNumber;
			do {
				accountNumber = String.valueOf(generateAccountNumber());
			} while (WD.hasKey(accountNumber));
			WD.setData(playerUID, accountNumber);
			WD.setData(accountNumber, 0);
			return true;
		}
		return false;
	}

	public void transferPlayerAccount(String playerUID) {
		UCWorldData WD = UCWorldData.getInstance();
		String oldAccount = WD.getString(playerUID);
		long oldBalance = getAccountBalance(oldAccount);
		WD.delData(playerUID);
		WD.delData(oldAccount);
		String accountNumber = oldAccount;
		do {
			accountNumber = String.valueOf(generateAccountNumber());
		} while (WD.hasKey(accountNumber));
		WD.setData(playerUID, accountNumber);
		WD.setData(accountNumber, oldBalance);
	}

	private int generateAccountNumber() {
		return (int) (Math.floor(Math.random() * 99999999) + 11111111);
	}

	
}
