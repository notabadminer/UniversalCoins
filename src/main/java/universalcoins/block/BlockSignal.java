package universalcoins.block;

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

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (playerIn.isSneaking()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof TileSignal) {
				TileSignal tentity = (TileSignal) te;
				if (playerIn.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
					playerIn.openGui(UniversalCoins.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		} else {
			// take coins and activate on click
			NonNullList<ItemStack> inventory = playerIn.inventory.mainInventory;
			TileSignal tentity = (TileSignal) worldIn.getTileEntity(pos);
			int coinsFound = 0;
			for (int i = 0; i < playerIn.inventory.getSizeInventory(); i++) {
				ItemStack stack = playerIn.inventory.getStackInSlot(i);
				if (stack != null) {
					switch (stack.getItem().getUnlocalizedName()) {
					case "item.universalcoins.iron_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[0];
						playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						break;
					case "item.universalcoins.gold_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[1];
						playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						break;
					case "item.universalcoins.emerald_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[2];
						playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						break;
					case "item.universalcoins.diamond_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[3];
						playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						break;
					case "item.universalcoins.obsidian_coin":
						coinsFound += stack.getCount() * UniversalCoins.coinValues[4];
						playerIn.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						break;
					}
				}
				if (coinsFound > tentity.fee)
					break;
			}
			if (worldIn.isRemote)
				return true; // we don't want to do the rest on client side
			if (coinsFound < tentity.fee) {
				playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("signal.message.notenough")));
			} else {
				// we have enough coins to cover the fee so we pay it and
				// return
				// the change
				playerIn.sendMessage(new TextComponentString(I18n.translateToLocal("signal.message.activated")));
				coinsFound -= tentity.fee;
				tentity.activateSignal();
			}
			ItemStack stack = null;
			while (coinsFound > 0) {
				if (coinsFound >= UniversalCoins.coinValues[4]) {
					stack = new ItemStack(UniversalCoins.Items.obsidian_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[4]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[4];
				} else if (coinsFound >= UniversalCoins.coinValues[3]) {
					stack = new ItemStack(UniversalCoins.Items.diamond_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[3]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[3];
				} else if (coinsFound >= UniversalCoins.coinValues[2]) {
					stack = new ItemStack(UniversalCoins.Items.emerald_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[2]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[2];
				} else if (coinsFound >= UniversalCoins.coinValues[1]) {
					stack = new ItemStack(UniversalCoins.Items.gold_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[1]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[1];
				} else if (coinsFound >= UniversalCoins.coinValues[0]) {
					stack = new ItemStack(UniversalCoins.Items.iron_coin, 1);
					stack.setCount((int) Math.floor(coinsFound / UniversalCoins.coinValues[0]));
					coinsFound -= stack.getCount() * UniversalCoins.coinValues[0];
				}
				if (stack == null)
					break;
				// add a stack to the players inventory
				if (playerIn.inventory.getFirstEmptyStack() != -1) {
					playerIn.inventory.addItemStackToInventory(stack);
				} else {
					for (int j = 0; j < playerIn.inventory.getSizeInventory(); j++) {
						ItemStack istack = playerIn.inventory.getStackInSlot(j);
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
						EntityItem entityItem = new EntityItem(worldIn, ((EntityPlayerMP) playerIn).posX + rx,
								((EntityPlayerMP) playerIn).posY + ry, ((EntityPlayerMP) playerIn).posZ + rz, stack);
						worldIn.spawnEntity(entityItem);
					}
				}
			}
		}
		return true;

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
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack stack = new ItemStack(UniversalCoins.Blocks.signalblock, 1);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound tagCompound = new NBTTagCompound();
			te.writeToNBT(tag);
			tagCompound.setTag("BlockEntityTag", tag);
			stack.setTagCompound(tagCompound);
		}
		drops.add(stack);
	}
}