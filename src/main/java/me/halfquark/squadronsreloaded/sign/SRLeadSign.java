package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.sign.AbstractMovecraftSign;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.ManOverboardEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.ChatUtils;
import org.jetbrains.annotations.Nullable;

public class SRLeadSign extends AbstractMovecraftSign implements ISquadronSign {

	private Location getCraftTeleportPoint(Craft craft) {
        double telX = (craft.getHitBox().getMinX() + craft.getHitBox().getMaxX())/2D + 0.5D;
        double telZ = (craft.getHitBox().getMinZ() + craft.getHitBox().getMaxZ())/2D + 0.5D;
        double telY = craft.getHitBox().getMaxY() + 1;
        return new Location(craft.getWorld(), telX, telY, telZ);
    }

    @Override
    protected boolean isSignValid(Action action, SignListener.SignWrapper signWrapper, Player player) {
        return true;
    }

    @Override
    protected boolean internalProcessSign(Action action, SignListener.SignWrapper signWrapper, Player player, @Nullable Craft craft) {
        Squadron sq;
        if (craft instanceof SquadronCraft sc) {
             sq = sc.getSquadron();
        } else {
            sq = SquadronManager.getInstance().getPlayerSquadron(player, true);
        }
        if(sq == null) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Squadron Found"));
            return false;
        }
        Craft leadCraft = sq.getLeadCraft();
        Location telPoint = getCraftTeleportPoint(leadCraft);

        ManOverboardEvent moEvent = new ManOverboardEvent(leadCraft, telPoint);
        //For some reason this commits sepuko
        Bukkit.getServer().getPluginManager().callEvent(moEvent);

        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.teleport(moEvent.getLocation());
        return true;
    }

    @Override
    public boolean processSignChange(SignChangeEvent signChangeEvent, SignListener.SignWrapper signWrapper) {
        return this.isSignValid(Action.PHYSICAL, signWrapper, signChangeEvent.getPlayer());
    }
}
