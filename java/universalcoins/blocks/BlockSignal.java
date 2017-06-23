package universalcoins.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileSignal;

public class BlockSignal extends BlockProtected {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockSignal() {
		super(Material.IRON);
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileSignal) {
				TileSignal tentity = (TileSignal) te;
				if (player.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
					player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		} else {
			// take coins and activate on click
			NonNullList<ItemStack> inventory = player.inventory.mainInventory;
			TileSignal tentity = (TileSignal) world.getTileEntity(pos);
			int coinsFound = 0;
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null) {
					switch (stack.getItem().getUnlocalizedName()) {
					case "item.iron_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[0];
						player.inventory.setInventorySlotContents(i, null);
						break;
					case "item.gold_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[1];
						player.inventory.setInventorySlotContents(i, null);
						break;
					case "item.emerald_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[2];
						player.inventory.setInventorySlotContents(i, null);
						break;
					case "item.diamond_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[3];
						player.inventory.setInventorySlotContents(i, null);
						break;
					case "item.obsidian_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[4];
						player.inventory.setInventorySlotContents(i, null);
						break;
					}
				}
				if (coinsFound > tentity.fee)
					break;
			}
			if (world.isRemote)
				return true; // we don't want to do the rest on client side
			if (coinsFound < tentity.fee) {
				player.sendMessage(new TextComponentString(I18n.translateToLocal("signal.message.notenough")));
			} else {
				// we have enough coins to cover the fee so we pay it and
				// return
				// the change
				player.sendMessage(new TextComponentString(I18n.translateToLocal("signal.message.activated")));
				coinsFound -= tentity.fee;
				tentity.activateSignal();
			}
			ItemStack stack = null;
			while (coinsFound > 0) {
				if (coinsFound >= UniversalCoins.coinValues[4]) {
					stack = new ItemStack(UniversalCoins.proxy.obsidian_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[4]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[4];
				} else if (coinsFound >= UniversalCoins.coinValues[3]) {
					stack = new ItemStack(UniversalCoins.proxy.diamond_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[3]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[3];
				} else if (coinsFound >= UniversalCoins.coinValues[2]) {
					stack = new ItemStack(UniversalCoins.proxy.emerald_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[2]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[2];
				} else if (coinsFound >= UniversalCoins.coinValues[1]) {
					stack = new ItemStack(UniversalCoins.proxy.gold_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[1]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[1];
				} else if (coinsFound >= UniversalCoins.coinValues[0]) {
					stack = new ItemStack(UniversalCoins.proxy.iron_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[0]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[0];
				}
				if (stack == null)
					break;
				// add a stack to the players inventory
				if (player.inventory.getFirstEmptyStack() != -1) {
					player.inventory.addItemStackToInventory(stack);
				} else {
					for (int j = 0; j < player.inventory.getSizeInventory(); j++) {
						ItemStack istack = player.inventory.getStackInSlot(j);
						if (istack != null && istack.getItem() == stack.getItem()) {
							int amountToAdd = (int) Math.min(stack.getCount(),
									istack.getMaxStackSize() - istack.getCount());
							istack.setCount(istack.getCount() + amountToAdd);
							stack.setCount(stack.getCount() - amountToAdd);
						}
					}
					if (stack.getCount() > 0) {
						// at this point, we're going to throw extra to the
						// world since
						// the player inventory must be full.
						Random rand = new Random();
						float rx = rand.nextFloat() * 0.8F + 0.1F;
						float ry = rand.nextFloat() * 0.8F + 0.1F;
						float rz = rand.nextFloat() * 0.8F + 0.1F;
						EntityItem entityItem = new EntityItem(world, ((EntityPlayerMP) player).posX + rx,
								((EntityPlayerMP) player).posY + ry, ((EntityPlayerMP) player).posZ + rz, stack);
						world.spawnEntity(entityItem);
					}
				}
			}
		}
		return true;

	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		world.setBlockToAir(pos);
		onBlockDestroyedByExplosion(world, pos, explosion);
		EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote)
			world.spawnEntity(entityItem);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
		super.onBlockPlacedBy(world, pos, state, player, stack);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileSignal) {
			TileSignal tentity = (TileSignal) te;
			tentity.blockOwner = player.getName();
		}
	}

	public void updatePower(World worldIn, BlockPos pos) {
		if (!worldIn.isRemote) {
			worldIn.notifyNeighborsOfStateChange(pos, this, true);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, true);
			}
		}
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		TileEntity te = blockAccess.getTileEntity(pos);
		if (te != null && te instanceof TileSignal) {
			TileSignal tentity = (TileSignal) te;
			return tentity.canProvidePower ? 15 : 0;
		}
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileSignal();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(FACING)).getIndex();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		java.util.List<ItemStack> ret = new java.util.ArrayList<ItemStack>();
		TileSignal te = world.getTileEntity(pos) instanceof TileSignal ? (TileSignal) world.getTileEntity(pos) : null;
		ItemStack stack = new ItemStack(UniversalCoins.proxy.signalblock, 1);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound tagCompound = new NBTTagCompound();
			te.writeToNBT(tag);
			tagCompound.setTag("BlockEntityTag", tag);
			stack.setTagCompound(tagCompound);
		}
		ret.add(stack);
		return ret;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		if (willHarvest)
			return true; // If it will harvest, delay deletion of the block
							// until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te,
			ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}
}