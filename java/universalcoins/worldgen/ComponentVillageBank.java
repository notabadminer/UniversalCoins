package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraftforge.fml.common.FMLLog;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

public class ComponentVillageBank extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageBank(Start startPiece, int p5, Random random, StructureBoundingBox box, EnumFacing facing) {
		super(startPiece, p5);
		this.coordBaseMode = facing;
		this.boundingBox = box;
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

			this.boundingBox.offset(0, getPathHeight(world) - this.boundingBox.minY - 1, 0);
		}

		// fill under building with cobblestone
		buildFoundation(world, sbb);

		// Clear area twice in case of sand
		fillWithAir(world, sbb, 0, 0, 0, 4, 4, 5);
		// start with block
		this.fillWithBlocks(world, sbb, 0, 0, 0, 4, 3, 5, Blocks.stone.getDefaultState(),
				Blocks.stone.getDefaultState(), false);
		// windows
		this.fillWithBlocks(world, sbb, 0, 2, 2, 4, 2, 3, Blocks.glass.getDefaultState(),
				Blocks.glass.getDefaultState(), false);
		// roof
		this.fillWithBlocks(world, sbb, 0, 4, 1, 4, 4, 5, Blocks.stone_slab.getDefaultState(),
				Blocks.stone_slab.getDefaultState(), false);
		this.fillWithBlocks(world, sbb, 1, 4, 2, 3, 4, 4, Blocks.double_stone_slab.getDefaultState(),
				Blocks.double_stone_slab.getDefaultState(), false);
		// clear inside
		fillWithAir(world, sbb, 1, 1, 2, 3, 3, 3);
		// clear front
		fillWithAir(world, sbb, 0, 1, 0, 4, 4, 0);
		// door opening
		fillWithAir(world, sbb, 2, 1, 1, 2, 2, 1);
		// atm - meta, LR, TB, FB
		this.setBlockState(world, UniversalCoins.proxy.blockCardStation.getStateFromMeta(getCorrectedMeta(3)), 2, 2, 4,
				boundingBox);
		this.setBlockState(world, UniversalCoins.proxy.blockBase.getDefaultState(), 2, 1, 4, boundingBox);
		// door
		this.placeDoorCurrentPosition(world, boundingBox, random, 2, 1, 1,
				EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 3)));
		// torches
		this.setBlockState(world, Blocks.torch.getDefaultState(), 1, 2, 2, boundingBox);
		this.setBlockState(world, Blocks.torch.getDefaultState(), 3, 2, 2, boundingBox);
		// sign
		this.setBlockState(world, UniversalCoins.proxy.wall_ucsign.getStateFromMeta(getCorrectedMeta(3)), 1, 2, 0,
				boundingBox);
		addSignText(world, boundingBox, 1, 2, 0);

		return false;
	}

	protected void addSignText(World world, StructureBoundingBox boundingBox, int par4, int par5, int par6) {
		int i1 = this.getXWithOffset(par4, par6);
		int j1 = this.getYWithOffset(par5);
		int k1 = this.getZWithOffset(par4, par6);

		FMLLog.info("new sign at: " + i1 + " " + k1);

		if (world.getTileEntity(new BlockPos(i1, j1, k1)) instanceof TileUCSign) {
			TileUCSign tileentitysign = (TileUCSign) world.getTileEntity(new BlockPos(i1, j1, k1));

			if (tileentitysign != null) {
				String signText[] = { "", "", "", "" };
				signText[1] = "Universal Bank";
				tileentitysign.signText[1] = new ChatComponentText(signText[1]);
			}
		}
	}

	private int getCorrectedMeta(int meta) {
		// sign meta/rotation
		// 2=S ,3=N ,4=E ,5=W
		// coordBaseMode
		// 0=S ,1=W ,2=N ,3=E
		// returns meta value needed to rotate sign normally
		if (coordBaseMode == EnumFacing.SOUTH) {
			if (meta == 2)
				return 3;
			if (meta == 3)
				return 2;
			if (meta == 4)
				return 4;
			if (meta == 5)
				return 5;
			return 2;
		}
		if (coordBaseMode == EnumFacing.WEST) {
			if (meta == 2)
				return 4;
			if (meta == 3)
				return 5;
			if (meta == 4)
				return 2;
			if (meta == 5)
				return 3;
			return 4;
		}
		if (coordBaseMode == EnumFacing.NORTH) {
			if (meta == 2)
				return 2;
			if (meta == 3)
				return 3;
			if (meta == 4)
				return 4;
			if (meta == 5)
				return 5;
			return 3;
		}
		if (coordBaseMode == EnumFacing.EAST) {
			if (meta == 2)
				return 5;
			if (meta == 3)
				return 4;
			if (meta == 4)
				return 2;
			if (meta == 5)
				return 3;
			return 5;
		}
		return 5;
	}

	private int getPathHeight(World world) {
		int i1 = this.getXWithOffset(0, 0);
		int j1 = this.getYWithOffset(0);
		int k1 = this.getZWithOffset(0, 0);
		if (coordBaseMode == EnumFacing.SOUTH) {
			BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(i1 + 2, 40, k1 - 1));
			return pos.getY();
		}
		if (coordBaseMode == EnumFacing.WEST) {
			BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(i1 + 1, 40, k1 - 2));
			return pos.getY();
		}
		if (coordBaseMode == EnumFacing.NORTH) {
			BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(i1 - 2, 40, k1 + 1));
			return pos.getY();
		}
		if (coordBaseMode == EnumFacing.EAST) {
			BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(i1 - 1, 40, k1 + 2));
			return pos.getY();
		}
		return 0;
	}

	private void buildFoundation(World world, StructureBoundingBox sbb) {
		for (int i = sbb.minX; i <= sbb.maxX; i++) {
			for (int j = sbb.minZ; j <= sbb.maxZ; j++) {
				int scanPos = sbb.minY;
				if (isReplaceableBlock(world, i, scanPos, j)) {
					world.setBlockState(new BlockPos(i, scanPos, j), Blocks.cobblestone.getDefaultState());
				} else {
					continue;
				}
			}
		}
	}

	private boolean isReplaceableBlock(World world, int x, int y, int z) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		if (state.getBlock().getMaterial() == Material.air || state.getBlock().getMaterial() == Material.water
				|| state.getBlock().getMaterial() == Material.grass) {
			return true;
		}
		return false;
	}
}
