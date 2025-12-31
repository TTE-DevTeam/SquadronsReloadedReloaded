package me.halfquark.squadronsreloaded.listener.craft;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import me.halfquark.squadronsreloaded.move.CraftDirectionDetection;
import me.halfquark.squadronsreloaded.move.CraftRotateManager;
import me.halfquark.squadronsreloaded.sign.SRCraftPilotSign;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent.Reason;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CraftDetectListener implements Listener {

    // TODO: Implement customisable squad max size per craft type and max distance from carrier
	@EventHandler
	public void onDetect(CraftDetectEvent event) {
		if(!(event.getCraft() instanceof SquadronCraft))
			return;
		SquadronCraft c = (SquadronCraft) event.getCraft();
		Player player = c.getSquadronPilot();
		Squadron squadron = c.getSquadron();
		if(c.getHitBox().size() == 0) {
    		CraftManager.getInstance().release(c, Reason.EMPTY, true);
    		return;
    	}
		final int carrierBlockCount = (squadron.getCarrier() != null ? squadron.getCarrier().getOrigBlockCount() : 1);
        if(squadron.getSize() + 1 > SquadronsReloaded.SQUADMAXSIZE + SquadronsReloaded.SQUADMAXSIZECARRIERMULT * carrierBlockCount) {
        	player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Too many crafts"));
        	CraftManager.getInstance().release(c, Reason.FORCE, true);
        	return;
        }
        if(squadron.getDisplacement() + c.getOrigBlockCount() > SquadronsReloaded.SQUADMAXDISP + SquadronsReloaded.SQUADMAXDISPCARRIERMULT * carrierBlockCount) {
        	player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Too much displacement"));
        	CraftManager.getInstance().release(c, Reason.FORCE, true);
        	return;
        }
        CruiseDirection cd = CraftDirectionDetection.detect(c);
        if(c.equals(squadron.getLeadCraft())) {
        	squadron.setDirection(cd);
        }
        if(cd == null) {
        	player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Contradicting Cruise signs"));
        	CraftManager.getInstance().release(c, Reason.FORCE, true);
        	return;
        }
        if(!cd.equals(CruiseDirection.NONE))
        	CraftRotateManager.getInstance().setDirection(c, cd);
        int position = squadron.putCraft(c);
        if (squadron.getCarrier() != null) {
			squadron.getCarrier().setHitBox(squadron.getCarrier().getHitBox().difference(c.getHitBox()));
			if(SquadronsReloaded.BLOCKCOUNTOVERRIDE)
				squadron.getCarrier().setOrigBlockCount(carrierBlockCount-c.getOrigBlockCount());
		}
    	player.sendMessage(I18nSupport.getInternationalisedString(c.getType().getStringProperty(CraftType.NAME) + "(" + c.getHitBox().size() + ") added to squadron in position") + " " + position);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraftTypesReload(TypesReloadedEvent event) {
        MovecraftSignRegistry.INSTANCE.registerCraftPilotSigns(CraftManager.getInstance().getTypesafeCraftTypes(), SRCraftPilotSign::new);
    }
	
}
