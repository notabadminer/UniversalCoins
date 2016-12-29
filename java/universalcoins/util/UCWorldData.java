package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import universalcoins.UniversalCoins;

public class UCWorldData extends WorldSavedData {

	final static String key = UniversalCoins.MODID;

	public static UCWorldData getInstance() {
		World world = MinecraftServer.getServer().worldServers[0];
		MapStorage storage = world.mapStorage;
		UCWorldData result = (UCWorldData) storage.loadData(UCWorldData.class, key);
		if (result == null) {
			result = new UCWorldData(key);
			storage.setData(key, result);
		}
		return result;
	}
	
	private NBTTagCompound data = new NBTTagCompound();

	public UCWorldData(String tagName) {
		super(tagName);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		data = compound.getCompoundTag(key);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setTag(key, data);
		this.markDirty();
	}

	public NBTTagCompound getData() {
		return data;
	}
}
