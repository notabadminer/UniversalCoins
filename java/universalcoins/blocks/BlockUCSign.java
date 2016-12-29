package universalcoins.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSign;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

public class BlockUCSign extends BlockSign {

	private Class signEntityClass;
	private boolean isStanding;

	public BlockUCSign(Class tileEntity, boolean standing) {
		super(tileEntity, standing);
		this.isStanding = standing;
		this.signEntityClass = tileEntity;
		float f = 0.25F;
		float f1 = 1.0F;
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
	}

	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		try {
			return (TileUCSign) this.signEntityClass.newInstance();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			if (tentity.blockOwner.matches(player.getDisplayName()) && isWoodPlank(player.getHeldItem())) {
				tentity.blockIcon = getPlankTexture(player.getHeldItem());
			} else if (player.getCommandSenderName().matches(tentity.blockOwner)) {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
			}
			return true;
		}
		return false;
	}

	private boolean isWoodPlank(ItemStack stack) {
		for (ItemStack oreStack : OreDictionary.getOres("plankWood")) {
			if (OreDictionary.itemMatches(oreStack, stack, false)) {
				return true;
			}
		}
		return false;
	}

	private String getPlankTexture(ItemStack stack) {
		String blockIcon = stack.getIconIndex().getIconName();
		// the iconIndex function does not work with BOP so we have to do a bit
		// of a hack here
		if (blockIcon.startsWith("biomesoplenty")) {
			String[] iconInfo = blockIcon.split(":");
			String[] blockName = stack.getUnlocalizedName().split("\\.", 3);
			String woodType = blockName[2].replace("Plank", "");
			// hellbark does not follow the same naming convention
			if (woodType.contains("hell"))
				woodType = "hell_bark";
			blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
			// bamboo needs a hack too
			if (blockIcon.contains("bamboo"))
				blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
		}
		return blockIcon;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileUCSign) world.getTileEntity(x, y, z)).blockOwner;
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
		String ownerName = ((TileUCSign) world.getTileEntity(x, y, z)).blockOwner;
		if (player.getDisplayName().equals(ownerName)) {
			this.setHardness(1.0F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(UniversalCoins.proxy.uc_sign);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TileUCSign) {
			TileUCSign te = (TileUCSign) tentity;
			NBTTagCompound tagCompound = new NBTTagCompound();
			tagCompound.setString("BlockIcon", te.blockIcon);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (block.getLocalizedName().matches("Chest") && tileEntity != null && tileEntity instanceof TileUCSign) {
			((TileUCSign) tileEntity).scanChestContents();
		}
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return UniversalCoins.proxy.uc_sign;
	}
}
