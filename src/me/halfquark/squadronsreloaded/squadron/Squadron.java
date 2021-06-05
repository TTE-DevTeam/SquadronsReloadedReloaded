package me.halfquark.squadronsreloaded.squadron;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import me.halfquark.squadronsreloaded.formation.Formation;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.events.CraftReleaseEvent.Reason;

public class Squadron {
	
	private Player pilot;
	private ConcurrentMap<SquadronCraft, Integer> crafts;
	private PlayerCraft carrier;
	private int nextId;
	private Formation formation;
	private Integer spacing;
	private CruiseDirection cruiseDirection;
	
	public Squadron(Player p) {
		pilot = p;
		crafts = new ConcurrentHashMap<>();
		nextId = 0;
		formation = null;
		spacing = null;
		cruiseDirection = null;
	}
	
	public Player getPilot() {return pilot;}
	public ConcurrentMap<SquadronCraft, Integer> getCraftMap() {return crafts;}
	public Set<SquadronCraft> getCrafts() {return crafts.keySet();}
	public Formation getFormation() {return formation;}
	public Integer getSpacing() {return spacing;}
	public boolean isFormingUp() {return formation != null;}
	@Nullable
	public CruiseDirection getDirection() {return cruiseDirection;}
	
	public int getSize() {return (crafts == null)?(0):(crafts.size());}
	public int getDisplacement() {
		if(crafts == null)
			return 0;
		int displacement = 0;
		for(Craft craft : crafts.keySet()) {
			displacement += craft.getHitBox().size();
		}
		return displacement;
	}
	@Nullable
	public PlayerCraft getCarrier() {return carrier;}
	public void setCarrier(PlayerCraft c) {carrier = c;}
	
	@Nullable
	public SquadronCraft getLeadCraft() {
		int min = -1;
		SquadronCraft leadCraft = null;
		if(crafts == null)
			return null;
		for(Entry<SquadronCraft, Integer> entry : crafts.entrySet()) {
			if(min == -1) {
				min = entry.getValue();
				leadCraft = entry.getKey();
				continue;
			}
			if(entry.getValue() < min) {
				leadCraft = entry.getKey();
				min = entry.getValue();
			}
		}
		return leadCraft;
	}
	
	@Nullable
	public Integer getLeadId() {
		int min = -1;
		if(crafts == null)
			return null;
		for(Entry<SquadronCraft, Integer> entry : crafts.entrySet()) {
			if(min == -1) {
				min = entry.getValue();
				continue;
			}
			if(entry.getValue() < min)
				min = entry.getValue();
		}
		if(min == -1)
			return null;
		return min;
	}
	
	@Nullable
	public Integer getCraftId(Craft craft) {
		if(crafts == null)
			return null;
		if(!crafts.containsKey(craft))
			return null;
		return crafts.get(craft);
	}
	
	@Nullable
	public Integer getCraftRank(Craft craft) {
		if(crafts == null)
			return null;
		if(!crafts.containsKey(craft))
			return null;
		int id = crafts.get(craft);
		int rank = 0;
		for(Entry<SquadronCraft, Integer> entry : crafts.entrySet()) {
			if(entry.getValue() < id)
				rank++;
		}
		return rank;
	}
	
	public int putCraft(SquadronCraft c) {
		if(crafts.containsKey(c))
			return -1;
		crafts.put(c, nextId);
		return nextId++;
	}
	
	public boolean removeCraft(Craft c) {
		if(crafts == null)
			return false;
		return (crafts.remove(c) != null);
	}
	
	public boolean hasCraft(Craft c) {
		return crafts.containsKey(c);
	}
	
	public void releaseAll(Reason r) {
		for(Craft c : crafts.keySet()) {
			CraftManager.getInstance().removeCraft(c, r);
		}
		crafts = new ConcurrentHashMap<>();
	}
	
	public void sinkAll() {
		for(SquadronCraft c : crafts.keySet()) {
			c.sink();
		}
		crafts = new ConcurrentHashMap<>();
	}
	
	public void formationOff() {
		formation = null;
		spacing = null;
	}
	
	public void formationOn(Formation f, int s) {
		formation = f;
		spacing = s;
	}
	
	public void setDirection(CruiseDirection cd) {cruiseDirection = cd;}
	
	@Override
	public String toString() {
		String out = "[Squadron]Pilot:" + pilot.getName();
		if(carrier != null)
			out += ",Carrier:" + carrier.getType().getCraftName() + "(" + carrier.getHitBox().size() + ")";
		for(Map.Entry<SquadronCraft, Integer> mapEntry : crafts.entrySet()) {
			out += "," + String.valueOf(mapEntry.getValue()) + ":";
			out += mapEntry.getKey().getType().getCraftName();
			out += "(" + mapEntry.getKey().getHitBox().size() + ")";
		}
		return out;
	}
	
}
