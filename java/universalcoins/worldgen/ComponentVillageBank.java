package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileUCSign;

public class ComponentVillageBank extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageBank(Start startPiece, int p5, Random random, StructureBoundingBox box, int p4) {
		super(startPiece, p5);
		this.coordBaseMode = p4;
		this.boundingBox = box;
		MapGenStructureIO.func_143031_a(ComponentVillageBank.class, "ViUB");
	}

	public static ComponentVillageBank buildComponent(Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, int p4, int p5) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 5, 5, 6, p4);
		return canVillageGoDeeper(box) && StructureComponent.findIntersecting(pieces, box) == null
				? new ComponentVillageBank(startPiece, p5, random, box, p4) : null;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox sbb) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);

			if (this.averageGroundLevel < 0)
				return true;

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 5 - 1, 0);

		}

		int meta = coordBaseMode + 2;
		if (meta > 3) {
			meta = meta - 4;
		}

		// Clear area
		fillWithAir(world, sbb, 0, 0, 0, 4, 4, 5);
		// start with block
		fillWithBlocks(world, sbb, 0, 0, 0, 4, 3, 5, Blocks.planks, Blocks.planks, false);
		// floor
		fillWithBlocks(world, sbb, 0, 0, 0, 4, 0, 5, Blocks.cobblestone, Blocks.cobblestone, false);
		// windows
		fillWithBlocks(world, sbb, 0, 2, 2, 4, 2, 3, Blocks.glass, Blocks.glass, false);
		// top
		fillWithBlocks(world, sbb, 1, 4, 2, 3, 4, 4, Blocks.planks, Blocks.planks, false);
		// top front
		fillWithMetadataBlocks(world, sbb, 1, 4, 1, 3, 4, 1, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 3), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 3), false);
		// top back
		fillWithMetadataBlocks(world, sbb, 1, 4, 5, 3, 4, 5, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 2), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 2), false);
		// top right
		fillWithMetadataBlocks(world, sbb, 4, 4, 1, 4, 4, 5, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 1), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 1), false);
		// top left
		fillWithMetadataBlocks(world, sbb, 0, 4, 1, 0, 4, 5, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 0), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 0), false);
		// clear inside
		fillWithAir(world, sbb, 1, 1, 2, 3, 3, 3);
		// clear front
		fillWithAir(world, sbb, 0, 1, 0, 4, 4, 0);
		// door opening
		fillWithAir(world, sbb, 2, 1, 1, 2, 2, 1);
		// atm - meta, LR, TB, FB
		placeBlockAtCurrentPosition(world, UniversalCoins.proxy.atm, meta, 2, 2, 4, boundingBox);
		// door
		placeDoorAtCurrentPosition(world, boundingBox, random, 2, 1, 1,
				this.getMetadataWithOffset(Blocks.wooden_door, 3));
		// torches
		placeBlockAtCurrentPosition(world, Blocks.torch, 0, 1, 2, 2, boundingBox);
		placeBlockAtCurrentPosition(world, Blocks.torch, 0, 3, 2, 2, boundingBox);
		// sign
		placeBlockAtCurrentPosition(world, UniversalCoins.proxy.wall_ucsign, getSignMeta(3), 1, 2, 0, boundingBox);
		addSignText(world, boundingBox, 1, 2, 0);

		// add stairs if needed
		if (this.getBlockAtCurrentPosition(world, 2, 0, -1, sbb).getMaterial() == Material.air
				&& this.getBlockAtCurrentPosition(world, 2, -1, -1, sbb).getMaterial() != Material.air) {
			this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs,
					this.getMetadataWithOffset(Blocks.stone_stairs, 3), 2, 0, -1, sbb);
		}

		// build foundation
		for (int k = 0; k < 6; ++k) { // length
			for (int l = 0; l < 5; ++l) {// width
				this.clearCurrentPositionBlocksUpwards(world, l, 7, k, sbb);
				this.func_151554_b(world, Blocks.cobblestone, 0, l, -1, k, sbb);
			}
		}

		return true;
	}

	protected void addSignText(World world, StructureBoundingBox boundingBox, int par4, int par5, int par6) {
		int i1 = this.getXWithOffset(par4, par6);
		int j1 = this.getYWithOffset(par5);
		int k1 = this.getZWithOffset(par4, par6);

		if (boundingBox.isVecInside(i1, j1, k1) && world.getBlock(i1, j1, k1) == UniversalCoins.proxy.wall_ucsign) {
			TileUCSign tileentitysign = (TileUCSign) world.getTileEntity(i1, j1, k1);

			if (tileentitysign != null) {
				String signText[] = { "", "", "", "" };
				signText[1] = "Universal Bank";
				tileentitysign.signText = signText;
			}
		}
	}

	private int getSignMeta(int meta) {
		// sign meta/rotation
		// 2=S ,3=N ,4=E ,5=W
		// coordBaseMode
		// 0=S ,1=W ,2=N ,3=E
		// returns meta value needed to rotate sign normally
		if (coordBaseMode == 0) {
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
		if (coordBaseMode == 1) {
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
		if (coordBaseMode == 2) {
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
		if (coordBaseMode == 3) {
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
}
