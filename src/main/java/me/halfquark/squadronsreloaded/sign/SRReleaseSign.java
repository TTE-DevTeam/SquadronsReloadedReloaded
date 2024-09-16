package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftReleaseEvent.Reason;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.sign.ReleaseSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

public class SRReleaseSign extends ReleaseSign {

    @Override
    protected boolean internalProcessSign(Action clickType, SignListener.SignWrapper sign, Player player, @Nullable Craft craft) {
        Squadron sq;
        if (craft instanceof SquadronCraft sc ) {
            sq = sc.getSquadron();
        } else {
            sq = SquadronManager.getInstance().getPlayerSquadron(player, true);
        }
        if(sq == null) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Squadron Found"));
            return false;
        }
        sq.releaseAll(Reason.PLAYER);
        SquadronManager.getInstance().removeSquadron(player);
        player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Squadron has been released"));
        return true;
    }

}
