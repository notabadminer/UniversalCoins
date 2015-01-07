package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;

public class BlockBase extends BlockContainer {
	
	private final String name = "blockBase";
	
	public BlockBase() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0f);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		// TODO Auto-generated method stub
		return null;
	}
}