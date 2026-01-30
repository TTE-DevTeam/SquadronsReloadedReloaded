package me.halfquark.squadronsreloaded.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class MathUtils extends net.countercraft.movecraft.util.MathUtils {

    public static BlockFace getClosestFace(final Vector vectorIn, final BlockFace[] possibleFaces) {
        Vector vector = vectorIn.clone();
        if (vector.lengthSquared() == 0) return BlockFace.SELF;

        vector = vector.clone().normalize();

        BlockFace closest = BlockFace.NORTH;
        double maxDot = -Double.MAX_VALUE;

        for (BlockFace face : possibleFaces) {
            if (face == BlockFace.SELF) continue;

            Vector faceVector = new Vector(face.getModX(), face.getModY(), face.getModZ()).normalize();
            double dot = vector.dot(faceVector);
            if (dot > maxDot) {
                maxDot = dot;
                closest = face;
            }
        }

        return closest;
    }
}
