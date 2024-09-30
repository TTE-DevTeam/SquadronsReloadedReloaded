package me.halfquark.squadronsreloaded.listener.craft;

import java.util.*;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.ManOverboardEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.ChatUtils;
import net.countercraft.movecraft.util.MathUtils;

public class CraftReleaseListener implements Listener {

	@EventHandler
	public void onCraftRelease(CraftReleaseEvent e) {
		// We don't care about normal releases :)
		// DONE: ConcurrentListAccessModification happens here... Fix it...
		if(e.getCraft() instanceof PlayerCraft) {
			List<Squadron> sqList = new ArrayList<>(SquadronManager.getInstance().getCarrierSquadrons((PlayerCraft) e.getCraft()));
			if(sqList.size() > 0) {
				List<Squadron> toRemove = new ArrayList<>();
				for(Squadron sq : sqList) {
					sq.releaseAll(e.getReason());
					toRemove.add(sq);
				}
				toRemove.forEach(SquadronManager.getInstance()::removeSquadron);
				return;
			}
		}
		if(!(e.getCraft() instanceof SquadronCraft))
			return;

		SquadronCraft craft = (SquadronCraft) e.getCraft();
		Squadron sq = craft.getSquadron();

		if (e.getReason() == CraftReleaseEvent.Reason.PLAYER) {
			attemptToAddSquadronBackToParent(craft, sq);
			return;
		}

		if(sq == null)
			return;
		if(sq.getCarrier() != null) {
			// Impossible condition => Carrier is a playercraft, and SquadronCraft never extends PlayerCraft
			/*if(sq.getCarrier().equals(e.getCraft())) {
				SquadronManager.getInstance().getPlayerSquadron(p, false).releaseAll(e.getReason());
				SquadronManager.getInstance().removeSquadron(p);
				return;
			}*/
		}
		Player p = craft.getSquadronPilot();
		if(p == null)
			return;
		if(sq.hasCraft(e.getCraft())) {
			boolean tpToLead = false;
			if(sq.getLeadCraft().equals(e.getCraft())) {
				if(MathUtils.locationNearHitBox(e.getCraft().getHitBox(), p.getLocation(),2))
					tpToLead = true;
			}
			sq.removeCraft(e.getCraft());
			if(sq.getSize() == 0) {
				SquadronManager.getInstance().removeSquadron(p);
				p.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Squadrons - Squadron has been released"));
			}
			if(tpToLead && SquadronsReloaded.TPTONEWLEAD) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if(!SquadronManager.getInstance().hasSquadron(p))
							return;
						ManOverboardEvent event = new ManOverboardEvent(sq.getLeadCraft(), getCraftTeleportPoint(sq.getLeadCraft()));
				        Bukkit.getServer().getPluginManager().callEvent(event);

				        p.setVelocity(new Vector(0, 0, 0));
				        p.setFallDistance(0);
				        p.teleport(event.getLocation());
					}		
				}.runTaskLater(SquadronsReloaded.getInstance(), 1);
			}
		}
	}

	private static void attemptToAddSquadronBackToParent(SquadronCraft squadronCraft, Squadron squadron) {
		if (squadron.getCarrier() == null) {
			return;
		}

		PlayerCraft carrier = squadron.getCarrier();
		HitBox carrierHitBox = carrier.getHitBox();

		if (!carrierHitBox.inBounds(squadronCraft.getHitBox().getMinX(), squadronCraft.getHitBox().getMinY(), squadronCraft.getHitBox().getMinZ())) {
			if (!carrierHitBox.inBounds(squadronCraft.getHitBox().getMaxX(), squadronCraft.getHitBox().getMaxY(), squadronCraft.getHitBox().getMaxZ())) {
				// Skiff can not be within the carrier
				return;
			}
		}

		// We are within the carrier
		// Now, are we touching it?
		// If the carrier may merge to the skiff, then we add it back
		if (checkCraftBorders(carrier, squadronCraft)) {
			carrier.setHitBox(carrier.getHitBox().union(squadronCraft.getHitBox()));
		}
	}

	private static boolean checkCraftBorders(Craft carrierCraft, SquadronCraft squadronCraft) {
		final EnumSet<Material> ALLOWED_BLOCKS = carrierCraft.getType().getMaterialSetProperty(CraftType.ALLOWED_BLOCKS);
		final EnumSet<Material> FORBIDDEN_BLOCKS = carrierCraft.getType().getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS);
		final MovecraftLocation[] SHIFTS = {
				//x
				new MovecraftLocation(-1, 0, 0),
				new MovecraftLocation(-1, -1, 0),
				new MovecraftLocation(-1,1,0),
				new MovecraftLocation(1, -1, 0),
				new MovecraftLocation(1, 1, 0),
				new MovecraftLocation(1, 0, 0),
				//z
				new MovecraftLocation(0, 1, 1),
				new MovecraftLocation(0, 0, 1),
				new MovecraftLocation(0, -1, 1),
				new MovecraftLocation(0, 1, -1),
				new MovecraftLocation(0, 0, -1),
				new MovecraftLocation(0, -1, -1),
				//y
				new MovecraftLocation(0, 1, 0),
				new MovecraftLocation(0, -1, 0)};
		//Check each location in the hitbox
		for (MovecraftLocation ml : carrierCraft.getHitBox()){
			//Check the surroundings of each location
			for (MovecraftLocation shift : SHIFTS){
				MovecraftLocation test = ml.add(shift);
				//Ignore locations contained in the craft's hitbox
				if (carrierCraft.getHitBox().contains(test)){
					continue;
				}

				if (!squadronCraft.getHitBox().inBounds(test)) {
					continue;
				}

				if (!squadronCraft.getHitBox().contains(test)) {
					continue;
				}

				Block testBlock = test.toBukkit(carrierCraft.getWorld()).getBlock();
				Material testMaterial = testBlock.getType();
				//Break the loop if an allowed block is found adjacent to the craft's hitbox
				if (ALLOWED_BLOCKS.contains(testMaterial)){
					return true;
				}
				//Do the same if a forbidden block is found
				else if (FORBIDDEN_BLOCKS.contains(testMaterial)){
					return true;
				}
			}
		}
		return false;
	}


	private Location getCraftTeleportPoint(Craft craft) {
        double telX = (craft.getHitBox().getMinX() + craft.getHitBox().getMaxX())/2D + 0.5D;
        double telZ = (craft.getHitBox().getMinZ() + craft.getHitBox().getMaxZ())/2D + 0.5D;
        double telY = craft.getHitBox().getMaxY() + 1;
        return new Location(craft.getWorld(), telX, telY, telZ);
    }
	
}
