package me.halfquark.squadronsreloaded.formation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import me.halfquark.squadronsreloaded.config.Config;
import me.halfquark.squadronsreloaded.utils.MathParser;
import me.halfquark.squadronsreloaded.utils.MathUtils;
import net.countercraft.movecraft.CruiseDirection;
import org.bukkit.block.BlockFace;

public class Formation extends Config {
	
	private String name;
	private Map<CruiseDirection, PositionExpression> positionMap;
	private int minSpacing;
	private int maxSpacing;
	
	public Formation(File file) {super(file);}
	public Formation(String path) {super(path);}
	public Formation(String n, Map<CruiseDirection, PositionExpression> m, int min, int max) {
		super(SquadronsReloaded.FORMATIONFOLDER + File.separator + n + ".formation");
		name = n;
		positionMap = m;
		minSpacing = min;
		maxSpacing = max;
		save();
	}

	@Override
	public void loadFields() {
		name = getString("name");
		minSpacing = getInt("minSpacing");
		maxSpacing = getInt("maxSpacing");
		name = getString("name");
		positionMap = new HashMap<>();
		if(getConfigurationSection("positionMap") == null)
			return;
		for(String key : getConfigurationSection("positionMap").getKeys(false)) {
			PositionExpression pe = new PositionExpression(
				getString("positionMap." + key + ".x"),
				getString("positionMap." + key + ".y"),
				getString("positionMap." + key + ".z")
				);
			positionMap.put(stringToCruiseDir(key), pe);
		}
	}

	@Override
	public void saveFields() {
		set("name", name);
		set("minSpacing", minSpacing);
		set("maxSpacing", maxSpacing);
		for(Entry<CruiseDirection, PositionExpression> entry : positionMap.entrySet()) {
			set("positionMap." + cruiseDirToString(entry.getKey()) + ".x", entry.getValue().getXExp());
			set("positionMap." + cruiseDirToString(entry.getKey()) + ".y", entry.getValue().getYExp());
			set("positionMap." + cruiseDirToString(entry.getKey()) + ".z", entry.getValue().getZExp());
		}
	}
	
	public String getName() {return name;}
	public int getMinSpacing() {return minSpacing;}
	public int getMaxSpacing() {return maxSpacing;}
	@Nullable
	public Double getXPosition(int n, int spacing, CruiseDirection cd) {
		if(positionMap.get(cd) == null)
			return null;
		return positionMap.get(cd).getXPosition(n, spacing);
	}
	@Nullable
	public Double getYPosition(int n, int spacing, CruiseDirection cd) {
		if(positionMap.get(cd) == null)
			return null;
		return positionMap.get(cd).getYPosition(n, spacing);
	}
	@Nullable
	public Double getZPosition(int n, int spacing, CruiseDirection cd) {
		if(positionMap.get(cd) == null)
			return null;
		return positionMap.get(cd).getZPosition(n, spacing);
	}

	static final BlockFace[] FACES = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
	@Nullable
	private String cruiseDirToString(CruiseDirection cd) {
		BlockFace nearest = MathUtils.getClosestFace(cd, FACES);
		return nearest.name();
	}
	
	@Nullable
	private CruiseDirection stringToCruiseDir(String s) {
		switch(s) {
		case "NORTH":
			return CruiseDirection.NORTH;
		case "EAST":
			return CruiseDirection.EAST;
		case "SOUTH":
			return CruiseDirection.SOUTH;
		case "WEST":
			return CruiseDirection.WEST;
		case "UP":
			return CruiseDirection.UP;
		case "DOWN":
			return CruiseDirection.DOWN;
		case "NONE":
			return CruiseDirection.NONE;
		}
		return null;
	}
	
}
