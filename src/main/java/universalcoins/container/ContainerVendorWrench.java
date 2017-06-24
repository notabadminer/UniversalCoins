package universalcoins.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.tileentity.TileVendor;
import universalcoins.tileentity.TileVendorFrame;

public class ContainerVendorWrench extends Container {
	private TileVendor tileEntity;
	private TileVendorFrame tileEntity2;
	private String lastBlockOwner;
	private Boolean lastInfinite;

	public ContainerVendorWrench(InventoryPlayer inventory, TileVendor tEntity) {
		tileEntity = tEntity;
	}

	public ContainerVendorWrench(InventoryPlayer inventory, TileVendorFrame tEntity) {
		tileEntity2 = tEntity;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUsableByPlayer(player);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (this.lastBlockOwner != this.tileEntity.blockOwner
				|| this.lastInfinite != this.tileEntity.infiniteMode) {
			// update
			tileEntity.updateTE();
		}

		this.lastBlockOwner = this.tileEntity.blockOwner;
		this.lastInfinite = this.tileEntity.infiniteMode;

	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		if (par1 == 0) {
			// this.tileEntity.autoMode = par2;
		}
	}

	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		this.tileEntity.inUseCleanup();
	}
}
