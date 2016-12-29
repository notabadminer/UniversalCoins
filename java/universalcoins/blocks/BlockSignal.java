package universalcoins.blocks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSignal;

public class BlockSignal extends BlockContainer {

	public BlockSignal() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
		setBlockTextureName("universalcoins:signalblock");
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		if (player.isSneaking()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te != null && te instanceof TileUCSignal) {
				TileUCSignal tentity = (TileUCSignal) te;
				if (player.getCommandSenderName().matches(tentity.blockOwner)) {
					player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				}
			}
		} else {
			// take coins and activate on click
			ItemStack[] inventory = player.inventory.mainInventory;
			TileUCSignal tentity = (TileUCSignal) world.getTileEntity(x, y, z);
			int coinsFound = 0;
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack == null)
					continue;
				switch (stack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinsFound = UniversalCoins.coinValues[0] * stack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
					break;
				case "item.gold_coin":
					coinsFound = UniversalCoins.coinValues[1] * stack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
					break;
				case "item.emerald_coin":
					coinsFound = UniversalCoins.coinValues[2] * stack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
					break;
				case "item.diamond_coin":
					coinsFound = UniversalCoins.coinValues[3] * stack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
					break;
				case "item.obsidian_coin":
					coinsFound = UniversalCoins.coinValues[4] * stack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
					break;
				}
				if (coinsFound > tentity.fee)
					break;
			}
			if (world.isRemote)
				return false;
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
			// return excess coins
			ItemStack stack = null;
			while (coinsFound > 0) {
				if (coinsFound > UniversalCoins.coinValues[4]) {
					stack = new ItemStack(UniversalCoins.proxy.obsidian_coin, 1);
					stack.stackSize = (int) Math.floor(coinsFound / UniversalCoins.coinValues[4]);
					coinsFound -= stack.stackSize * UniversalCoins.coinValues[4];
				} else if (coinsFound > UniversalCoins.coinValues[3]) {
					stack = new ItemStack(UniversalCoins.proxy.diamond_coin, 1);
					stack.stackSize = (int) Math.floor(coinsFound / UniversalCoins.coinValues[3]);
					coinsFound -= stack.stackSize * UniversalCoins.coinValues[3];
				} else if (coinsFound > UniversalCoins.coinValues[2]) {
					stack = new ItemStack(UniversalCoins.proxy.emerald_coin, 1);
					stack.stackSize = (int) Math.floor(coinsFound / UniversalCoins.coinValues[2]);
					coinsFound -= stack.stackSize * UniversalCoins.coinValues[2];
				} else if (coinsFound > UniversalCoins.coinValues[1]) {
					stack = new ItemStack(UniversalCoins.proxy.gold_coin, 1);
					stack.stackSize = (int) Math.floor(coinsFound / UniversalCoins.coinValues[1]);
					coinsFound -= stack.stackSize * UniversalCoins.coinValues[1];
				} else if (coinsFound >= UniversalCoins.coinValues[0]) {
					stack = new ItemStack(UniversalCoins.proxy.iron_coin, 1);
					stack.stackSize = (int) Math.floor(coinsFound / UniversalCoins.coinValues[0]);
					coinsFound -= stack.stackSize * UniversalCoins.coinValues[0];
				}

				// add a stack to the recipients inventory
				if (player.inventory.getFirstEmptyStack() != -1) {
					player.inventory.addItemStackToInventory(stack);
				} else {
					for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
						ItemStack istack = player.inventory.getStackInSlot(i);
						if (istack != null && istack.getItem() == stack.getItem()) {
							int amountToAdd = (int) Math.min(stack.stackSize,
									istack.getMaxStackSize() - istack.stackSize);
							istack.stackSize += amountToAdd;
							stack.stackSize -= amountToAdd;
						}
					}
					// at this point, we're going to throw extra to the world
					// since the player inventory must be full.
					Random rand = new Random();
					float rx = rand.nextFloat() * 0.8F + 0.1F;
					float ry = rand.nextFloat() * 0.8F + 0.1F;
					float rz = rand.nextFloat() * 0.8F + 0.1F;
					EntityItem entityItem = new EntityItem(world, player.posX + rx, player.posY + ry, player.posZ + rz,
							stack);
					world.spawnEntityInWorld(entityItem);
				}
			}
		}
		return true;
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote)
			return;
		int rotation = MathHelper.floor_double((double) ((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null) {
			((TileUCSignal) world.getTileEntity(x, y, z)).blockOwner = player.getCommandSenderName();
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileUCSignal();
	}

	public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side) {
		TileUCSignal tentity = (TileUCSignal) block.getTileEntity(x, y, z);
		if (tentity.canProvidePower) {
			return 15;
		} else {
			return 0;
		}
	}

	public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side) {
		return isProvidingWeakPower(block, x, y, z, side);
	}

	public boolean canProvidePower() {
		return true;
	}
}