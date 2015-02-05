package universalcoins.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import universalcoins.UniversalCoins;
import universalcoins.items.ItemEnderCard;

public class UCCraftingEventHandler {
	
	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
		
		IInventory craftMatrix = event.craftMatrix;
		
		if (event.crafting.getItem() instanceof ItemEnderCard) {
			if (craftMatrix.getStackInSlot(4).getTagCompound() != null) {
				event.crafting.getTagCompound().equals((NBTTagCompound) craftMatrix.getStackInSlot(4).getTagCompound().copy());
			}
		}
		
		//we can't compare the itemblock to the block class, so we just compare the unlocalized names here
		if (event.crafting.getItem().getUnlocalizedName().contentEquals(UniversalCoins.proxy.blockVendorFrame.getUnlocalizedName())) {
			String blockIcon = ""; //= craftMatrix.getStackInSlot(4). .getIconIndex().getIconName();
			//the iconIndex function does not work with BOP so we have to do a bit of a hack here
			if (blockIcon.startsWith("biomesoplenty")){
				String[] iconInfo = blockIcon.split(":");
				String[] blockName = craftMatrix.getStackInSlot(4).getUnlocalizedName().split("\\.", 3);
				String woodType = blockName[2].replace("Plank", "");
				//hellbark does not follow the same naming convention
				if (woodType.contains("hell")) woodType = "hell_bark";
				blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
				//bamboo needs a hack too
				if (blockIcon.contains("bamboo")) blockIcon = blockIcon.replace("plank_bambooThatching", "bamboothatching");
				//I feel dirty now :(
			}
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("blockIcon", blockIcon);
			event.crafting.setTagCompound(tag);
		}
		
		
	}
}
