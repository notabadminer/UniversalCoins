package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;


public class BlockVendorFrame extends BlockContainer {
	
	private final String name = "blockVendorFrame";
		
	public BlockVendorFrame() {
		super(new Material(MapColor.woodColor));
		setHardness(1.0f);
		setResistance(6000.0F);
		setBlockBounds(0, 0, 0, 0, 0, 0);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + ":" + name);
	}
	
	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		ItemStack stack = new ItemStack(this, 1, 0);
		TileEntity tentity = world.getTileEntity(pos);
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
			}tagCompound.setTag("Inventory", itemList);
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
	
	public int getRenderType() {
        return -1;
    }
	
	/**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        IBlockState state = null;
		return super.getCollisionBoundingBox(world, new BlockPos(x, y, z), state);
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
        this.getBlockBoundsFromMeta(0);
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBox(world, new BlockPos(x, y, z)); // .getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    public void getBlockBoundsFromMeta(int meta) {
        if (meta == 0) {
            this.setBlockBounds(0.12f, 0.12f, 0f, 0.88f, 0.88f, 0.07f);
        }
        if (meta == 1) {
            this.setBlockBounds(0.93f, 0.12f, 0.12f, 1.0f, 0.88f, 0.88f);
        }
        if (meta == 2) {
            this.setBlockBounds(0.12f, 0.12f, 0.93f, 0.88f, 0.88f, 1.00f);
        }
        if (meta == 3) {
            this.setBlockBounds(0.07f, 0.12f, 0.12f, 0f, 0.88f, 0.88f);
        }
    }
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (((TileVendor) tileEntity).inUse) {
			if (!world.isRemote) { player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse"))); }
			return true;
		} else {
			player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			((TileVendor) tileEntity).playerName = player.getName();
			return true;
		}
	}
		
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		//set block meta so we can use it later for rotation
		int rotation = MathHelper.floor_double((double)((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		//world.setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), rotation, 2);
		if (world.isRemote) return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileVendorFrame) {
				TileVendorFrame tentity = (TileVendorFrame) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
				if (tagCompound == null) {
					return;
				}
				NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
					byte slot = tag.getByte("Slot");
					if (slot < tentity.getSizeInventory()) {
						tentity.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
					}
				}
				tentity.coinSum = tagCompound.getInteger("CoinSum");
				tentity.userCoinSum = tagCompound.getInteger("UserCoinSum");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.blockOwner = player.getCommandSenderEntity().getName(); //always set to whomever place the block
				tentity.infiniteMode = tagCompound.getBoolean("Infinite");
				tentity.blockIcon = tagCompound.getString("blockIcon");
			}
			world.markBlockForUpdate(pos);	
		}
	}
	
	@Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		String ownerName = ((TileVendorFrame)world.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.harvestBlock(world, player, pos, state, te);
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, pos.getX(), pos.getY(), pos.getZ());
			EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.spawnEntityInWorld(entityItem);
			super.harvestBlock(world, player, pos, state, te);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorFrame();
	}
}
