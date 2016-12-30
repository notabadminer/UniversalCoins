package universalcoins.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileTradeStation;

public class BlockTradeStation extends BlockProtected {

	public BlockTradeStation() {
		super(Material.IRON);
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileTradeStation) {
			if (((TileTradeStation) tileEntity).inUse) {
				if (!world.isRemote) {
					player.addChatMessage(new TextComponentString(I18n.translateToLocal("chat.warning.inuse")));
				}
				return true;
			}
			if (((TileTradeStation) tileEntity).publicAccess) {
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				((TileTradeStation) tileEntity).playerName = player.getName();
				((TileTradeStation) tileEntity).inUse = true;
				return true;
			} else {
				if (((TileTradeStation) tileEntity).blockOwner.matches(player.getName())) {
					player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
					((TileTradeStation) tileEntity).playerName = player.getName();
					((TileTradeStation) tileEntity).inUse = true;
					return true;
				}
				if (!world.isRemote) {
					player.addChatMessage(new TextComponentString(I18n.translateToLocal("chat.warning.private")));
				}
			}
		}
		return false;
	}
	
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		if (world.isRemote)
			return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(pos);
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
				tentity.blockOwner = player.getName();
			}
		} else {
			((TileTradeStation) world.getTileEntity(pos)).blockOwner = player.getName();
		}
		if (stack.hasDisplayName()) {
			((TileTradeStation) world.getTileEntity(pos)).setName(stack.getDisplayName());
		}
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTradeStation();
	}
}
