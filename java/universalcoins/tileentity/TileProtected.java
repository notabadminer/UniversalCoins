package universalcoins.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileProtected extends TileEntity {

	public String blockOwner = "none";
	public String playerName = "";
	public boolean inUse = false;

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		try {
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			playerName = tagCompound.getString("playerName");
		} catch (Throwable ex2) {
			playerName = "none";
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "none";
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setString("playerName", playerName);
		tagCompound.setString("blockOwner", blockOwner);
		return tagCompound;
	}

	//@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}
}
