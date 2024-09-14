package me.halfquark.squadronsreloaded.sign;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.halfquark.squadronsreloaded.SquadronsReloaded;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.sign.RemoteSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SRCarrierCommandSign extends RemoteSign implements ISquadronSign {

    @Override
    protected boolean internalProcessSignWithCraft(Action action, SignListener.SignWrapper signWrapper, Craft craft, Player player) {
        List<Squadron> squadrons = this.getSquadronsToProcess(signWrapper, craft, player);
        if (squadrons.isEmpty()) {
            player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - No matching squadrons found"));
            return false;
        }

        for (Squadron squadron : squadrons) {
            this.processSquadron(squadron, action, signWrapper, player);
        }

        return true;
    }

    protected void processSquadron(Squadron squadron, Action clickType, SignListener.SignWrapper signWrapper, Player player) {
        for (Craft craft : squadron.getCrafts()) {
            super.internalProcessSignWithCraft(clickType, signWrapper, craft, player);
        }
    }

    protected List<Squadron> getSquadronsToProcess(SignListener.SignWrapper signWrapper, Craft craft, Player player) {
        List<Squadron> result = new ObjectArrayList<>();
        Squadron ownSquadron = SquadronManager.getInstance().getPlayerSquadron(player, false);
        // Shift => ONLY affect your own squadron
        if (player.isSneaking()) {
            // Only your own squadron!
            if (ownSquadron != null) {
                result.add(ownSquadron);
                return result;
            }
        } else {
            final String squadronName = (String) Arrays.stream(signWrapper.rawLines()).skip(2L).filter((f) -> {
                return f != null && !f.trim().isEmpty();
            }).collect(Collectors.joining(" "));

            // If the player is not the carrier commander, we can only add their own squadron, if it maches the name
            if (craft instanceof PilotedCraft pc && pc.getPilot() != player && ownSquadron != null && ownSquadron.getName().equalsIgnoreCase(squadronName)) {
                result.add(ownSquadron);
            } else {
                List<Squadron> sqList = craft.getDataTag(SquadronsReloaded.CARRIER_SQUADRONS);
                if(sqList != null && sqList.size() > 0) {
                    if (squadronName != null && !squadronName.isBlank()) {
                        sqList.removeIf(squadron -> !squadronName.equalsIgnoreCase(squadron.getName()));
                    }
                    result.addAll(sqList);
                }
            }
        }

        return result;
    }

    @Override
    protected boolean canPlayerUseSign(Action clickType, SignListener.SignWrapper sign, Player player) {
        return player.hasPermission("movecraft.squadron.carriercommandsign");
    }

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        if (craft instanceof SquadronCraft) {
            player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Must not be on a squadron craft"));
            return false;
        }
        else {
            if (craft instanceof PilotedCraft pc) {
                return pc.getPilot() == player;
            } else {
                Squadron ownSquadron = SquadronManager.getInstance().getPlayerSquadron(player, false);
                if (ownSquadron == null) {
                    player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - No own squadron and not carrier commander"));
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
}
