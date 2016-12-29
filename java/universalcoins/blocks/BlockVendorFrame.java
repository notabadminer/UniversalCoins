package universalcoins.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;

public class BlockVendorFrame extends BlockContainer {

	public BlockVendorFrame() {
		super(new Material(MapColor.woodColor));
		setHardness(1.0f);
		setBlockTextureName("minecraft:planks_oak"); // fixes missing texture on
														// block break
		setResistance(6000.0F);
		setBlockBounds(0, 0, 0, 0, 0, 0);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
		TileEntity tentity = world.getTileEntity(x, y, z);
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
			tagCompound.setString("BlockIcon", te.blockIcon);

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
	 * Returns a bounding box from the pool of bounding boxes (this means this
	 * box can change after the pool has been cleared to be reused)
	 */
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y,
	 * z
	 */
	public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
		this.getBlockBoundsFromMeta(block.getBlockMetadata(x, y, z));
	}

	/**
	 * Returns the bounding box of the wired rectangular prism to render.
	 */
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof TileVendor) {
			TileVendor tileVendor = (TileVendor) world.getTileEntity(x, y, z);
			EntityPlayer playerTest = world.getPlayerEntityByName(tileVendor.playerName);
			if (playerTest == null || !tileVendor.isUseableByPlayer(playerTest)) {
				tileVendor.inUse = false;
			}
			if (tileVendor.inUse && !player.getDisplayName().contentEquals(tileVendor.playerName)) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return true;
			}
			if (tileVendor.blockOwner.matches(player.getDisplayName()) && isWoodPlank(player.getHeldItem())) {
				tileVendor.blockIcon = getPlankTexture(player.getHeldItem());
			} else {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				tileVendor.playerName = player.getDisplayName();
				tileVendor.inUse = true;
				return true;
			}
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		// set block meta so we can use it later for rotation
		int rotation = MathHelper.floor_double((double) ((entity.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileVendorFrame) {
				TileVendorFrame tentity = (TileVendorFrame) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
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
				tentity.blockOwner = entity.getCommandSenderName();
				tentity.blockIcon = tagCompound.getString("BlockIcon");
			}
			world.markBlockForUpdate(x, y, z);
		} else {
			((TileVendorFrame) world.getTileEntity(x, y, z)).blockOwner = entity.getCommandSenderName();
		}

	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileVendorFrame) world.getTileEntity(x, y, z)).blockOwner;
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
		String ownerName = ((TileVendorFrame) world.getTileEntity(x, y, z)).blockOwner;
		if (player.getDisplayName().equals(ownerName)) {
			this.setHardness(1.0F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorFrame();
	}
}
