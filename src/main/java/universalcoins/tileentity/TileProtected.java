package universalcoins.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;

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

	// ==== chunk load sync ====//
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}
	// ==== /chunk load sync ====//

	// ===== block update sync =========//
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void updateTE() {
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	}
	// ===== /block update sync =========//

	// ==== general client to server messages ====/
	public void sendButtonMessage(int functionID, boolean shiftPressed) {
		UniversalCoins.snw
				.sendToServer(new UCButtonMessage(pos.getX(), pos.getY(), pos.getZ(), functionID, shiftPressed));
	}
	// ==== /general client to server messages ====/
}
