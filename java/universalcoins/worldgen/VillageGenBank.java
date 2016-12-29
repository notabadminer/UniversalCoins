package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import universalcoins.UniversalCoins;

public class VillageGenBank implements IVillageCreationHandler {

	@Override
	public PieceWeight getVillagePieceWeight(Random random, int i) {
		return new StructureVillagePieces.PieceWeight(ComponentVillageBank.class, UniversalCoins.bankGenWeight, 1);
	}

	@Override
	public Class<?> getComponentClass() {
		return ComponentVillageBank.class;
	}

	@Override
	public Object buildComponent(PieceWeight villagePiece, Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, int p4, int p5) {
		return ComponentVillageBank.buildComponent(startPiece, pieces, random, p1, p2, p3, p4, p5);
	}

}
