package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class UniversalPower {

	private static final UniversalPower instance = new UniversalPower();

	public static UniversalPower getInstance() {
		return instance;
	}

	private UniversalPower() {

	}

	public long getRFLevel() {
		if (hasKey("power")) {
			return getWorldLong("power");
		} else
			setWorldLong("power", 0);
		return 0;
	}

	public long extractEnergy(int maxSend, boolean simulate) {
		long powerLevel = getWorldLong("power");
		maxSend = (int) Math.min(maxSend, powerLevel);
		if (!simulate) {
			powerLevel -= maxSend;
			setWorldLong("power", powerLevel);
		}
		return maxSend;
	}

	public long receiveEnergy(int maxReceive, boolean simulate) {
		long powerLevel = getWorldLong("power");
		if (Long.MAX_VALUE - maxReceive >= powerLevel) {
			powerLevel += maxReceive;
		} else {
			maxReceive = (int) (Long.MAX_VALUE - powerLevel);
			powerLevel += maxReceive;
		}
		if (!simulate) {
			setWorldLong("power", powerLevel);
		}
		return maxReceive;
	}

	private World getWorld() {
		return MinecraftServer.getServer().worldServers[0];
	}

	private boolean hasKey(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		return wdTag.hasKey(tag);
	}

	private void setWorldLong(String tag, long data) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		wdTag.setLong(tag, data);
		wData.markDirty();
	}

	private long getWorldLong(String tag) {
		UCWorldData wData = UCWorldData.get(getWorld());
		NBTTagCompound wdTag = wData.getData();
		return wdTag.getLong(tag);
	}
}
