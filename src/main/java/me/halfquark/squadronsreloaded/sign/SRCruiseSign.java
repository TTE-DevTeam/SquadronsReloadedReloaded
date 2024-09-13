package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.CruiseSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

public class SRCruiseSign extends CruiseSign implements ISquadronSign {

    public SRCruiseSign(String ident) {
        super(ident);
    }

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        return this.sq_canPlayerUseSignOn(player, craft, super::canPlayerUseSignOn);
    }

    @Override
    protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, Craft craft, Player player) {
        // Are we sure we want to keep that?
        if (craft instanceof SquadronCraft sc) {
            boolean onBoardCraft = false;
            for(Craft scTmp : sc.getSquadron().getCrafts()) {
                if (MathUtils.locationNearHitBox(craft.getHitBox(), player.getLocation(),2)) {
                    onBoardCraft = true;
                    break;
                }
            }
            if(!onBoardCraft)
                return false;
        }


        return super.internalProcessSignWithCraft(clickType, sign, craft, player);
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
