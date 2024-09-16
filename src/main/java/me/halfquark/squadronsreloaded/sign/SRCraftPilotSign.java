package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.SquadronsReloaded;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import me.halfquark.squadronsreloaded.squadron.SquadronManager;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.sign.CraftPilotSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.Nullable;

public class SRCraftPilotSign extends CraftPilotSign implements ISquadronSign {

    public SRCraftPilotSign(CraftType craftType) {
        super(craftType);
    }

    @Override
    protected boolean internalProcessSign(Action clickType, SignListener.SignWrapper sign, Player player, @Nullable Craft craft) {
        if (clickType.isRightClick()) {
            if (craft instanceof SquadronCraft squadronCraft) {
                if (this.craftType.getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                    // TODO: Add whitelist or blacklist for synched launches
                    this.findMatchingSignsInSquadronPerCraft(squadronCraft.getSquadron(), sign, squadronCraft).forEach(
                            (squadronCraftTmp, signs) -> {
                                signs.forEach(sw -> super.internalProcessSign(clickType, sw, player, squadronCraftTmp));
                            }
                    );
                }
            }
            return super.internalProcessSign(clickType, sign, player, craft);
        } else {
            if (!player.hasPermission("movecraft.squadron.pilot")) {
                player.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                return false;
            }

            if (SquadronsReloaded.NEEDSCARRIER && craft == null) {
                player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Needs to be carried"));
            }

            if (!SquadronsReloaded.CARRIEDTYPES.contains(this.craftType.getStringProperty(CraftType.NAME))) {
                // TODO: Message player
                return false;
            }
            PlayerCraft playerCraft = null;
            if (craft == null || !(craft instanceof PlayerCraft)) {
                for(Craft c : CraftManager.getInstance().getCraftsInWorld(player.getWorld())) {
                    if(MathUtils.locationInHitBox(c.getHitBox(), sign.block().getLocation())) {
                        if(c instanceof PlayerCraft) {
                            playerCraft = (PlayerCraft) c;
                            break;
                        }
                    }
                }
            } else {
                playerCraft = (PlayerCraft) craft;
            }

            if (playerCraft == null) {
                player.sendMessage(I18nSupport.getInternationalisedString("Squadrons - Must pilot a carrier"));
                return false;
            }

            if (!SquadronsReloaded.CARRIERTYPES.contains(playerCraft.getType().getStringProperty(CraftType.NAME))) {
                // TODO: Message player
                return false;
            }

            Squadron squadron;
            if(SquadronManager.getInstance().getPlayerSquadron(player, false) == null) {
                squadron = new Squadron(player);
                squadron.setCarrier(playerCraft);
                playerCraft.getDataTag(SquadronsReloaded.CARRIER_SQUADRONS).add(squadron);
                SquadronManager.getInstance().putSquadron(player, squadron);
            } else {
                squadron = SquadronManager.getInstance().getPlayerSquadron(player, false);
            }
            // Attempt to run detection
            Location loc = sign.block().getLocation();
            MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            CraftManager.getInstance().detect(
                    startPoint,
                    this.craftType, (t, w, p, parents) -> {
                        if (p == null || parents.size() == 0) {
                            return new Pair<>(Result.fail(), null);
                        }

                        final SquadronCraft c = new SquadronCraft(this.craftType, loc.getWorld(), p, squadron);
                        return new Pair<>(Result.succeed(), c);
                    },
                    player.getWorld(), player,
                    player,
                    oldCraft -> () -> {
                        // Release old craft if it exists
                    }
            );
            return true;
        }
    }

}
