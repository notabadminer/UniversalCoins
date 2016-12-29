package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileATM;
import universalcoins.tile.TileTradeStation;

public class BlockATM extends BlockContainer {

	IIcon blockIcon;

	public BlockATM() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setBlockTextureName("universalcoins:atm");
		setResistance(30.0F);
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
		return -1;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof TileATM) {
			TileATM tileCardStation = (TileATM) world.getTileEntity(x, y, z);
			EntityPlayer playerTest = world.getPlayerEntityByName(tileCardStation.playerName);
			if (playerTest == null || !tileCardStation.isUseableByPlayer(playerTest)) {
				tileCardStation.inUse = false;
			}
			if (tileCardStation.inUse && !player.getDisplayName().contentEquals(tileCardStation.playerName)) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				tileCardStation.playerName = player.getDisplayName();
				tileCardStation.playerUID = player.getUniqueID().toString();
				tileCardStation.inUse = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote)
			return;
		int rotation = MathHelper.floor_double((double) ((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileATM) {
			TileATM tentity = (TileATM) te;
			tentity.blockOwner = player.getCommandSenderName();
			if (!stack.getDisplayName().matches(StatCollector.translateToLocal("tile.atm.name"))) {
				tentity.customName = stack.getDisplayName();
			}
		}
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileATM) world.getTileEntity(x, y, z)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, player, x, y, z);
			return true;
		}
		if (player.getDisplayName().matches(ownerName) && !world.isRemote) {
			super.removedByPlayer(world, player, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileATM();
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		world.setBlockToAir(x, y, z);
		onBlockDestroyedByExplosion(world, x, y, z, explosion);
		EntityItem entityItem = new EntityItem(world, x, y, z, new ItemStack(UniversalCoins.proxy.atm, 1));
		if (!world.isRemote)
			world.spawnEntityInWorld(entityItem);
	}
}
