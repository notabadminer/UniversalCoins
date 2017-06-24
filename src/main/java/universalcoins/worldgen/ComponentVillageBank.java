package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.blocks.BlockATM;
import universalcoins.blocks.BlockUCWallSign;
import universalcoins.tileentity.TileUCSign;

public class ComponentVillageBank extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageBank(Start startPiece, int p5, Random random, StructureBoundingBox box, EnumFacing facing) {
		super(startPiece, p5);
		this.boundingBox = box;
		this.setCoordBaseMode(facing);
		MapGenStructureIO.registerStructureComponent(ComponentVillageBank.class, "ViUB");
	}

	public static ComponentVillageBank buildComponent(Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, EnumFacing facing, int p5) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 5, 6, 6,
				facing);
		return canVillageGoDeeper(box) && StructureComponent.findIntersecting(pieces, box) == null
				? new ComponentVillageBank(startPiece, p5, random, box, facing) : null;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox sbb) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);

			if (this.averageGroundLevel < 0)
				return true;

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.minY, 0);
		}

		// Clear area in case of sand
		fillWithAir(world, sbb, 0, 0, 0, 4, 4, 5);
		// start with block
		this.fillWithBlocks(world, sbb, 0, 0, 0, 4, 3, 5, Blocks.COBBLESTONE.getDefaultState(),
				Blocks.COBBLESTONE.getDefaultState(), false);
		// windows
		this.fillWithBlocks(world, sbb, 0, 2, 2, 4, 2, 3, Blocks.GLASS.getDefaultState(),
				Blocks.GLASS.getDefaultState(), false);
		// top
		fillWithBlocks(world, sbb, 0, 4, 1, 4, 4, 5, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(),
				false);
		// top front
		IBlockState iblockstate = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.NORTH);
		this.setBlockState(world, iblockstate, 1, 4, 1, sbb);
		this.setBlockState(world, iblockstate, 2, 4, 1, sbb);
		this.setBlockState(world, iblockstate, 3, 4, 1, sbb);
		// top back
		IBlockState iblockstate2 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.SOUTH);
		this.setBlockState(world, iblockstate2, 1, 4, 5, sbb);
		this.setBlockState(world, iblockstate2, 2, 4, 5, sbb);
		this.setBlockState(world, iblockstate2, 3, 4, 5, sbb);
		// top right
		IBlockState iblockstate3 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.WEST);
		this.setBlockState(world, iblockstate3, 4, 4, 0, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 1, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 2, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 3, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 4, sbb);
		this.setBlockState(world, iblockstate3, 4, 4, 5, sbb);
		// top left
		IBlockState iblockstate4 = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING,
				EnumFacing.EAST);
		this.setBlockState(world, iblockstate4, 0, 4, 0, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 1, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 2, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 3, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 4, sbb);
		this.setBlockState(world, iblockstate4, 0, 4, 5, sbb);

		// clear inside
		fillWithAir(world, sbb, 1, 1, 2, 3, 3, 3);
		// clear front
		fillWithAir(world, sbb, 0, 1, 0, 4, 4, 0);
		// door opening
		fillWithAir(world, sbb, 2, 1, 1, 2, 2, 1);
		// atm - meta, LR, TB, FB
		this.setBlockState(world,
				UniversalCoins.Blocks.atm.getDefaultState().withProperty(BlockATM.FACING, EnumFacing.SOUTH), 2, 2, 4,
				sbb);
		// door
		this.createVillageDoor(world, sbb, random, 2, 1, 1, EnumFacing.NORTH);
		// torches
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH), 1,
				2, 2, boundingBox);
		this.setBlockState(world, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH), 3,
				2, 2, boundingBox);
		// sign
		this.setBlockState(world, UniversalCoins.Blocks.wall_ucsign.getDefaultState()
				.withProperty(BlockUCWallSign.FACING, EnumFacing.SOUTH), 1, 2, 0, sbb);
		addSignText(world, boundingBox, 1, 2, 0);

		// add stairs if needed
		if (this.getBlockStateFromPos(world, 2, 0, -1, sbb).getMaterial() == Material.AIR
				&& this.getBlockStateFromPos(world, 2, -1, -1, sbb).getMaterial() != Material.AIR) {
			this.setBlockState(world,
					Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH), 2, 0, -1,
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

	protected void addSignText(World world, StructureBoundingBox boundingBox, int par4, int par5, int par6) {
		int i1 = this.getXWithOffset(par4, par6);
		int j1 = this.getYWithOffset(par5);
		int k1 = this.getZWithOffset(par4, par6);

		if (world.getTileEntity(new BlockPos(i1, j1, k1)) instanceof TileUCSign) {
			TileUCSign tileentitysign = (TileUCSign) world.getTileEntity(new BlockPos(i1, j1, k1));

			if (tileentitysign != null) {
				String signText[] = { "", "", "", "" };
				signText[1] = "Universal Bank";
				tileentitysign.signText[1] = new TextComponentString(signText[1]);
			}
		}
	}
}
