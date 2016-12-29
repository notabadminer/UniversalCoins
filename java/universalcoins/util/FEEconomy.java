package universalcoins.util;

import java.text.DecimalFormat;

import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.economy.Economy;
import com.forgeessentials.api.economy.Wallet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StatCollector;

public class FEEconomy implements Economy {

	@Override
	public String currency(long arg0) {
		return toString(arg0);
	}

	@Override
	public Wallet getWallet(UserIdent ident) {
		return new FEWallet(UniversalAccounts.getInstance().getOrCreatePlayerAccount(ident.getUuid().toString()));
	}

	@Override
	public String toString(long arg0) {
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		return formatter.format(arg0) + " " + StatCollector.translateToLocal("item.itemCoin.name");
	}
}
