package me.halfquark.squadronsreloaded.sign;

import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.sign.SubcraftRotateSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class SRSubcraftRotateSign extends SubcraftRotateSign implements ISquadronSign {

    public SRSubcraftRotateSign(Function<String, @Nullable CraftType> craftTypeRetrievalFunction, Supplier<Plugin> plugin) {
        super(craftTypeRetrievalFunction, plugin);
    }

    @Override
    protected boolean canPlayerUseSignOn(Player player, @Nullable Craft craft) {
        return this.sq_canPlayerUseSignOn(player, craft, super::canPlayerUseSignOn);
    }

    @Override
    protected boolean internalProcessSignWithCraft(Action clickType, SignListener.SignWrapper sign, @Nullable Craft craft, Player player) {
        if (craft instanceof SquadronCraft sc && !player.isSneaking()) {
            Squadron squadron = sc.getSquadron();
            this.findMatchingSignsInSquadronPerCraft(squadron, sign, null).forEach((c, s) -> {
                s.forEach((wrapperToUse) -> {
                    super.internalProcessSignWithCraft(clickType, wrapperToUse, c, player);
                });
            });
            return true;
        }
        return super.internalProcessSignWithCraft(clickType, sign, craft, player);
    }

}
