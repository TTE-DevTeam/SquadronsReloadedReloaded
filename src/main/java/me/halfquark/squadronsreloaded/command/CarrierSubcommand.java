package me.halfquark.squadronsreloaded.command;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import net.countercraft.movecraft.MovecraftLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.ManOverboardEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.ChatUtils;

public class CarrierSubcommand {

	public void run(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
            sender.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Must Be Player"));
            return;
        }
		Player player = (Player) sender;
		if(!player.hasPermission("movecraft.squadron.carrier")) {
			player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return;
		}
		Squadron sq = SquadronManager.getInstance().getPlayerSquadron(player, true);
		if(sq == null){
			player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Squadron Found"));
            return;
		}
		Craft carrier = sq.getCarrier();
        if (carrier == null) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Carrier Found"));
            return;
        }

		Location telPoint = getCraftTeleportPoint(carrier);
        if (carrier.getWorld() != player.getWorld()) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("ManOverboard - Other World"));
            return;
        }

        MovecraftLocation squadronCenter = sq.getSquadronCenter();
        if (squadronCenter == null) {
            player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - No Center"));
            return;
        }
        // TODO: Maybe change to distance squared
        // TODO: This is deprecated and shall be removed!
        if (sq.getContacts().contains(carrier)) {
            // All good
        } else {
            double distance = carrier.getHitBox().getMidPoint().distance(squadronCenter);
            double compareDistance = SquadronsReloaded.getInstance().CARRIER_RETURN_MINIMAL_MAX_DISTANCE;
            if (distance > compareDistance) {
                player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Carrier too far away"));
                return;
            }
        }


        ManOverboardEvent event = new ManOverboardEvent(carrier, telPoint);
        Bukkit.getServer().getPluginManager().callEvent(event);

        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.teleport(event.getLocation());
        return;
	}
	
	private Location getCraftTeleportPoint(Craft craft) {
        double telX = (craft.getHitBox().getMinX() + craft.getHitBox().getMaxX())/2D + 0.5D;
        double telZ = (craft.getHitBox().getMinZ() + craft.getHitBox().getMaxZ())/2D + 0.5D;
        double telY = craft.getHitBox().getMaxY() + 1;
        return new Location(craft.getWorld(), telX, telY, telZ);
    }
	
}
