package universalcoins.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileVendorBlock;

public class BlockTradeStation extends BlockContainer {

	IIcon blockIcon, blockIconFace;

	public BlockTradeStation() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000.0F);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister registerer) {
		blockIcon = registerer.registerIcon("universalcoins:tradestation");
		blockIconFace = registerer.registerIcon("universalcoins:tradestation_face");
	}

	public IIcon getIcon(int par1, int par2) {
		if (par1 == 0 || par1 == 1) {
			return blockIcon;
		}
		return blockIconFace;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
			float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof TileTradeStation) {
			TileTradeStation tileTrade = (TileTradeStation) world.getTileEntity(x, y, z);
			EntityPlayer playerTest = world.getPlayerEntityByName(tileTrade.playerName);
			if (playerTest == null || !tileTrade.isUseableByPlayer(playerTest)) {
				tileTrade.inUse = false;
			}
			if (tileTrade.inUse && !player.getDisplayName().contentEquals(tileTrade.playerName)) {
				if (!world.isRemote) {
					player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse")));
				}
				return false;
			}
			if (((TileTradeStation) tileEntity).publicAccess) {
				player.openGui(UniversalCoins.instance, 0, world, x, y, z);
				((TileTradeStation) tileEntity).playerName = player.getDisplayName();
				((TileTradeStation) tileEntity).inUse = true;
				return true;
			} else {
				if (((TileTradeStation) tileEntity).blockOwner.matches(player.getDisplayName())) {
					player.openGui(UniversalCoins.instance, 0, world, x, y, z);
					((TileTradeStation) tileEntity).playerName = player.getDisplayName();
					((TileTradeStation) tileEntity).inUse = true;
					return true;
				}
				if (!world.isRemote) {
					player.addChatMessage(
							new ChatComponentText(StatCollector.translateToLocal("chat.warning.private")));
				}
			}
		}
		return false;
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TileTradeStation) {
			TileTradeStation te = (TileTradeStation) tentity;
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
			tagCompound.setLong("CoinsLeft", te.coinSum);
			tagCompound.setInteger("AutoMode", te.autoMode);
			tagCompound.setInteger("CoinMode", te.coinMode);
			tagCompound.setInteger("ItemPrice", te.itemPrice);
			tagCompound.setString("CustomName", te.getInventoryName());
			stack.setTagCompound(tagCompound);
		}
		return stack;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		if (world.isRemote)
			return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileTradeStation) {
				TileTradeStation tentity = (TileTradeStation) te;
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
				tentity.coinSum = tagCompound.getInteger("CoinsLeft");
				tentity.autoMode = tagCompound.getInteger("AutoMode");
				tentity.coinMode = tagCompound.getInteger("CoinMode");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.customName = tagCompound.getString("CustomName");
				tentity.blockOwner = ((EntityPlayerMP) player).getDisplayName();
			}
			world.markBlockForUpdate(x, y, z);
		} else if (stack.hasDisplayName()) {
			((TileTradeStation) world.getTileEntity(x, y, z)).setInventoryName(stack.getDisplayName());
		} else {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileTradeStation) {
				TileTradeStation tentity = (TileTradeStation) te;
				tentity.blockOwner = player.getCommandSenderName();
			}
		}
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileTradeStation) world.getTileEntity(x, y, z)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, player, x, y, z);
			return false;
		}
		if (player.getDisplayName().matches(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, x, y, z);
			EntityItem entityItem = new EntityItem(world, x, y, z, stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, player, x, y, z);
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileTradeStation();
	}
}
