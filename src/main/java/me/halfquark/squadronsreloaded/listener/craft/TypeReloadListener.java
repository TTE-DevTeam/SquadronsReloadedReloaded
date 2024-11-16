package me.halfquark.squadronsreloaded.listener.craft;

import me.halfquark.squadronsreloaded.sign.SRCraftPilotSign;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TypeReloadListener implements Listener {

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onCraftTypeReload(final TypesReloadedEvent event) {
        MovecraftSignRegistry.INSTANCE.registerCraftPilotSigns(CraftManager.getInstance().getCraftTypes(), SRCraftPilotSign::new);
    }

}
