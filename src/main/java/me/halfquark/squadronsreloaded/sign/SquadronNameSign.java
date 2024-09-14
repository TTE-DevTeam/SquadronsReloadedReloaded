package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.sign.AbstractCraftSign;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SquadronNameSign extends AbstractCraftSign {

    public static final String NAME_SIGN_PERMISSION = "movecraft.squadron.name.place";

    public SquadronNameSign() {
        super(NAME_SIGN_PERMISSION, true);
    }

    protected boolean canPlayerUseSign(Action clickType, SignListener.SignWrapper sign, Player player) {
        return !Settings.RequireNamePerm || super.canPlayerUseSign(clickType, sign, player);
    }

    protected boolean isSignValid(Action clickType, SignListener.SignWrapper sign, Player player) {
        return true;
    }

    protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, Craft craft, Player player) {
        return true;
    }

    protected boolean internalProcessSign(Action clickType, SignListener.SignWrapper sign, Player player, @Nullable Craft craft) {
        return true;
    }

    protected void onCraftIsBusy(Player player, Craft craft) {
    }

    protected void onCraftNotFound(Player player, SignListener.SignWrapper sign) {
    }

    public boolean processSignChange(SignChangeEvent event, SignListener.SignWrapper sign) {
        if (this.canPlayerUseSign(Action.RIGHT_CLICK_BLOCK, (SignListener.SignWrapper)null, event.getPlayer())) {
            return true;
        } else {
            event.getPlayer().sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + "Insufficient permissions");
            event.setCancelled(true);
            return false;
        }
    }

    public void onCraftDetect(CraftDetectEvent event, SignListener.SignWrapper sign) {
        Craft craft = event.getCraft();
        if (craft != null && craft instanceof SquadronCraft sc && sc.getSquadronPilot() != null) {
            if (!sc.getSquadronPilot().hasPermission(NAME_SIGN_PERMISSION)) {
                return;
            }
            Squadron squadron = sc.getSquadron();
            if (squadron != null) {
                final String squadronName = (String)Arrays.stream(sign.rawLines()).skip(1L).filter((f) -> {
                    return f != null && !f.trim().isEmpty();
                }).collect(Collectors.joining(" "));
                if (!squadron.setName(squadronName).equalsIgnoreCase(squadronName)) {
                    // TODO: Send message that it uses the other name!
                }
            }
        }
    }

}
