package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.formation.Formation;
import me.halfquark.squadronsreloaded.formation.FormationManager;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.sign.AbstractMovecraftSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;

// TODO: Permissions
public class SRFormationSign extends AbstractMovecraftSign {

    @Override
    public boolean processSignClick(Action clickType, SignListener.SignWrapper sign, Player player) {
        this.formation = null;
        return super.processSignClick(clickType, sign, player);
    }

    private @Nullable Formation formation;
    private int spacing;

    @Override
    protected boolean isSignValid(Action action, SignListener.SignWrapper signWrapper, Player player) {
        this.formation = FormationManager.getInstance().getFormation(signWrapper.getRaw(1));
        if(this.formation == null) {
            player.sendMessage(ChatUtils.errorPrefix().append(I18nSupport.getInternationalisedComponent("Squadrons - Specify a valid formation")));
            return false;
        }
        try {
            this.spacing = Integer.valueOf(signWrapper.getRaw(2));
        } catch(NumberFormatException e) {
            player.sendMessage(ChatUtils.errorPrefix().append(I18nSupport.getInternationalisedComponent("Squadrons - Specify a valid integer")));
            return false;
        }
        if(spacing < formation.getMinSpacing() || formation.getMaxSpacing() < spacing) {
            player.sendMessage(ChatUtils.ERROR_PREFIX + I18nSupport.getInternationalisedComponent("Squadrons - Spacing has to be in range")
                    + " [" + String.valueOf(formation.getMinSpacing()) + "," + String.valueOf(formation.getMaxSpacing()) + "]");
            return false;
        }
        return true;
    }

    @Override
    protected boolean internalProcessSign(Action action, SignListener.SignWrapper signWrapper, Player player, @Nullable Craft craft) {
        Squadron squadron;
        if (craft != null && craft instanceof SquadronCraft sc) {
            squadron = sc.getSquadron();
        } else {
            squadron = SquadronManager.getInstance().getPlayerSquadron(player, true);
        }
        if(squadron == null) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Squadron Found"));
            return false;
        }
        // Just to be sure
        if (this.formation == null) {
            return false;
        }

        squadron.formationOn(formation, spacing);
        player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Formation")
                + " " + formation.getName() + " " + spacing);

        return true;
    }

    @Override
    public boolean processSignChange(SignChangeEvent signChangeEvent, SignListener.SignWrapper signWrapper) {
        return this.isSignValid(Action.PHYSICAL, signWrapper, signChangeEvent.getPlayer());
    }
}
