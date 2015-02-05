package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileTradeStation;

public class BlockTradeStation extends BlockContainer {
	
	private final String name = "blockTradeStation";
	
	public BlockTradeStation() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + ":" + name);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (((TileTradeStation) tileEntity).inUse) {
			if (!world.isRemote) { player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse"))); }
		} else {
			player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getY());
			((TileTradeStation) tileEntity).playerName = player.getName();
		}
		return true;
	}
	
	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		ItemStack stack = new ItemStack(UniversalCoins.proxy.blockTradeStation, 1);
		TileEntity tentity = world.getTileEntity(pos);
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
			tagCompound.setInteger("CoinsLeft", te.coinSum);
			tagCompound.setInteger("AutoMode", te.autoMode);
			tagCompound.setInteger("CoinMode", te.coinMode);
			tagCompound.setInteger("ItemPrice", te.itemPrice);
			tagCompound.setString("CustomName", te.getInventoryName());
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}
		
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (worldIn.isRemote) return;
		if (stack.hasTagCompound()) {
			TileEntity te = worldIn.getTileEntity(pos);
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
			}
			worldIn.markBlockForUpdate(pos);
		} else if (stack.hasDisplayName()) {
            ((TileTradeStation)worldIn.getTileEntity(pos)).setInventoryName(stack.getDisplayName());
        }
	}
	
	@Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
		int xCoord = pos.getX();
		int yCoord = pos.getY();
		int zCoord = pos.getZ();
		if (!worldIn.isRemote) {
			if (!player.capabilities.isCreativeMode) {
				ItemStack stack = getItemStackWithData(worldIn, xCoord, yCoord, zCoord);
				EntityItem entityItem = new EntityItem(worldIn, xCoord, yCoord, zCoord, stack);
				worldIn.spawnEntityInWorld(entityItem);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileTradeStation();
	}
}
