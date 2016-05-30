package universalcoins.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import universalcoins.UniversalCoins;
import universalcoins.net.UCSignServerMessage;
import universalcoins.net.UCTileSignMessage;

public class TileUCSign extends TileEntitySign {

	public String blockOwner = "";

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		for (int i = 0; i < 4; ++i) {
			String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
			tagCompound.setString("Text" + (i + 1), s);
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "";
		}
	}

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
		markDirty();
		worldObj.notifyBlockUpdate(getPos(), worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(new UCSignServerMessage(pos.getX(), pos.getY(), pos.getZ(), signText));
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		ITextComponent[] aITextComponent = new ITextComponent[4];
		System.arraycopy(this.signText, 0, aITextComponent, 0, 4);
		SPacketUpdateTileEntity p = new SPacketUpdateTileEntity(pos, getBlockMetadata(), getTileData());
		return p;
	}

	public void scanChestContents() {
		TileEntity tileEntity = null;
		String[] itemName = { "", "", "", "" };
		int[] itemCount = { 0, 0, 0, 0 };
		int meta = super.getBlockMetadata();
		if (meta == 2) {
			tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
		}
		if (meta == 3) {
			tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
		}
		if (meta == 4) {
			tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
		}
		if (meta == 5) {
			tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
		}
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
						if (itemName[j].matches(inventory.getStackInSlot(i).getDisplayName())) {
							itemCount[j] += inventory.getStackInSlot(i).stackSize;
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
								if (itemName[j].matches(inventory.getStackInSlot(i).getDisplayName())) {
									itemCount[j] += inventory.getStackInSlot(i).stackSize;
								}
							}
						}
					}
				}
			}
			// update sign with info collected
			for (int i = 0; i < itemName.length; i++) {
				if (itemName[i] != "") {
					ITextComponent itextcomponent = ITextComponent.Serializer
							.jsonToComponent(itemCount[i] + " " + itemName[i]);
					signText[i] = itextcomponent;
				} else {
					signText[i] = new TextComponentString("");
				}
				this.updateSign();
			}
		}
	}
}
