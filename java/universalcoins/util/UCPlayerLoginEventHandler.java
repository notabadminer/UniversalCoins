package universalcoins.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import universalcoins.UniversalCoins;
import universalcoins.net.UCRecipeMessage;

public class UCPlayerLoginEventHandler {
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		
		MinecraftServer server = MinecraftServer.getServer();
		if (!server.isSinglePlayer()) {
			//we need to update client with recipes that are enabled
			UniversalCoins.snw.sendTo(new UCRecipeMessage(), (EntityPlayerMP) event.player);
		}
	}
}
