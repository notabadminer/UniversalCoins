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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TilePowerTransmitter;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendorBlock;

public class BlockPowerTransmitter extends BlockContainer {

	public BlockPowerTransmitter() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
		setBlockTextureName("universalcoins:power_transmitter");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TilePowerTransmitter) {
			TilePowerTransmitter tentity = (TilePowerTransmitter) te;
			if (tentity.publicAccess || player.getCommandSenderName().matches(tentity.blockOwner)) {
				tentity.playerName = player.getDisplayName();
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				return true;
			}
			if (!world.isRemote) {
				player.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("chat.warning.private")));
			}
		}
		return false;
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		world.setBlockToAir(x, y, z);
		onBlockDestroyedByExplosion(world, x, y, z, explosion);
		EntityItem entityItem = new EntityItem(world, x, y, z, new ItemStack(this, 1));
		if (!world.isRemote)
			world.spawnEntityInWorld(entityItem);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (world.isRemote)
			return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TilePowerTransmitter) {
				TilePowerTransmitter tentity = (TilePowerTransmitter) te;
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
				tentity.coinSum = tagCompound.getInteger("coinSum");
			}
			world.markBlockForUpdate(x, y, z);

		}
		((TilePowerTransmitter) world.getTileEntity(x, y, z)).blockOwner = entity.getCommandSenderName();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TilePowerTransmitter) world.getTileEntity(x, y, z)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, player, x, y, z);
			return false;
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, x, y, z);
			EntityItem entityItem = new EntityItem(world, x, y, z, stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, player, x, y, z);
		}
		return false;
	}
	
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		String ownerName = ((TilePowerTransmitter) world.getTileEntity(x, y, z)).blockOwner;
		if (player.getDisplayName().equals(ownerName)) {
			this.setHardness(3.0F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(UniversalCoins.proxy.power_transmitter);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TilePowerTransmitter) {
			TilePowerTransmitter te = (TilePowerTransmitter) tentity;
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
			tagCompound.setLong("coinSum", te.coinSum);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePowerTransmitter();
	}
}