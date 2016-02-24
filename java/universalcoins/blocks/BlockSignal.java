package universalcoins.blocks;

import java.util.Random;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileSignal;

public class BlockSignal extends BlockRotatable {

	public BlockSignal() {
		super();
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileSignal) {
				TileSignal tentity = (TileSignal) te;
				if (player.getCommandSenderEntity().getName().matches(tentity.blockOwner)) {
					player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		} else {
			// take coins and activate on click
			ItemStack[] inventory = player.inventory.mainInventory;
			TileSignal tentity = (TileSignal) world.getTileEntity(pos);
			int coinsFound = 0;
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				for (int j = 0; j < tentity.coins.length; j++) {
					if (stack != null && stack.getItem() == tentity.coins[j]) {
						coinsFound += stack.stackSize * tentity.multiplier[j];
						player.inventory.setInventorySlotContents(i, null);
					}
				}
			}
			if (world.isRemote)
				return false; // we don't want to do the rest on client side
			if (coinsFound < tentity.fee) {
				player.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("signal.message.notenough")));
			} else {
				// we have enough coins to cover the fee so we pay it and return
				// the change
				player.addChatMessage(
						new ChatComponentText(StatCollector.translateToLocal("signal.message.activated")));
				coinsFound -= tentity.fee;
				tentity.activateSignal();
			}
			if (coinsFound > 0) {
				Random rand = new Random();
				while (coinsFound > 0) {
					float rx = rand.nextFloat() * 0.8F + 0.1F;
					float ry = rand.nextFloat() * 0.8F + 0.1F;
					float rz = rand.nextFloat() * 0.8F + 0.1F;
					int logVal = Math.min((int) (Math.log(coinsFound) / Math.log(9)), 4);
					int stackSize = Math.min((int) (coinsFound / Math.pow(9, logVal)), 64);
					EntityItem entityItem = new EntityItem(world, player.getPosition().getX() + rx,
							player.getPosition().getY() + ry, player.getPosition().getZ() + rz,
							new ItemStack(tentity.coins[logVal], stackSize));
					world.spawnEntityInWorld(entityItem);
					coinsFound -= Math.pow(9, logVal) * stackSize;
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
			world.spawnEntityInWorld(entityItem);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
		if (world.isRemote)
			return;
		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			((TileSignal) world.getTileEntity(pos)).blockOwner = player.getCommandSenderEntity().getName();
		}
	}

	public void updatePower(World worldIn, BlockPos pos) {
		if (!worldIn.isRemote) {
			worldIn.notifyNeighborsOfStateChange(pos, this);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}
		}
	}

	@Override
	public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
		TileEntity te = worldIn.getTileEntity(pos);
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
	public boolean canProvidePower() {
		return true;
	}
}