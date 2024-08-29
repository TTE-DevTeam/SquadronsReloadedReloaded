package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.AbstractSignListener;
import net.countercraft.movecraft.sign.AscendSign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;


public class SRAscendSign extends AscendSign {

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        if (!super.canPlayerUseSignOn(player, craft)) {
            return false;
        }
        if (craft instanceof SquadronCraft sc) {
            return sc.getSquadron().getPilot() == player;
        }
        return true;
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
    protected void onAfterStoppingCruise(Craft craft, AbstractSignListener.SignWrapper signWrapper, Player player) {
        super.onAfterStoppingCruise(craft, signWrapper, player);

        if (craft instanceof SquadronCraft sc) {
            Squadron sq = sc.getSquadron();
            sq.setCruising(false);
        }
    }
}
