package universalcoins.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
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
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockUCWallSign;
import universalcoins.tileentity.TileVendor;
import universalcoins.util.UCItemPricer;

public class ComponentVillageShop extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageShop() {
	}

	public ComponentVillageShop(Start startPiece, int p5, Random random, StructureBoundingBox box, EnumFacing facing) {
		super(startPiece, p5);
		this.boundingBox = box;
		//TODO:Following line is suspect. Changed from obfuscated name based on signature.
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

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 5 - 1, 0);
		}

		// Clear area
		fillWithAir(world, sbb, 0, 0, 0, 5, 6, 7);
		// start with block
		this.fillWithBlocks(world, sbb, 0, 0, 0, 5, 0, 7, Blocks.DOUBLE_STONE_SLAB.getDefaultState(),
				Blocks.DOUBLE_STONE_SLAB.getDefaultState(), false);
		// main wall
		this.fillWithBlocks(world, sbb, 0, 1, 0, 5, 1, 7, Blocks.PLANKS.getDefaultState(),
				Blocks.PLANKS.getDefaultState(), false);
		// front
		this.fillWithBlocks(world, sbb, 0, 2, 0, 5, 3, 0, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		// back
		this.fillWithBlocks(world, sbb, 0, 2, 7, 5, 3, 7, Blocks.OAK_FENCE.getDefaultState(),
				Blocks.OAK_FENCE.getDefaultState(), false);
		// top
		this.fillWithBlocks(world, sbb, 0, 4, 0, 5, 4, 7, Blocks.PLANKS.getDefaultState(),
				Blocks.PLANKS.getDefaultState(), false);
		this.fillWithBlocks(world, sbb, 1, 5, 1, 4, 5, 6, Blocks.PLANKS.getDefaultState(),
				Blocks.PLANKS.getDefaultState(), false);
		// clear main
		fillWithAir(world, sbb, 1, 1, 1, 4, 4, 6);
		// clear door
		fillWithAir(world, sbb, 2, 1, 0, 3, 2, 0);
		// clear back
		fillWithAir(world, sbb, 1, 2, 7, 4, 2, 7);
		// torches
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH), 1,
				4, 1, boundingBox);
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH), 4,
				4, 1, boundingBox);
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 1,
				4, 6, boundingBox);
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 4,
				4, 6, boundingBox);
		// signs
		this.fillWithBlocks(world, sbb, 1, 1, 1, 1, 1, 6,
				UniversalCoins.proxy.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.EAST),
				UniversalCoins.proxy.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.EAST),
				false);
		this.fillWithBlocks(world, sbb, 4, 1, 1, 4, 1, 6,
				UniversalCoins.proxy.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.WEST),
				UniversalCoins.proxy.wall_ucsign.getDefaultState().withProperty(BlockUCWallSign.FACING,
						EnumFacing.WEST),
				false);
		// vending blocks
		this.fillWithBlocks(world, sbb, 0, 2, 1, 0, 2, 6, UniversalCoins.proxy.vendor.getDefaultState(),
				UniversalCoins.proxy.vendor.getDefaultState(), false);
		this.fillWithBlocks(world, sbb, 5, 2, 1, 5, 2, 6, UniversalCoins.proxy.vendor.getDefaultState(),
				UniversalCoins.proxy.vendor.getDefaultState(), false);

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
					Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 1, 0, -1,
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
