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
		if (craft instanceof SquadronCraft sc && sc.getSquadron() != null) {
			MovecraftRotation rotation = MovecraftRotation.fromAction(clickType);

			final Squadron squadron = sc.getSquadron();
			final MovecraftLocation signLoc = MathUtils.bukkit2MovecraftLoc(sign.block().getLocation());

			if (!player.isSneaking()) {
				for (SquadronCraft scTmp : squadron.getCrafts()) {
					processSingleCraft(scTmp, rotation, signLoc);
				}
			} else {
				processSingleCraft(sc, rotation, signLoc);
			}

			return true;
		}
		else {
			return super.internalProcessSignWithCraft(clickType, sign, craft, player);
		}
	}

	protected void processSingleCraft(SquadronCraft craft, MovecraftRotation rotation, MovecraftLocation signLocation) {
		MovecraftLocation mLoc;
		if (craft.getType().getBoolProperty(CraftType.ROTATE_AT_MIDPOINT)) {
			mLoc = craft.getHitBox().getMidPoint();
		} else {
			mLoc = signLocation;
		}
		craft.rotate(rotation, mLoc, true);
	}
}
