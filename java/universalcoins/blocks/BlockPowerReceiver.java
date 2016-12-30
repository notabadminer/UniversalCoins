package universalcoins.blocks;

import net.minecraft.block.Block;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TilePowerReceiver;

public class BlockPowerReceiver extends BlockProtected {

	public BlockPowerReceiver() {
		super(Material.IRON);
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TilePowerReceiver) {
			TilePowerReceiver tentity = (TilePowerReceiver) te;
			if (tentity.publicAccess || player.getName().matches(tentity.blockOwner)) {
				tentity.blockOwner = player.getName();
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
			if (!world.isRemote) {
				player.addChatMessage(
						new TextComponentString(I18n.translateToLocal("chat.warning.private")));
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
			if (te instanceof TilePowerReceiver) {
				TilePowerReceiver tentity = (TilePowerReceiver) te;
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
				tentity.resetPowerDirection();
				tentity.coinSum = tagCompound.getInteger("coinSum");
				tentity.rfLevel = tagCompound.getInteger("rfLevel");
			}
		}
		((TilePowerReceiver) world.getTileEntity(pos)).blockOwner = player.getName();
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePowerReceiver();
	}

	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
		TilePowerReceiver tileentity = (TilePowerReceiver) world.getTileEntity(pos);
		tileentity.resetPowerDirection();
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
}