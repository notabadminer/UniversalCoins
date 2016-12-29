package universalcoins.blocks;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.render.BlockVendorRenderer;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorBlock;

public class BlockVendor extends BlockContainer {
	Block[] supportBlocks;

	IIcon iconTop, iconSide;

	public BlockVendor(Block[] supports) {
		super(Material.glass);

		supportBlocks = supports;
		setStepSound(soundTypeGlass);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setHardness(0.3F);
		setResistance(6000.0F);
		setBlockBounds(0.0625f, 0.125f, 0.0625f, 0.9375f, 0.9375f, 0.9375f);
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
			;
			if (tileVendor.inUse && !player.getDisplayName().contentEquals(tileVendor.playerName)) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return true;
			} else {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				tileVendor.playerName = player.getDisplayName();
				tileVendor.inUse = true;
				tileVendor.updateCoinsForPurchase();
				return true;
			}
		}
		return false;
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TileVendorBlock) {
			TileVendorBlock te = (TileVendorBlock) tentity;
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
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (world.isRemote)
			return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileVendorBlock) {
				TileVendorBlock tentity = (TileVendorBlock) te;
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
				tentity.infiniteMode = tagCompound.getBoolean("Infinite");
			}
			world.markBlockForUpdate(x, y, z);

		} else {
			// item has no owner so we'll set one and get out of here
			((TileVendorBlock) world.getTileEntity(x, y, z)).blockOwner = entity.getCommandSenderName();
		}
		int meta = stack.getItemDamage();
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileVendorBlock) world.getTileEntity(x, y, z)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, player, x, y, z);
			return true;
		}
		if (player.getDisplayName().matches(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, x, y, z);
			EntityItem entityItem = new EntityItem(world, x, y, z, stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, player, x, y, z);
			return true;
		}
		return false;
	}

	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		String ownerName = ((TileVendorBlock) world.getTileEntity(x, y, z)).blockOwner;
		if (player.getDisplayName().equals(ownerName)) {
			this.setHardness(0.3F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {

		return side < 2 ? iconTop : iconSide;
	}

	@Override
	public String getLocalizedName() {
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorBlock();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		iconTop = register.registerIcon("universalcoins:vendor_top");
		iconSide = register.registerIcon("universalcoins:vendor_side");
	}

	@Override
	public int getRenderType() {
		return BlockVendorRenderer.id;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return true;
	}

	@Override
	public void getSubBlocks(Item block, CreativeTabs creativeTabs, List list) {
		for (int i = 0; i < supportBlocks.length; ++i) {
			list.add(new ItemStack(block, 1, i));
		}
	}
}
