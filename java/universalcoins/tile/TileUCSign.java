package universalcoins.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import universalcoins.UniversalCoins;
import universalcoins.net.UCSignServerMessage;
import universalcoins.net.UCTextureMessage;
import universalcoins.net.UCTileSignMessage;

import com.google.gson.JsonParseException;

public class TileUCSign extends TileEntitySign {
		
	public String blockOwner = "";
	public String blockIcon = "";

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		for (int i = 0; i < 4; ++i) {
			String s = tagCompound.getString("Text" + (i + 1));
			try {
				IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s);
				this.signText[i] = ichatcomponent;
			} catch (JsonParseException jsonparseexception) {
				this.signText[i] = new ChatComponentText(s);
			}
		}
		try {
			blockOwner = tagCompound.getString("blockOwner");
		} catch (Throwable ex2) {
			blockOwner = "";
		}
        try {
			blockIcon = tagCompound.getString("blockIcon");
		} catch (Throwable ex2) {
			blockIcon = "";
		}
    }
	
	 public void writeToNBT(NBTTagCompound tagCompound)
	    {
	        super.writeToNBT(tagCompound);
	        for (int i = 0; i < 4; ++i)
	        {
	            String s = IChatComponent.Serializer.componentToJson(this.signText[i]);
	            tagCompound.setString("Text" + (i + 1), s);
	        }
	        tagCompound.setString("blockOwner", blockOwner);
	        tagCompound.setString("blockIcon", blockIcon);
	    }
	
	public void updateSign() {
		super.worldObj.markBlockForUpdate(pos);
	}
	
	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(new UCSignServerMessage(pos.getX(), pos.getY(), pos.getZ(), signText));
	}
	
	@Override
	public Packet getDescriptionPacket() {
		IChatComponent[] aichatcomponent = new IChatComponent[4];
		System.arraycopy(this.signText, 0, aichatcomponent, 0, 4);
		return UniversalCoins.snw.getPacketFrom(new UCTileSignMessage(pos.getX(), 
				pos.getY(), pos.getZ(), signText, blockOwner, blockIcon));
    }
	
	public void sendTextureUpdateMessage(ItemStack stack) {
		if (!worldObj.isRemote) return;
			ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			String blockIcon = imm.getItemModel(stack).getTexture().getIconName();
			//the iconIndex function does not work with BOP so we have to do a bit of a hack here
			if (blockIcon.startsWith("biomesoplenty")){
				String[] iconInfo = blockIcon.split(":");
				String[] blockName = stack.getUnlocalizedName().split("\\.", 3);
				String woodType = blockName[2].replace("Plank", "");
				//hellbark does not follow the same naming convention
				if (woodType.contains("hell")) woodType = "hell_bark";
				blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
				//bamboo needs a hack too
				if (blockIcon.contains("bamboo")) blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
				//I feel dirty now :(
			}
		UniversalCoins.snw.sendToServer(new UCTextureMessage(pos.getX(), pos.getY(), pos.getZ(), blockIcon));
	}
	
	public void scanChestContents() {	
		TileEntity tileEntity = null;
		String[] itemName = {"", "", "", ""};
		int[] itemCount = {0, 0, 0, 0};
		int meta = super.getBlockMetadata();
		if (meta == 2) { tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)); }
		if (meta == 3) { tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)); }
		if (meta == 4) { tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())); }
		if (meta == 5) { tileEntity = worldObj.getTileEntity(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())); }
		if (tileEntity != null && tileEntity instanceof IInventory) {
			IInventory inventory = (IInventory) tileEntity;
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if (stack != null) {
					for (int j = 0; j < itemName.length; j++) {
						if (itemName[j] == "") {
							if (!itemName[0].matches(inventory.getStackInSlot(i).getDisplayName()) &&
								!itemName[1].matches(inventory.getStackInSlot(i).getDisplayName()) &&
								!itemName[2].matches(inventory.getStackInSlot(i).getDisplayName()) &&
								!itemName[3].matches(inventory.getStackInSlot(i).getDisplayName())) {
									itemName[j] = inventory.getStackInSlot(i).getDisplayName();
							}
						}
						if (itemName[j].matches(inventory.getStackInSlot(i).getDisplayName())) {
							itemCount[j] += inventory.getStackInSlot(i).stackSize;
						}
					}
				}
			}
			//if we have a double chest, scan it too
			if (tileEntity instanceof TileEntityChest) {
				TileEntity tileEntity2 = null;
				if (((TileEntityChest) tileEntity).adjacentChestXNeg != null ) 
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestXNeg);
				if (((TileEntityChest) tileEntity).adjacentChestXPos != null ) 
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestXPos);
				if (((TileEntityChest) tileEntity).adjacentChestZNeg != null ) 
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestZNeg);
				if (((TileEntityChest) tileEntity).adjacentChestZPos != null ) 
					tileEntity2 = (((TileEntityChest) tileEntity).adjacentChestZPos);
				if (tileEntity2 != null) {
					inventory = (IInventory) tileEntity2;
					for (int i = 0; i < inventory.getSizeInventory(); i++) {
						ItemStack stack = inventory.getStackInSlot(i);
						if (stack != null) {
							for (int j = 0; j < itemName.length; j++) {
								if (itemName[j] == "") {
									if (!itemName[0].matches(inventory.getStackInSlot(i).getDisplayName()) &&
										!itemName[1].matches(inventory.getStackInSlot(i).getDisplayName()) &&
										!itemName[2].matches(inventory.getStackInSlot(i).getDisplayName()) &&
										!itemName[3].matches(inventory.getStackInSlot(i).getDisplayName())) {
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
			//update sign with info collected
			for (int i = 0; i < itemName.length; i++) {
				if (itemName[i] != "") {
					IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(itemCount[i] + " " + itemName[i]);
					signText[i] = ichatcomponent;
				} else {
					signText[i] = new ChatComponentText("");
				}
				this.updateSign();
			}
		}
	}
}
