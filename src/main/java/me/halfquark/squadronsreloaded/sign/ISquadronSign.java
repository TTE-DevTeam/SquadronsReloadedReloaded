package me.halfquark.squadronsreloaded.sign;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.halfquark.squadronsreloaded.squadron.Squadron;
import me.halfquark.squadronsreloaded.squadron.SquadronCraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.SignListener;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ISquadronSign {

    public default List<SignListener.SignWrapper> findMatchingSignsInSquadron(final @NotNull Squadron squadron, final SignListener.SignWrapper originalSign, @Nullable SquadronCraft squadronToIgnore) {
        List<SignListener.SignWrapper> result = new ObjectArrayList<>();

        final Function<SignListener.SignWrapper, Boolean> testFunction = originalSign::areSignsEqual;
        // TODO: Use trackedlocations for finding signs
        squadron.getCrafts().forEach(craft -> {
            if (craft != squadronToIgnore) {
                Collection<SignListener.SignWrapper> entries = findAllSignsOnCraft(craft, testFunction);
                if (!entries.isEmpty()) {
                    result.addAll(entries);
                }
            }
        }) ;

        return result;
    }

    public default Map<SquadronCraft, Collection<SignListener.SignWrapper>> findMatchingSignsInSquadronPerCraft(final @NotNull Squadron squadron, final SignListener.SignWrapper originalSign, @Nullable SquadronCraft squadronToIgnore) {
        Map<SquadronCraft, Collection<SignListener.SignWrapper>> result = new Object2ObjectArrayMap<>();

        final Function<SignListener.SignWrapper, Boolean> testFunction = originalSign::areSignsEqual;
        // TODO: Use trackedlocations for finding signs
        squadron.getCrafts().forEach(craft -> {
            if (craft != squadronToIgnore) {
                Collection<SignListener.SignWrapper> entries = findAllSignsOnCraft(craft, testFunction);
                if (!entries.isEmpty()) {
                    result.put(craft, entries);
                }
            }
        }) ;

        return result;
    }

    default Collection<SignListener.SignWrapper> findAllSignsOnCraft(Craft craft, Function<SignListener.SignWrapper, Boolean> testFunction) {
        Collection<SignListener.SignWrapper> result = new ObjectArrayList<>();

        Iterator<MovecraftLocation> iter = craft.getHitBox().iterator();
        while(iter.hasNext()) {
            MovecraftLocation mLoc = iter.next();
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            if (block == null) {
                continue;
            }
            BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                continue;
            }
            Sign sign = (Sign) state;
            SignListener.SignWrapper[] wrappersAtLoc = SignListener.INSTANCE.getSignWrappers(sign, true);
            if (wrappersAtLoc == null) {
                continue;
            }
            for (SignListener.SignWrapper wrapperTmp : wrappersAtLoc) {
                if (testFunction.apply(wrapperTmp)) {
                    result.add(wrapperTmp);
                }
            }
        }

        return result;
    }

    public default boolean sq_canPlayerUseSignOn(Player player, @Nullable Craft craft, BiFunction<Player, @Nullable Craft, Boolean> superFunction) {
        if (!superFunction.apply(player, craft)) {
            return false;
        }
        if (craft instanceof SquadronCraft sc) {
            return sc.getSquadron().getPilot() == player;
        }
        return true;
    }

}
