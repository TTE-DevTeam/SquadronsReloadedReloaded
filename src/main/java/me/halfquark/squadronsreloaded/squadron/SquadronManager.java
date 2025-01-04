package me.halfquark.squadronsreloaded.squadron;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.craft.PlayerCraft;

public class SquadronManager {

	private static SquadronManager inst;
	private static ConcurrentMap<UUID, Squadron> squads;
	
	public static void initialize() {
		inst = new SquadronManager();
	}
	
	private SquadronManager() {
		squads = new ConcurrentHashMap<>();
	}
	
	public static SquadronManager getInstance() {return inst;}
	
	public void putSquadron(Player p, Squadron s) {squads.put(p.getUniqueId(), s);}
	public void removeSquadron(Player p) {
		Squadron sq = squads.remove(p.getUniqueId());
		if (sq != null && sq.getCarrier() != null) {
			sq.getCarrier().getDataTag(SquadronsReloaded.CARRIER_SQUADRONS).remove(sq);
		}
	}
	public void removeSquadron(Squadron sq) {
		if (sq != null && sq.getCarrier() != null) {
			sq.getCarrier().getDataTag(SquadronsReloaded.CARRIER_SQUADRONS).remove(sq);
		}
		squads.entrySet().removeIf(entry -> entry.getValue().equals(sq));
	}
	public boolean hasSquadron(Player p) {
		if(!squads.containsKey(p.getUniqueId()))
			return false;
		Squadron sq = squads.get(p.getUniqueId());
		if(sq == null) {
			return false;
		}
		if((sq.getCarrier() == null) && SquadronsReloaded.NEEDSCARRIER) {
			sq.sinkAll();
			return false;
		}
		if(sq.getCrafts() == null) {
			return false;
		}
		if(sq.getCrafts().size() == 0) {
			return false;
		}
		return true;
	}
	@Nullable
	public Squadron getPlayerSquadron(Player p, boolean check) {
		if(!check)
			return squads.get(p.getUniqueId());
		if(hasSquadron(p))
			return squads.get(p.getUniqueId());
		return null;
	}
	
	public List<Squadron> getCarrierSquadrons(PlayerCraft c) {
		return c.getDataTag(SquadronsReloaded.CARRIER_SQUADRONS);
	}

	public List<Squadron> getSquadronList() {
		ArrayList<Squadron> squadList = new ArrayList<>();
		for(Map.Entry<UUID, Squadron> entry : squads.entrySet()) {
			squadList.add(entry.getValue());
		}
		return squadList;
	}
	
}
