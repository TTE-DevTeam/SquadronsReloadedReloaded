package me.halfquark.squadronsreloaded.move;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.sign.AbstractCraftSign;
import net.countercraft.movecraft.sign.CruiseSign;
import net.countercraft.movecraft.sign.MovecraftSignRegistry;
import net.countercraft.movecraft.sign.SignListener;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import javax.annotation.Nullable;

public class CraftDirectionDetection {
	
	@Nullable
	public static CruiseDirection detect(Craft craft) {
		World w = craft.getWorld();
		HitBox hitbox = craft.getHitBox();
		CruiseDirection cd = null;
		for(MovecraftLocation mLoc : hitbox.asSet()) {
			Block block = w.getBlockAt(mLoc.toBukkit(w));
			if (!(block.getState() instanceof Sign))
	            continue;
	        Sign sign = (Sign) block.getState();
			SignListener.SignWrapper[] signWrappers = SignListener.INSTANCE.getSignWrappers(sign, true);
			for (SignListener.SignWrapper signWrapper : signWrappers) {
				AbstractCraftSign acs = MovecraftSignRegistry.INSTANCE.getCraftSign(signWrapper.line(0));
				if (acs == null || !(acs instanceof CruiseSign)) {
					continue;
				}
				CruiseDirection cdTemp = CruiseDirection.fromBlockFace(signWrapper.facing());
				if (cd == null) {
					cd = cdTemp;
				} else if (!cd.equals(cdTemp)) {
					return null;
				}
			}
		}
		if(cd == null)
			return CruiseDirection.NONE;
		return cd;
	}
	
}
