package me.halfquark.squadronsreloaded;

import me.halfquark.squadronsreloaded.async.SRAsyncManager;
import me.halfquark.squadronsreloaded.command.SquadronCommand;
import me.halfquark.squadronsreloaded.formation.FormationManager;
import me.halfquark.squadronsreloaded.listener.craft.*;
import me.halfquark.squadronsreloaded.listener.player.PlayerInteractListener;
import me.halfquark.squadronsreloaded.listener.player.PlayerMovementListener;
import me.halfquark.squadronsreloaded.listener.redstone.RedstoneComponentListener;
import me.halfquark.squadronsreloaded.listener.redstone.SwitchListener;
import me.halfquark.squadronsreloaded.move.CraftProximityManager;
import me.halfquark.squadronsreloaded.move.CraftRotateManager;
import me.halfquark.squadronsreloaded.move.CraftTranslateManager;
import me.halfquark.squadronsreloaded.sign.*;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.datatag.CraftDataTagKey;
import net.countercraft.movecraft.craft.datatag.CraftDataTagRegistry;
import net.countercraft.movecraft.sign.HelmSign;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SquadronsReloaded extends JavaPlugin {

	public static final CraftDataTagKey<List<Squadron>> CARRIER_SQUADRONS = CraftDataTagRegistry.INSTANCE.registerTagKey(new NamespacedKey("squadronsreloadedreloaded", "carriersquadrons"), craft -> new ArrayList<>());
	public static final CraftDataTagKey<Squadron> SQUADRON = CraftDataTagRegistry.INSTANCE.registerTagKey(new NamespacedKey("squadronsreloadedreloaded", "squadron"), craft -> {
		if (craft instanceof SquadronCraft sc) {
			return sc.getSquadron();
		}
		if (craft instanceof PilotedCraft pc) {
			return new Squadron(pc.getPilot());
		}
		throw new IllegalStateException("Impossible state!");
	});
	
	private static SquadronsReloaded inst;
	private SquadronCommand sc;
	
	public static File FORMATIONFOLDER;
	
	public static boolean TPTONEWLEAD;
	
	public static List<String> CARRIERTYPES;
	public static List<String> CARRIEDTYPES;
	public static boolean NEEDSCARRIER;
	public static int MANOVERBOARDTIME;
	public static int TURNTICKS;
	public static double SQUADMAXSIZE;
	public static double SQUADMAXSIZECARRIERMULT;
	public static double SQUADMAXDISP;
	public static double SQUADMAXDISPCARRIERMULT;
	public static double FORMATIONROUNDDISTANCE;
	public static double FORMATIONSPEEDMULTIPLIER;
	public static double CARRIER_RETURN_MINIMAL_MAX_DISTANCE;
	public static Material SQUADRONPILOTTOOL;
	public static boolean BLOCKCOUNTOVERRIDE;

	public static List<String> SYNCEDSIGNS;
	
	@Override
	public void onEnable() {
		inst = this;
		saveDefaultConfig();
		
		FORMATIONFOLDER = new File(SquadronsReloaded.inst.getDataFolder(), File.separator + "formations");
		CARRIERTYPES = getConfig().getStringList("carrierTypes");
		CARRIEDTYPES = getConfig().getStringList("carriedTypes");
		NEEDSCARRIER = getConfig().getBoolean("needsCarrier");
		MANOVERBOARDTIME = getConfig().getInt("manoverboardTime");
		TPTONEWLEAD = getConfig().getBoolean("tpToNewLead");
		TURNTICKS = getConfig().getInt("turnTicks");
		SQUADMAXSIZE = getConfig().getDouble("squadMaxSize");
		SQUADMAXSIZECARRIERMULT = getConfig().getDouble("squadMaxSizeCarrierMultiplier");
		SQUADMAXDISP = getConfig().getDouble("squadMaxDisplacement");
		SQUADMAXDISPCARRIERMULT = getConfig().getDouble("squadMaxDisplacementCarrierMultiplier");
		FORMATIONROUNDDISTANCE = getConfig().getDouble("formationRoundDistance");
		SYNCEDSIGNS = getConfig().getStringList("syncedSigns");
		FORMATIONSPEEDMULTIPLIER = getConfig().getDouble("formationSpeedMultiplier");
		SQUADRONPILOTTOOL = Material.getMaterial(getConfig().getString("squadronPilotTool"));
		CARRIER_RETURN_MINIMAL_MAX_DISTANCE = getConfig().getDouble("carrierReturnMinimalMaxDistance", 640);
		BLOCKCOUNTOVERRIDE = getConfig().getBoolean("blockCountOverride");

		loadResources();
		
		SquadronManager.initialize();
		FormationManager.initialize();
		CraftTranslateManager.initialize();
		CraftRotateManager.initialize();
		CraftProximityManager.initialize();
		SRAsyncManager.initialize(this);
		
		getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);
		//getServer().getPluginManager().registerEvents(new SRCruiseSign(), this);
		MovecraftSignRegistry.INSTANCE.register("Cruise:", new SRCruiseSign("Cruise:"), true);
		//getServer().getPluginManager().registerEvents(new SRHelmSign(), this);
		MovecraftSignRegistry.INSTANCE.register("[Helm]", new SRHelmSign(), true, HelmSign.PRETTY_HEADER);
		//getServer().getPluginManager().registerEvents(new SRAscendSign(), this);
		MovecraftSignRegistry.INSTANCE.register("Ascend:", new SRAscendSign("Ascend:"), true);
		//getServer().getPluginManager().registerEvents(new SRDescendSign(), this);
		MovecraftSignRegistry.INSTANCE.register("Descend:", new SRDescendSign("Descend:"), true);
		//getServer().getPluginManager().registerEvents(new SRLeadSign(), this);
		MovecraftSignRegistry.INSTANCE.register("SquadronLead", new SRLeadSign(), true, "SQLead");
		//getServer().getPluginManager().registerEvents(new SRReleaseSign(), this);
		MovecraftSignRegistry.INSTANCE.register("SquadronRelease", new SRReleaseSign(), true, "SRelease", "SQRelease");
		//getServer().getPluginManager().registerEvents(new SRFormationSign(), this);
		MovecraftSignRegistry.INSTANCE.register("Formation:", new SRFormationSign(), true);
		//getServer().getPluginManager().registerEvents(new SRSyncedSign(), this);
		MovecraftSignRegistry.INSTANCE.register("Remote Sign", new SRRemoteSign(), true, "Remote");
		// TODO: Suppress the subcraft rotate sign from rotating to other squadron members when called by a remote!
		// MovecraftSignRegistry.INSTANCE.register("Subcraft Rotate", new SRSubcraftRotateSign(CraftManager.getInstance()::getCraftTypeFromString, Movecraft::getInstance)), true);
		MovecraftSignRegistry.INSTANCE.registerCraftPilotSigns(CraftManager.getInstance().getCraftTypes(), SRCraftPilotSign::new);
		MovecraftSignRegistry.INSTANCE.register("Squadron Name:", new SquadronNameSign(), "SName:");
		MovecraftSignRegistry.INSTANCE.register("Squadron Remote", new SRCarrierCommandSign(), "SRemote");

		getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
		getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
		getServer().getPluginManager().registerEvents(new SwitchListener(), this);
		getServer().getPluginManager().registerEvents(new RedstoneComponentListener(), this);
		
		getServer().getPluginManager().registerEvents(new CraftTranslateListener(), this);
		getServer().getPluginManager().registerEvents(new CraftRotateListener(), this);
		sc = new SquadronCommand();
		getCommand("squadron").setExecutor(sc);
		getCommand("squadron").setTabCompleter(sc);
	}
	
	public static SquadronsReloaded getInstance() {return inst;}
	
	private void loadResources() {
		File[] files = SquadronsReloaded.FORMATIONFOLDER.listFiles();
		if(files != null)
			return;
		saveResource("formations" + File.separator + "Echelon.formation", false);
		saveResource("formations" + File.separator + "Line.formation", false);
		saveResource("formations" + File.separator + "Column.formation", false);
		saveResource("formations" + File.separator + "Vic.formation", false);
	}
	
}
