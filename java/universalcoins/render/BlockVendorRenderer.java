package universalcoins.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import universalcoins.util.Vending;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

//author AUTOMATIC_MAIDEN

public class BlockVendorRenderer implements ISimpleBlockRenderingHandler {
	public static int id;

	public BlockVendorRenderer(int i) {
		id = i;
	}

	void drawBlock(Block block, int meta, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
		tessellator.draw();
	}

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelID, RenderBlocks renderer) {
		drawBlock(block, meta, renderer);
		renderer.setRenderBounds(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
		drawBlock(Vending.supports[meta], 0, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);
		renderer.renderStandardBlock(block, x, y, z);
		renderer.setRenderBounds(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
		renderer.renderStandardBlock(Vending.supports[meta], x, y, z);
		return false;
	}

	@Override
	public int getRenderId() {
		return id;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
}
