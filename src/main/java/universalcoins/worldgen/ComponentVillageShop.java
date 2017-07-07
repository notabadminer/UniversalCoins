package universalcoins.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.block.BlockUCWallSign;
import universalcoins.tileentity.TileVendor;
import universalcoins.util.UCItemPricer;

public class ComponentVillageShop extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageShop() {
	}

	public ComponentVillageShop(Start startPiece, int p5, Random random, StructureBoundingBox box, EnumFacing facing) {
		super(startPiece, p5);
		this.boundingBox = box;
		this.setCoordBaseMode(facing);
		MapGenStructureIO.registerStructureComponent(ComponentVillageShop.class, "ViUS");
	}

	public static ComponentVillageShop buildComponent(Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, EnumFacing facing, int p5) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 5, 6, 6,
				facing);
		return canVillageGoDeeper(box) && StructureComponent.findIntersecting(pieces, box) == null
				? new ComponentVillageShop(startPiece, p5, random, box, facing) : null;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox sbb) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);

			if (this.averageGroundLevel < 0)
				return true;

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.minY, 0);
		}

		// Clear area
		fillWithAir(world, sbb, 0, 0, 0, 5, 6, 7);
		// start with block
		fillWithBlocks(world, sbb, 0, 0, 0, 5, 0, 7, Blocks.COBBLESTONE.getDefaultState(),
				Blocks.COBBLESTONE.getDefaultState(), false);
		// main wall
		fillWithBlocks(world, sbb, 0, 1, 0, 5, 1, 7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(),
				false);
		// fence front
		fillWithBlocks(world, sbb, 0, 2, 0, 5, 4, 0, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		fillWithAir(world, sbb, 1, 2, 0, 4, 3, 0);
		// fence back
		fillWithBlocks(world, sbb, 0, 2, 7, 5, 4, 7, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		fillWithAir(world, sbb, 1, 2, 7, 4, 3, 7);
		// fence left
		fillWithBlocks(world, sbb, 0, 3, 0, 0, 4, 7, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		fillWithAir(world, sbb, 0, 3, 1, 0, 3, 6);
		// fence right
		fillWithBlocks(world, sbb, 5, 3, 0, 5, 4, 7, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		fillWithAir(world, sbb, 5, 3, 1, 5, 3, 6);
		// clear doorway and path
		fillWithAir(world, sbb, 2, 1, 0, 3, 1, 7);
		// top
		fillWithBlocks(world, sbb, 1, 5, 1, 4, 5, 6, Blocks.STAINED_GLASS.getStateFromMeta(15),
				Blocks.STAINED_GLASS.getStateFromMeta(15), false);
		// top front
		IBlockState iblockstate = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.NORTH);
		this.setBlockState(world, iblockstate, 1, 5, 0, sbb);
		this.setBlockState(world, iblockstate, 2, 5, 0, sbb);
		this.setBlockState(world, iblockstate, 3, 5, 0, sbb);
		this.setBlockState(world, iblockstate, 4, 5, 0, sbb);
		// top back
		IBlockState iblockstate2 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.SOUTH);
		this.setBlockState(world, iblockstate2, 1, 5, 7, sbb);
		this.setBlockState(world, iblockstate2, 2, 5, 7, sbb);
		this.setBlockState(world, iblockstate2, 3, 5, 7, sbb);
		this.setBlockState(world, iblockstate2, 4, 5, 7, sbb);
		// top right
		IBlockState iblockstate3 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.WEST);
		this.setBlockState(world, iblockstate3, 5, 5, 0, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 1, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 2, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 3, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 4, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 5, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 6, sbb);
		this.setBlockState(world, iblockstate3, 5, 5, 7, sbb);
		// top left
		IBlockState iblockstate4 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.EAST);
		this.setBlockState(world, iblockstate4, 0, 5, 0, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 1, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 2, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 3, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 4, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 5, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 6, sbb);
		this.setBlockState(world, iblockstate4, 0, 5, 7, sbb);
		// signs
		this.fillWithBlocks(world, sbb, 1, 1, 1, 1, 1, 6,
				UniversalCoins.Blocks.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.EAST),
				UniversalCoins.Blocks.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.EAST),
				false);
		this.fillWithBlocks(world, sbb, 4, 1, 1, 4, 1, 6,
				UniversalCoins.Blocks.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.WEST),
				UniversalCoins.Blocks.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.WEST),
				false);
		// vending blocks
		this.fillWithBlocks(world, sbb, 0, 2, 1, 0, 2, 6, UniversalCoins.Blocks.vendor_block.getDefaultState(),
				UniversalCoins.Blocks.vendor_block.getDefaultState(), false);
		this.fillWithBlocks(world, sbb, 5, 2, 1, 5, 2, 6, UniversalCoins.Blocks.vendor_block.getDefaultState(),
				UniversalCoins.Blocks.vendor_block.getDefaultState(), false);

		// list of items
		List<Item> saleItems = new ArrayList();

		// fill left vending blocks
		for (int i = 0; i < 6; i++) {
			int priceModifier = random.nextInt(UniversalCoins.shopMaxPrice - UniversalCoins.shopMinPrice)
					+ UniversalCoins.shopMinPrice;
			ItemStack stack = UCItemPricer.getInstance().getRandomPricedStack();
			while (saleItems.contains(stack.getItem())) {
				stack = UCItemPricer.getInstance().getRandomPricedStack();
			}
			int price = UCItemPricer.getInstance().getItemPrice(stack);
			addVendorItems(world, 0, 2, i + 1, stack, price * priceModifier / 100);
			saleItems.add(stack.getItem());
		}
		// fill right vending blocks
		for (int i = 0; i < 6; i++) {
			int priceModifier = random.nextInt(UniversalCoins.shopMaxPrice - UniversalCoins.shopMinPrice)
					+ UniversalCoins.shopMinPrice;
			ItemStack stack = UCItemPricer.getInstance().getRandomPricedStack();
			while (saleItems.contains(stack.getItem())) {
				stack = UCItemPricer.getInstance().getRandomPricedStack();
			}
			int price = UCItemPricer.getInstance().getItemPrice(stack);
			addVendorItems(world, 5, 2, i + 1, stack, price * priceModifier / 100);
			saleItems.add(stack.getItem());
		}

		// add stairs if needed
		if (this.getBlockStateFromPos(world, 1, 0, -1, sbb).getMaterial() == Material.AIR
				&& this.getBlockStateFromPos(world, 1, -1, -1, sbb).getMaterial() != Material.AIR) {
			this.setBlockState(world,
					Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 2, 0, -1,
					sbb);
			this.setBlockState(world,
					Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 3, 0, -1,
					sbb);
			this.setBlockState(world,
					Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 2, 0, 8,
					sbb);
			this.setBlockState(world,
					Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH), 3, 0, 8,
					sbb);
		}

		// build foundation
		for (int l = 0; l < 6; ++l) {
			for (int k = 0; k < 5; ++k) {
				this.clearCurrentPositionBlocksUpwards(world, k, 7, l, sbb);
				this.replaceAirAndLiquidDownwards(world, Blocks.COBBLESTONE.getDefaultState(), k, -1, l, sbb);
			}
		}

		return true;
	}

	protected void addVendorItems(World world, int par4, int par5, int par6, ItemStack stack, int price) {
		int i1 = this.getXWithOffset(par4, par6);
		int j1 = this.getYWithOffset(par5);
		int k1 = this.getZWithOffset(par4, par6);

		TileEntity tentity = world.getTileEntity(new BlockPos(i1, j1, k1));
		if (tentity instanceof TileVendor) {
			TileVendor tileentity = (TileVendor) tentity;
			tileentity.infiniteMode = true;
			tileentity.itemPrice = price;
			tileentity.setSellItem(stack);
			tileentity.updateTE();
			tileentity.updateSigns();
		}
	}
}
