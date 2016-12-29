package universalcoins.util;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.economy.Wallet;

public class FEWallet implements Wallet {
	private String accountNumber;

	public FEWallet(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	@Override
	public void add(long amount) {
		if (amount > (long) Integer.MAX_VALUE) {
			amount = Integer.MAX_VALUE;
		}
		UniversalAccounts.getInstance().creditAccount(accountNumber, (int) amount);
	}

	@Override
	public void add(double amount) {
		if (amount > (long) Integer.MAX_VALUE) {
			amount = Integer.MAX_VALUE;
		}
		UniversalAccounts.getInstance().creditAccount(accountNumber, (int) amount);
	}

	@Override
	public boolean covers(long amount) {
		if (UniversalAccounts.getInstance().getAccountBalance(accountNumber) > amount) {
			return true;
		}
		return false;
	}

	@Override
	public long get() {
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber);
	}

	@Override
	public void set(long balance) {
		UniversalAccounts.getInstance().setAccountBalance(accountNumber, balance);
	}

	@Override
	public boolean withdraw(long amount) {
		if (amount > (long) Integer.MAX_VALUE) {
			return false;
		}
		return UniversalAccounts.getInstance().debitAccount(accountNumber, (int) amount);
	}

	@Override
	public String toString() {
		return APIRegistry.economy.toString(UniversalAccounts.getInstance().getAccountBalance(accountNumber));
	}
}
