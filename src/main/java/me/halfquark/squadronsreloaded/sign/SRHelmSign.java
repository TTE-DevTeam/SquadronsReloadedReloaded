package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.sign.HelmSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

public class SRHelmSign extends HelmSign implements ISquadronSign {

	@Override
	protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
		return this.sq_canPlayerUseSignOn(player, craft, super::canPlayerUseSignOn);
	}

	@Override
	protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, Craft craft, Player player) {
		if (craft instanceof SquadronCraft sc && !player.isSneaking()) {
			MovecraftRotation rotation = MovecraftRotation.fromAction(clickType);

			if (!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(player.getLocation()))) {
				return false;
			} else {
				boolean onBoardCraft = false;
				final Squadron squadron = sc.getSquadron();
				for(SquadronCraft scTmp : squadron.getCrafts()) {
					if (MathUtils.locationNearHitBox(craft.getHitBox(),player.getLocation(),2)) {
						onBoardCraft = true;
						break;
					}
				}
				if(!onBoardCraft)
					return false;

				final MovecraftLocation signLoc = MathUtils.bukkit2MovecraftLoc(sign.block().getLocation());
				for(SquadronCraft scTmp : squadron.getCrafts()) {
					MovecraftLocation mLoc;
					if (craft.getType().getBoolProperty(CraftType.ROTATE_AT_MIDPOINT)) {
						mLoc = craft.getHitBox().getMidPoint();
					} else {
						mLoc = signLoc;
					}
					scTmp.rotate(rotation, mLoc, true);
				}

				return true;
			}
		}
		else {
			return super.internalProcessSignWithCraft(clickType, sign, craft, player);
		}
	}
}
