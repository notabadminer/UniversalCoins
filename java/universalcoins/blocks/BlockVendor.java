package universalcoins.blocks;


import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;

public class BlockVendor extends BlockContainer {
	
	private final String name = "blockVendor";

	Block[] supportBlocks;

	public BlockVendor(Block[] supports) {
		super(Material.glass);	

		supportBlocks = supports;

		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setHardness(0.3F);
		setResistance(6000000.0F);
		setStepSound(Block.soundTypeGlass);
		setBlockBounds(0.0625f, 0.125f, 0.0625f, 0.9375f, 0.9375f, 0.9375f);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}

	/*@Override
	public void onBlockClicked(World world, int i, int j, int k, EntityPlayer entityplayer) {
		
	}*/

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		player.openGui(UniversalCoins.instance, 0, world, xCoord, yCoord, zCoord);
		return true;
	}
	
	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		ItemStack stack = new ItemStack(UniversalCoins.proxy.blockVendor, 1, 0);
		TileEntity tentity = world.getTileEntity(pos);
		if (tentity instanceof TileVendor) {
			TileVendor te = (TileVendor) tentity;
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
			tagCompound.setBoolean("Infinite", te.infiniteSell);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (world.isRemote) return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileVendor) {
				TileVendor tentity = (TileVendor) te;
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
				tentity.coinSum = tagCompound.getInteger("CoinSum");
				tentity.userCoinSum = tagCompound.getInteger("UserCoinSum");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.blockOwner = tagCompound.getString("BlockOwner");
				tentity.infiniteSell = tagCompound.getBoolean("Infinite");
			}
			world.markBlockForUpdate(pos);
			
		} else {
			//item has no owner so we'll set one and get out of here
			((TileVendor)world.getTileEntity(pos)).blockOwner = placer.getName();
		}
		int meta = stack.getItemDamage();
		//world.setBlockMetadataWithNotify(x, y, z, meta, 2);		
	}
	
	@Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		TileVendor tentity = (TileVendor) world.getTileEntity(pos);
		String ownerName = tentity.blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.harvestBlock(world, player, pos, state, te);
		}
		if (player.getDisplayName().toString().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, xCoord, yCoord, zCoord);
			EntityItem entityItem = new EntityItem(world, xCoord, yCoord, zCoord, stack);
			world.spawnEntityInWorld(entityItem);
			super.harvestBlock(world, player, pos, state, te);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendor();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	//@Override
	 public int damageDropped(IBlockState state) {
		 return this.getMetaFromState(state);
	 }

	@Override
	public int getRenderType() {
		return 0;
		//return BlockVendorRenderer.id;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item block, CreativeTabs creativeTabs, List list) {
		for (int i = 0; i < supportBlocks.length; ++i) {
			list.add(new ItemStack(block, 1, i));
		}
	}
}
