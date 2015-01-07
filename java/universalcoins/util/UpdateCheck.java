package universalcoins.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import universalcoins.UniversalCoins;
import cpw.mods.fml.common.FMLLog;

public class UpdateCheck {

	public static String onlineVersion = null;

	public static boolean isUpdateAvailable() {
		try {
			BufferedReader versionFile = new BufferedReader(
					new InputStreamReader(
							new URL("https://dl.dropboxusercontent.com/s/h4hy30i1c8qsh2n/version.txt").openStream()));
			onlineVersion = versionFile.readLine();
			versionFile.close();
		} catch (MalformedURLException e) {
			FMLLog.warning("Universal Coins: Malformed update URL in update check");
		} catch (IOException e) {
			FMLLog.warning("Universal Coins: IO exception during update check");
		}
		
		String rawString = UniversalCoins.version;
		String splitString[] = rawString.split("-");
		int thisVersion = Integer.parseInt(splitString[1].replaceAll("[^\\d]", ""));
		
		if (onlineVersion == null) {
			return false;
		}
		
		String splitString2[] = onlineVersion.split("-");
		int latestVersion = Integer.parseInt(splitString2[1].replaceAll("[^\\d]", ""));
		
		if (latestVersion > thisVersion) {
			return true;
		}
		return false;
	}
}
