package universalcoins.gui;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class HintGuiRenderer {
	public static HintGuiRenderer instance = new HintGuiRenderer();
	private static Minecraft mc = Minecraft.getMinecraft();

	@SubscribeEvent
	public void RenderGameOverlayEvent(
			net.minecraftforge.client.event.RenderGameOverlayEvent event) {

		// render everything onto the screen
		if (event.type == net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT) {
			HintGui gui = new HintGui();
			gui.renderToHud();

		}
	}
}
