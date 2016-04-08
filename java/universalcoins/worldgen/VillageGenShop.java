package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import universalcoins.UniversalCoins;

public class VillageGenShop implements IVillageCreationHandler {

	@Override
	public PieceWeight getVillagePieceWeight(Random random, int i) {
		return new StructureVillagePieces.PieceWeight(ComponentVillageShop.class, 20, 4);
		//return new StructureVillagePieces.PieceWeight(ComponentVillageShop.class, UniversalCoins.shopGenWeight, random.nextInt(4));
	}

	@Override
	public Class<?> getComponentClass() {
		return ComponentVillageShop.class;
	}

	@Override
	public Village buildComponent(PieceWeight villagePiece, Start startPiece, List pieces, Random random, int p1,
			int p2, int p3, EnumFacing facing, int p5) {
		return ComponentVillageShop.buildComponent(startPiece, pieces, random, p1, p2, p3, facing, p5);
	}

}
