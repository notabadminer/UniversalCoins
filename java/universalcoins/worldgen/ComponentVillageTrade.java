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

public class ComponentVillageTrade extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageTrade(Start startPiece, int p5, Random random, StructureBoundingBox box, int p4) {
		super(startPiece, p5);
		this.coordBaseMode = p4;
		this.boundingBox = box;
		MapGenStructureIO.func_143031_a(ComponentVillageTrade.class, "ViUT");
	}

	public static ComponentVillageTrade buildComponent(Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, int p4, int p5) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 4, 4, 4, p4);
		return canVillageGoDeeper(box) && StructureComponent.findIntersecting(pieces, box) == null
				? new ComponentVillageTrade(startPiece, p5, random, box, p4) : null;
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

		// Clear area
		fillWithAir(world, sbb, 0, 0, 0, 4, 4, 4);
		// floor
		fillWithBlocks(world, sbb, 0, 0, 0, 4, 0, 4, Blocks.cobblestone, Blocks.cobblestone, false);
		// fill
		fillWithBlocks(world, sbb, 0, 1, 0, 4, 3, 4, Blocks.fence, Blocks.fence, false);
		// top
		fillWithMetadataBlocks(world, sbb, 1, 4, 1, 3, 4, 3, Blocks.stained_glass, 15, Blocks.stained_glass, 15, false);
		// top front
		fillWithMetadataBlocks(world, sbb, 1, 4, 0, 3, 4, 0, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 3), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 3), false);
		// top back
		fillWithMetadataBlocks(world, sbb, 1, 4, 4, 3, 4, 4, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 2), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 2), false);
		// top right
		fillWithMetadataBlocks(world, sbb, 4, 4, 0, 4, 4, 4, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 1), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 1), false);
		// top left
		fillWithMetadataBlocks(world, sbb, 0, 4, 0, 0, 4, 4, Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 0), Blocks.oak_stairs,
				this.getMetadataWithOffset(Blocks.oak_stairs, 0), false);
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
		placeBlockAtCurrentPosition(world, UniversalCoins.proxy.trade_station, 0, 2, 1, 2, sbb);

		// add stairs if needed
		if (this.getBlockAtCurrentPosition(world, 2, 0, -1, sbb).getMaterial() == Material.air
				&& this.getBlockAtCurrentPosition(world, 2, -1, -1, sbb).getMaterial() != Material.air) {
			this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs,
					this.getMetadataWithOffset(Blocks.stone_stairs, 3), 2, 0, -1, sbb);
			this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs,
					this.getMetadataWithOffset(Blocks.stone_stairs, 2), 2, 0, 5, sbb);
			this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs,
					this.getMetadataWithOffset(Blocks.stone_stairs, 0), -1, 0, 2, sbb);
			this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs,
					this.getMetadataWithOffset(Blocks.stone_stairs, 1), 5, 0, 2, sbb);
		}

		// build foundation
		for (int k = 0; k < 5; ++k) { // length
			for (int l = 0; l < 5; ++l) {// width
				this.clearCurrentPositionBlocksUpwards(world, l, 5, k, sbb);
				this.func_151554_b(world, Blocks.cobblestone, 0, l, -1, k, sbb);
			}
		}

		return true;
	}
}
