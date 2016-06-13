package universalcoins.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import universalcoins.UniversalCoins;

public class UCWorldData extends WorldSavedData {

	final static String key = UniversalCoins.MODID;

	public static UCWorldData getInstance() {
		World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		MapStorage storage = world.getMapStorage();
		UCWorldData result = (UCWorldData) storage.getOrLoadData(UCWorldData.class, key);
		if (result == null) {
			result = new UCWorldData(key);
			storage.setData(key, result);
		}
		return result;
	}

	private NBTTagCompound nbt = new NBTTagCompound();;

	public UCWorldData(String tagName) {
		super(tagName);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		nbt = compound.getCompoundTag(key);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag(key, nbt);
		this.markDirty();
		return compound;
	}

	public boolean hasKey(String tag) {
		return nbt.hasKey(tag);
	}

	public void setData(String tag, String data) {
		nbt.setString(tag, data);
		markDirty();
	}

	public void setData(String tag, long data) {
		nbt.setLong(tag, data);
		markDirty();
	}

	public long getLong(String tag) {
		return nbt.getLong(tag);
	}

	public String getString(String tag) {
		return nbt.getString(tag);
	}

	public void delData(String tag) {
		nbt.removeTag(tag);
		markDirty();
	}

	public int getInteger(String key) {
		return nbt.getInteger(key);
	}

	public void setData(String tag, int data) {
		nbt.setInteger(tag, data);
		markDirty();
	}
}
