package me.halfquark.squadronsreloaded.squadron;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import net.countercraft.movecraft.MovecraftLocation;
import org.bukkit.entity.Player;

import me.halfquark.squadronsreloaded.formation.Formation;
import net.countercraft.movecraft.craft.type.CraftType;
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
	private CruiseDirection orientation;
	private boolean cruising;
	private CruiseDirection cruiseDirection;
	private boolean pilotLocked;
	// TODO: Migrate to use Components
	private String name = "";

	public Squadron(Player p) {
		pilot = p;
		crafts = new ConcurrentHashMap<>();
		nextId = 0;
		formation = null;
		spacing = null;
		orientation = null;
		cruising = false;
		pilotLocked = false;
	}

	public UUID getSquadronUUID() {
		if (this.getLeadCraft() == null) {
			return null;
		}
		return this.getLeadCraft().getUUID();
	}

	public String getName() {
		return this.name;
	}
	public Player getPilot() {return pilot;}
	public ConcurrentMap<SquadronCraft, Integer> getCraftMap() {return crafts;}
	public Set<SquadronCraft> getCrafts() {return crafts.keySet();}
	public Formation getFormation() {return formation;}
	public Integer getSpacing() {return spacing;}
	public boolean isFormingUp() {return formation != null;}
	@Nullable
	public CruiseDirection getDirection() {return orientation;}
	public boolean getCruising() {return cruising;}
	public CruiseDirection getCruiseDirection() {return cruiseDirection;}

	public String setName(String value) {
		if (this.name == null || this.name.isBlank()) {
			this.name = value;
		}
		return this.name;
	}

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
			CraftManager.getInstance().release(c, r, true);
		}
		crafts = new ConcurrentHashMap<>();
	}
	
	public void sinkAll() {
		for(SquadronCraft c : crafts.keySet()) {
			CraftManager.getInstance().sink(c);
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

	@Nullable
	public MovecraftLocation getSquadronCenter() {
		MovecraftLocation result = new MovecraftLocation(0, 0, 0);
		int divisor = 0;
		for (Craft craft : this.getCrafts()) {
			if (craft == null) {
				continue;
			}
			divisor++;
			result = result.add(craft.getHitBox().getMidPoint());
		}
		if (divisor == 0) {
			return null;
		}
		return result.scalarDivide(divisor);
	}
	
	public void setDirection(CruiseDirection cd) {orientation = cd;}
	public void setCruising(boolean b) {cruising = b;}
	public void setCruiseDirection(CruiseDirection cd) {cruiseDirection = cd;}
	public void setPilotLocked(boolean b) {pilotLocked = b;}
	public boolean getPilotLocked() {return pilotLocked;}
	
	@Override
	public String toString() {
		String out = "[Squadron]Pilot:" + pilot.getName();
		if(carrier != null)
			out += ",Carrier:" + carrier.getType().getStringProperty(CraftType.NAME) + "(" + carrier.getHitBox().size() + ")";
		for(Map.Entry<SquadronCraft, Integer> mapEntry : crafts.entrySet()) {
			out += "," + String.valueOf(mapEntry.getValue()) + ":";
			out += mapEntry.getKey().getType().getStringProperty(CraftType.NAME);
			out += "(" + mapEntry.getKey().getHitBox().size() + ")";
		}
		return out;
	}

	public Collection<Craft> getContacts() {
		Set<Craft> result = new HashSet<>();
		for (Craft craft : this.getCrafts()) {
			craft.getDataTag(Craft.CONTACTS).forEach(
					uuid -> {
						Craft craftTmp = Craft.getCraftByUUID(uuid);
						result.add(craftTmp);
					}
			);
		}
		return result;
	}
}
