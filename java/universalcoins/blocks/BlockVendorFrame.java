package universalcoins.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;

public class BlockVendorFrame extends BlockRotatable {

	public BlockVendorFrame() {
		setHardness(1.0f);
		setResistance(6000.0F);
		setBlockBounds(0, 0, 0, 0, 0, 0);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(UniversalCoins.proxy.blockVendorFrame, 1, 0);
		TileEntity tentity = world.getTileEntity(new BlockPos(x, y, z));
		if (tentity instanceof TileVendorFrame) {
			TileVendorFrame te = (TileVendorFrame) tentity;
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
			tagCompound.setInteger("CoinSum", te.coinSum);
			tagCompound.setInteger("UserCoinSum", te.userCoinSum);
			tagCompound.setInteger("ItemPrice", te.itemPrice);
			tagCompound.setString("BlockOwner", te.blockOwner);
			tagCompound.setBoolean("Infinite", te.infiniteMode);

			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isFullCube() {
		return false;
	}

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
		this.setBlockBoundsBasedOnState(worldIn, pos);
		return super.getSelectedBoundingBox(worldIn, pos);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos);
		EnumFacing facing = (EnumFacing) state.getValue(FACING);

		if (facing == EnumFacing.NORTH) {
			this.setBlockBounds(0.12f, 0.12f, 0f, 0.88f, 0.88f, 0.07f);
		}
		if (facing == EnumFacing.EAST) {
			this.setBlockBounds(0.93f, 0.12f, 0.12f, 1.0f, 0.88f, 0.88f);
		}
		if (facing == EnumFacing.SOUTH) {
			this.setBlockBounds(0.12f, 0.12f, 0.93f, 0.88f, 0.88f, 1.00f);
		}
		if (facing == EnumFacing.WEST) {
			this.setBlockBounds(0.07f, 0.12f, 0.12f, 0f, 0.88f, 0.88f);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileVendor) {
			TileVendor tentity = (TileVendor) tileEntity;
			if (tentity.inUse) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				tentity.updateEntity();
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				tentity.playerName = player.getName();
				tentity.inUse = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing()), 2);
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileVendorFrame) {
				TileVendorFrame tentity = (TileVendorFrame) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
				if (tagCompound.getString("BlockIcon") == "") {
					NBTTagList textureList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
					byte slot = tagCompound.getByte("Texture");
					ItemStack textureStack = ItemStack.loadItemStackFromNBT(tagCompound);
				}
				NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				if (tagList.tagCount() > 0) {
					for (int i = 0; i < tagList.tagCount(); i++) {
						NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
						byte slot = tag.getByte("Slot");
						if (slot < tentity.getSizeInventory()) {
							tentity.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
						}
					}
				}
				tentity.coinSum = tagCompound.getInteger("CoinSum");
				tentity.userCoinSum = tagCompound.getInteger("UserCoinSum");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.infiniteMode = tagCompound.getBoolean("Infinite");
				tentity.blockOwner = player.getName();
			}
			world.markBlockForUpdate(pos);
		} else {
			// Vending Frame pulled from NEI or creative. Cheaters :P
			((TileVendorFrame) world.getTileEntity(pos)).blockOwner = player.getName();
		}

	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		String ownerName = ((TileVendorFrame) world.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, pos, player, willHarvest);
			return false;
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, pos.getX(), pos.getY(), pos.getZ());
			EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, pos, player, willHarvest);
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorFrame();
	}
}
