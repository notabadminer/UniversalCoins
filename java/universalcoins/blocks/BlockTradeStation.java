package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileTradeStation;

public class BlockTradeStation extends BlockContainer {
	
	public BlockTradeStation() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int par6, float par7, float par8, float par9) {
		player.openGui(UniversalCoins.instance, 0, world, x, y, z);
		return true;
	}
	
	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TileTradeStation) {
			TileTradeStation te = (TileTradeStation) tentity;
			NBTTagList itemList = new NBTTagList();
			NBTTagCompound tagCompound = new NBTTagCompound();
			for (int i = 0; i < te.getSizeInventory(); i++) {
				ItemStack invStack = te.getStackInSlot(i);
				if (invStack != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setByte("Slot", (byte) i);
					invStack.writeToNBT(tag);
					itemList.appendTag(tag);
				}
			}
			tagCompound.setTag("Inventory", itemList);
			tagCompound.setInteger("CoinsLeft", te.coinSum);
			tagCompound.setInteger("AutoMode", te.autoMode);
			tagCompound.setInteger("CoinMode", te.coinMode);
			tagCompound.setInteger("ItemPrice", te.itemPrice);
			tagCompound.setString("CustomName", te.getInventoryName());
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}
		
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote) return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileTradeStation) {
				TileTradeStation tentity = (TileTradeStation) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
				if (tagCompound == null) {
					return;
				}
				NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
					byte slot = tag.getByte("Slot");
					if (slot >= 0 && slot < tentity.getSizeInventory()) {
						tentity.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
					}
				}
				tentity.coinSum = tagCompound.getInteger("CoinsLeft");
				tentity.autoMode = tagCompound.getInteger("AutoMode");
				tentity.coinMode = tagCompound.getInteger("CoinMode");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.customName = tagCompound.getString("CustomName");
			}
			world.markBlockForUpdate(x, y, z);
		} else if (stack.hasDisplayName()) {
            ((TileTradeStation)world.getTileEntity(x, y, z)).setInventoryName(stack.getDisplayName());
        }
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!world.isRemote) {
			if (!player.capabilities.isCreativeMode) {
				ItemStack stack = getItemStackWithData(world, x, y, z);
				EntityItem entityItem = new EntityItem(world, x, y, z, stack);
				world.spawnEntityInWorld(entityItem);
			}
			super.removedByPlayer(world, player, x, y, z);
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileTradeStation();
	}
}
