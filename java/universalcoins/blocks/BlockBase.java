package universalcoins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameRegistry;
import universalcoins.UniversalCoins;

public class BlockBase extends Block {
	
	private final String name = "blockBase";
	
	public BlockBase() {
		super(new Material(MapColor.stoneColor));
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(6000000.0F);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(UniversalCoins.MODID + ":" + name);
	}
	
	public String getName() {
		return name;
	}

	@Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);
        EntityItem entityItem = new EntityItem( world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this, 1));
		if (!world.isRemote) world.spawnEntityInWorld(entityItem);
    }
}