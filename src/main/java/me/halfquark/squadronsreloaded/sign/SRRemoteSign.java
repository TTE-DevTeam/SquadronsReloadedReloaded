package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.RemoteSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

public class SRRemoteSign extends RemoteSign implements ISquadronSign {

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        return this.sq_canPlayerUseSignOn(player, craft, super::canPlayerUseSignOn);
    }

    @Override
    protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, Craft craft, Player player) {
        if (craft instanceof SquadronCraft sc && !player.isSneaking()) {
            Squadron squadron = sc.getSquadron();
            for (SquadronCraft c : squadron.getCrafts()) {
                if (c != craft) {
                    super.internalProcessSignWithCraft(clickType, sign, c, player);
                }
            }
        }
        return super.internalProcessSignWithCraft(clickType, sign, craft, player);
    }
}
