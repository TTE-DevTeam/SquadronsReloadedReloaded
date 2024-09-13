package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.DescendSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SRDescendSign extends DescendSign implements ISquadronSign {

	public SRDescendSign(String ident) {
		super(ident);
	}

	@Override
	protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
		return this.sq_canPlayerUseSignOn(player, craft, super::canPlayerUseSignOn);
	}

	@Override
	protected void setCraftCruising(Player player, CruiseDirection direction, Craft craft) {
		if (craft instanceof SquadronCraft sc) {
			Squadron squadron = sc.getSquadron();
			squadron.setCruiseDirection(direction);
			squadron.setCruising(true);

			for (SquadronCraft c : squadron.getCrafts()) {
				c.setLastCruiseUpdate(System.currentTimeMillis());
			}
		}
		else {
			super.setCraftCruising(player, direction, craft);
		}
	}

	@Override
	protected void onAfterStoppingCruise(Craft craft, SignListener.SignWrapper signWrapper, Player player) {
		super.onAfterStoppingCruise(craft, signWrapper, player);

		if (craft instanceof SquadronCraft sc) {
			Squadron sq = sc.getSquadron();
			sq.setCruising(false);
		}
	}
	
}
