package universalcoins.worldgen;

import java.util.List;
import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import universalcoins.UniversalCoins;

public class VillageGenTrade implements IVillageCreationHandler {

	@Override
	public PieceWeight getVillagePieceWeight(Random random, int i) {
		return new StructureVillagePieces.PieceWeight(ComponentVillageTrade.class, UniversalCoins.tradeGenWeight, 1);
	}

	@Override
	public Class<?> getComponentClass() {
		return ComponentVillageTrade.class;
	}

	@Override
	public Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int p1,
            int p2, int p3, EnumFacing facing, int p5) {
		return ComponentVillageTrade.buildComponent(startPiece, pieces, random, p1, p2, p3, facing, p5);
	}

}
