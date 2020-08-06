package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import universalcoins.UniversalCoins;

public class ComponentVillageTrade extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageTrade(Start startPiece, int p5, Random random, StructureBoundingBox box, EnumFacing facing) {
		super(startPiece, p5);
		this.setCoordBaseMode(facing);
		this.boundingBox = box;
		MapGenStructureIO.registerStructureComponent(ComponentVillageTrade.class, "ViUT");
	}

	public static ComponentVillageTrade buildComponent(Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2,
			int p3, EnumFacing facing, int p5) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 4, 4, 4,
				facing);
		return canVillageGoDeeper(box) && StructureComponent.findIntersecting(pieces, box) == null
				? new ComponentVillageTrade(startPiece, p5, random, box, facing) : null;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox sbb) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);

			if (this.averageGroundLevel < 0) {
				return true;
			}

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.minY, 0);
		}
		
		IBlockState iblockstate0 = Blocks.STAINED_GLASS.getStateFromMeta(15);
        IBlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
        IBlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
        IBlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
        IBlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        IBlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());

		// Clear area
		fillWithAir(world, sbb, 0, 0, 0, 4, 4, 4);
		// floor
		fillWithBlocks(world, sbb, 0, 0, 0, 4, 0, 4, iblockstate6,
				iblockstate6, false);
		// fill fence
		fillWithBlocks(world, sbb, 0, 1, 0, 4, 3, 4, iblockstate5,
				iblockstate5, false);
		// top
		this.setBlockState(world, iblockstate0, 1, 4, 1, sbb);
		this.setBlockState(world, iblockstate0, 2, 4, 1, sbb);
		this.setBlockState(world, iblockstate0, 3, 4, 1, sbb);
		this.setBlockState(world, iblockstate0, 1, 4, 2, sbb);
		this.setBlockState(world, iblockstate0, 2, 4, 2, sbb);
		this.setBlockState(world, iblockstate0, 3, 4, 2, sbb);
		this.setBlockState(world, iblockstate0, 1, 4, 3, sbb);
		this.setBlockState(world, iblockstate0, 2, 4, 3, sbb);
		this.setBlockState(world, iblockstate0, 3, 4, 3, sbb);
		// top front
		this.setBlockState(world, iblockstate1, 1, 4, 0, sbb);
		this.setBlockState(world, iblockstate1, 2, 4, 0, sbb);
		this.setBlockState(world, iblockstate1, 3, 4, 0, sbb);
		// top back
		this.setBlockState(world, iblockstate2, 1, 4, 4, sbb);
		this.setBlockState(world, iblockstate2, 2, 4, 4, sbb);
		this.setBlockState(world, iblockstate2, 3, 4, 4, sbb);
		// top right
		this.setBlockState(world, iblockstate3, 4, 4, 0, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 1, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 2, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 3, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 4, sbb);
		// top left
		this.setBlockState(world, iblockstate4, 0, 4, 0, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 1, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 2, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 3, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 4, sbb);
		// clear inside
		fillWithAir(world, sbb, 1, 1, 1, 3, 3, 3);
		// clear front door
		fillWithAir(world, sbb, 2, 1, 0, 2, 2, 0);
		// clear back door
		fillWithAir(world, sbb, 2, 1, 4, 2, 2, 4);
		// clear left door
		fillWithAir(world, sbb, 0, 1, 2, 0, 2, 2);
		// clear right door
		fillWithAir(world, sbb, 4, 1, 2, 4, 2, 2);

		// trade station
		this.setBlockState(world, UniversalCoins.Blocks.tradestation.getDefaultState(), 2, 1, 2, sbb);

		// add stairs if needed
		if (this.getBlockStateFromPos(world, 1, 0, -1, sbb).getMaterial() == Material.AIR
				&& this.getBlockStateFromPos(world, 1, -1, -1, sbb).getMaterial() != Material.AIR) {
			this.setBlockState(world,
					getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH)), 2, 0, -1,
					sbb);
			this.setBlockState(world,
					getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH)), 2, 0, 5,
					sbb);
			this.setBlockState(world,
					getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST)), -1, 0, 2,
					sbb);
			this.setBlockState(world,
					getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST)), 5, 0, 2,
					sbb);
		}

		// build foundation
		for (int k = 0; k < 5; ++k) { // length
			for (int l = 0; l < 5; ++l) {// width
				this.clearCurrentPositionBlocksUpwards(world, k, 7, l, sbb);
				this.replaceAirAndLiquidDownwards(world, iblockstate6, k, -1, l, sbb);
			}
		}

		return true;
	}
}
