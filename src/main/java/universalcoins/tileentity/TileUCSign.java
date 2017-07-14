package universalcoins.tileentity;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import universalcoins.UniversalCoins;
import universalcoins.net.UCSignServerMessage;

public class TileUCSign extends TileEntitySign {

	public String blockOwner = "";

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		for (int i = 0; i < 4; ++i) {
			String s = tagCompound.getString("Text" + (i + 1));
			ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);

			this.signText[i] = itextcomponent;

			try {
				blockOwner = tagCompound.getString("blockOwner");
			} catch (Throwable ex) {
				blockOwner = "";
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		for (int i = 0; i < 4; ++i) {
			String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
			tagCompound.setString("Text" + (i + 1), s);
		}

		tagCompound.setString("blockOwner", blockOwner);
		return tagCompound;
	}

	public void updateSign() {
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(new UCSignServerMessage(pos.getX(), pos.getY(), pos.getZ(), signText));
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 9, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	public void scanChestContents(IBlockState state, BlockPos fromPos) {
		TileEntity tileEntity = world.getTileEntity(fromPos);
		String[] itemName = { "", "", "", "" };
		int[] itemCount = { 0, 0, 0, 0 };
		if (tileEntity != null && tileEntity instanceof IInventory) {
			IInventory inventory = (IInventory) tileEntity;
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack != null) {
					for (int j = 0; j < itemName.length; j++) {
						if (itemName[j] == "") {
							if (!itemName[0].matches(inventory.getStackInSlot(i).getDisplayName())
									&& !itemName[1].matches(inventory.getStackInSlot(i).getDisplayName())
									&& !itemName[2].matches(inventory.getStackInSlot(i).getDisplayName())
									&& !itemName[3].matches(inventory.getStackInSlot(i).getDisplayName())) {
								itemName[j] = inventory.getStackInSlot(i).getDisplayName();
							}
						}
						if (itemName[j].contentEquals(inventory.getStackInSlot(i).getDisplayName())) {
							if (itemName[j].contentEquals("Air")) {
								itemCount[j]++;
							}
							itemCount[j] += inventory.getStackInSlot(i).getCount();
						}
					}
				}
			}
			// if we have a double chest, scan it too
			if (tileEntity instanceof TileEntityChest) {
				TileEntity tileEntity2 = null;
				if (((TileEntityChest) tileEntity).adjacentChestXNeg != null)
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestXNeg);
				if (((TileEntityChest) tileEntity).adjacentChestXPos != null)
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestXPos);
				if (((TileEntityChest) tileEntity).adjacentChestZNeg != null)
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestZNeg);
				if (((TileEntityChest) tileEntity).adjacentChestZPos != null)
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestZPos);
				if (tileEntity2 != null) {
					inventory = (IInventory) tileEntity2;
					for (int i = 0; i < inventory.getSizeInventory(); i++) {
						ItemStack stack = inventory.getStackInSlot(i);
						if (stack != null) {
							for (int j = 0; j < itemName.length; j++) {
								if (itemName[j] == "") {
									if (!itemName[0].matches(inventory.getStackInSlot(i).getDisplayName())
											&& !itemName[1].matches(inventory.getStackInSlot(i).getDisplayName())
											&& !itemName[2].matches(inventory.getStackInSlot(i).getDisplayName())
											&& !itemName[3].matches(inventory.getStackInSlot(i).getDisplayName())) {
										itemName[j] = inventory.getStackInSlot(i).getDisplayName();
									}
								}
								if (itemName[j].contentEquals(inventory.getStackInSlot(i).getDisplayName())) {
									if (itemName[j].contentEquals("Air")) {
										itemCount[j]++;
									}
									itemCount[j] += inventory.getStackInSlot(i).getCount();
								}
							}
						}
					}
				}
			}

			// update sign with info collected
			for (int i = 0; i < itemName.length; i++) {
				if (itemName[i].contentEquals("Air")) {
					signText[i] = new TextComponentString(itemCount[i] + " Empty Slots");

				} else if (!itemName[i].contentEquals("")) {
					signText[i] = new TextComponentString(itemCount[i] + " " + itemName[i]);
				} else {
					{
						signText[i] = new TextComponentString("");
					}
					this.updateSign();
				}
			}
		}
	}
}
