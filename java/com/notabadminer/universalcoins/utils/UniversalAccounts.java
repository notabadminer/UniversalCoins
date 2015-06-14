package com.notabadminer.universalcoins.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class UniversalAccounts {
	
	private static UniversalAccounts instance = new UniversalAccounts();
	
	public static UniversalAccounts getInstance()
	{
		 return instance;
	}
	
	private UniversalAccounts()
	{
		
	}
	
	public int getAccountBalance(World world, String accountNumber) {
		if (hasKey(world, accountNumber)) {
			return getWorldInt(world, accountNumber);
		} else return -1;	
	}
	
	public void debitAccount(World world, String accountNumber, int amount) {
		if (hasKey(world, accountNumber)) {
			int balance = getWorldInt(world, accountNumber);
			balance -= amount;
			setWorldData(world, accountNumber, balance);
		}
	}
	
	public void creditAccount(World world, String accountNumber, int amount) {
		if (hasKey(world, accountNumber)) {
			int balance = getWorldInt(world, accountNumber);
			balance += amount;
			setWorldData(world, accountNumber, balance);
		}
	}
	
	public String getPlayerAccount(World world, String playerUID) {
		//returns an empty string if no account found
		return getWorldString(world, playerUID);
	}
	
	public String getOrCreatePlayerAccount(World world, String playerUID) {
		String accountNumber = getWorldString(world, playerUID);
		if (accountNumber == "") {
			while (!hasKey(world, playerUID)) {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(world, accountNumber) == "") {
					setWorldData(world, playerUID, accountNumber);
					setWorldData(world, accountNumber, 0);
				}
			}
		}
		return accountNumber;
	}
	
	public void addPlayerAccount(World world, String playerUID) {
		if (getWorldString(world, playerUID) == "") {
			String accountNumber = "";
			while (getWorldString(world, accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(world, accountNumber) == "") {
					setWorldData(world, playerUID, accountNumber);
					setWorldData(world, accountNumber, 0);
				}
			}
		} else {
			//we have a problem we need to clear stale account data
			
		}
	}
	
	public String getCustomAccount(World world, String playerUID){
		return getWorldString(world, "¿" + playerUID);
	}
	
	public boolean addCustomAccount(World world, String customName, String playerUID) {
		//custom accounts are added as a relation of playername to customname
		//customnames are then associated with an account number
		if (getWorldString(world, "¿" + playerUID) == "" && getWorldString(world, customName) == "") {
			String customAccountNumber = "";
			while (getWorldString(world, customAccountNumber ) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(world, customAccountNumber) == "") {
					setWorldData(world, "¿" + playerUID, customName);
					setWorldData(world, customName, customAccountNumber);
					setWorldData(world, customAccountNumber, 0);
					return true;
				}
			}
		}
		return false;
	}
	
	public void transferCustomAccount(World world, String playerUID, String customAccountName) {
		String oldName = getWorldString(world, "¿" + playerUID);
		String oldAccount = getWorldString(world, oldName);
		int oldBalance = getAccountBalance(world, oldAccount);
		delWorldData(world, "¿" + playerUID);
		delWorldData(world, oldName);
		delWorldData(world, oldAccount);
		if (getWorldString(world, "¿" + playerUID) == "") {
			String customAccountNumber = "none";
			while (getWorldString(world, customAccountNumber) == "") {
				customAccountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(world, customAccountNumber) == "") {
					setWorldData(world, "¿" + playerUID, customAccountName);
					setWorldData(world, customAccountName, customAccountNumber);
					setWorldData(world, customAccountNumber, oldBalance);
				}
				if (getWorldString(world, oldAccount) != "") {
					delWorldData(world, oldAccount);
					delWorldData(world, oldName);
				}
			}
		}
	}
	
	public void transferPlayerAccount(World world, String playerUID) {
		String oldAccount = getWorldString(world, playerUID);
		int oldBalance = getAccountBalance(world, oldAccount);
		delWorldData(world, playerUID);
		if (getWorldString(world, playerUID) == "") {
			String accountNumber = "none";
			while (getWorldString(world, accountNumber) == "") {
				accountNumber = String.valueOf(generateAccountNumber());
				if (getWorldString(world, accountNumber) == "") {
					setWorldData(world, playerUID, accountNumber);
					setWorldData(world, accountNumber, oldBalance);
				}
			}
		}
		delWorldData(world, oldAccount);
	}
	
	private int generateAccountNumber() {
		return (int) (Math.floor(Math.random() * 99999999) + 11111111);
	}
	
	private boolean hasKey(World world, String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.hasKey(tag);
	}
	
	private void setWorldData(World world, String tag, String data) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setString(tag, data);
		wData.markDirty();
	}
	
	private void setWorldData(World world, String tag, int data) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.setInteger(tag, data);
		wData.markDirty();
	}
	
	private int getWorldInt(World world, String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getInteger(tag);
	}
	
	private String getWorldString(World world, String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getString(tag);
	}
	
	private void delWorldData(World world, String tag) {
		UCWorldData wData = UCWorldData.get(world);
		NBTTagCompound wdTag = wData.getData();
		wdTag.removeTag(tag);
		wData.markDirty();
	}

}
