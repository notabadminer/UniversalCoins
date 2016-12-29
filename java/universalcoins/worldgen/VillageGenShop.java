package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import universalcoins.UniversalCoins;

public class VillageGenShop implements IVillageCreationHandler {

	@Override
	public PieceWeight getVillagePieceWeight(Random random, int i) {
		return new StructureVillagePieces.PieceWeight(ComponentVillageShop.class, UniversalCoins.shopGenWeight, 1);
	}

	@Override
	public Class<?> getComponentClass() {
		return ComponentVillageShop.class;
	}

	@Override
	public Object buildComponent(PieceWeight villagePiece, Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, int p4, int p5) {
		return ComponentVillageShop.buildComponent(startPiece, pieces, random, p1, p2, p3, p4, p5);
	}

}
