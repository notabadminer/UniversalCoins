package universalcoins.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
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
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.tile.TilePackager;

public class BlockPackager extends BlockContainer {

	private IIcon blockIcon, blockIconFace;

	public BlockPackager() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon("universalcoins:packager");
		blockIconFace = par1IconRegister.registerIcon("universalcoins:packager_face");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return meta == 0 && side == 3 ? blockIconFace : (side == meta ? blockIconFace : blockIcon);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof TilePackager) {
			TilePackager tilePackager = (TilePackager) world.getTileEntity(x, y, z);
			EntityPlayer playerTest = world.getPlayerEntityByName(tilePackager.playerName);
			if (playerTest == null || !tilePackager.isUseableByPlayer(playerTest)) {
				tilePackager.inUse = false;
			}
			if (tilePackager.inUse && !player.getDisplayName().contentEquals(tilePackager.playerName)) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				tilePackager.playerName = player.getDisplayName();
				tilePackager.inUse = true;
				tilePackager.packageTarget = "";
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote)
			return;
		int l = MathHelper.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

		switch (l) {
		case 0:
			world.setBlockMetadataWithNotify(x, y, z, 2, l);
			break;
		case 1:
			world.setBlockMetadataWithNotify(x, y, z, 5, l);
			break;
		case 2:
			world.setBlockMetadataWithNotify(x, y, z, 3, l);
			break;
		case 3:
			world.setBlockMetadataWithNotify(x, y, z, 4, l);
			break;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePackager();
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
	public void onNeighborBlockChange(World world, int xCoord, int yCoord, int zCoord, Block block) {
		if (!world.isRemote && world.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			TileEntity tileEntity = world.getTileEntity(xCoord, yCoord, zCoord);
			if (tileEntity != null && tileEntity instanceof TilePackager) {
				TilePackager tilePackager = (TilePackager) world.getTileEntity(xCoord, yCoord, zCoord);
				tilePackager.onButtonPressed(0, false);
			}
		}
	}
}