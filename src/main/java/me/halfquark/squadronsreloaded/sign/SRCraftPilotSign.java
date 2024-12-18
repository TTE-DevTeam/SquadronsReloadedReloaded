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
import org.bukkit.World;
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
            if (!player.isSneaking() && (craft instanceof SquadronCraft squadronCraft)) {
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
                player.sendMessage(I18nSupport.getInternationalisedComponent("Insufficient Permissions"));
                return false;
            }

            if (SquadronsReloaded.NEEDSCARRIER && craft == null) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("Squadrons - Needs to be carried"));
            }

            if (craft instanceof SquadronCraft) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("This squadron is already in use!"));
                return false;
            }

            if (!SquadronsReloaded.CARRIEDTYPES.contains(this.craftType.getStringProperty(CraftType.NAME))) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("This crafttype is not supported as squadron!"));
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
                        if (c instanceof SquadronCraft) {
                            player.sendMessage(I18nSupport.getInternationalisedComponent("This squadron is already in use!"));
                            return false;
                        }
                    }
                }
            } else {
                playerCraft = (PlayerCraft) craft;
            }

            if (playerCraft == null && SquadronsReloaded.NEEDSCARRIER) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("Squadrons - Must pilot a carrier"));
                return false;
            } else if (playerCraft != null) {
                if (!SquadronsReloaded.CARRIERTYPES.contains(playerCraft.getType().getStringProperty(CraftType.NAME))) {
                    player.sendMessage(I18nSupport.getInternationalisedComponent("Squadrons - Piloted craft is not a carrier type"));
                    return false;
                }
            }

            Squadron squadron = SquadronManager.getInstance().getPlayerSquadron(player, false);
            if(squadron == null) {
                squadron = new Squadron(player);
                if (playerCraft != null) {
                    squadron.setCarrier(playerCraft);
                    playerCraft.getDataTag(SquadronsReloaded.CARRIER_SQUADRONS).add(squadron);
                }
                SquadronManager.getInstance().putSquadron(player, squadron);
            }

            // This should never happen!
            if (squadron == null) {
                player.sendMessage(I18nSupport.getInternationalisedComponent("Squadrons - No squadron found for player. This should never happen!"));
                return false;
            }

            final Squadron fSquadron = squadron;
            // Attempt to run detection
            Location loc = sign.block().getLocation();
            MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            runDetectTaskWithPlayerCraft(squadron, startPoint, player, loc.getWorld());

            return true;
        }
    }

    protected void runDetectTaskWithPlayerCraft(final Squadron squadron, final MovecraftLocation startPoint, final Player player, final World world) {
        CraftManager.getInstance().detect(
                startPoint,
                this.craftType, (t, w, p, parents) -> {
                    if (p == null || !(parents.size() > 0 || !SquadronsReloaded.NEEDSCARRIER)) {
                        return new Pair<>(Result.failWithMessage("Player is null or parents are not present!"), null);
                    }

                    final SquadronCraft c = new SquadronCraft(this.craftType, world, p, squadron);
                    return new Pair<>(Result.succeed(), c);
                },
                world, player,
                player,
                oldCraft -> () -> {
                    // Release old craft if it exists
                }
        );
    }

}
