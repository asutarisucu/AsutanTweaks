package org.asutarisucu.tweak.WorldEditGUI;

/**
 * The current WorldEdit selection as reported over the CUI channel.
 *
 * Deliberately holds nothing but ints so the whole parsing side stays free of
 * Minecraft types (and therefore of preprocessor branches).
 *
 * Only cuboid selections are tracked. WorldEdit's other shapes (polygon2d,
 * ellipsoid, cylinder, polyhedron, convex) announce themselves with an
 * {@code s|<shape>} event too; those clear the selection rather than drawing a
 * wrong box.
 */
public final class WorldEditSelection {

    private static volatile boolean cuboid = true;
    private static volatile boolean hasP1, hasP2;
    private static int p1x, p1y, p1z, p2x, p2y, p2z;
    private static volatile boolean awaitingPoints;

    private WorldEditSelection() {}

    /**
     * WorldEdit re-announces the shape whenever it describes the selection, so the
     * points are only dropped when the shape actually changed — clearing on every
     * announcement would leave a half-filled selection.
     *
     * {@code //sel} with no argument clears the region selector and then describes
     * it, which sends this announcement and no points at all. That is the only
     * signal the client gets, so the announcement is remembered and
     * {@link #endPendingAnnouncement()} drops the points a tick later if none
     * arrived.
     */
    public static synchronized void setShape(String shape) {
        awaitingPoints = true;
        boolean nowCuboid = shape != null && shape.startsWith("cuboid");
        if (nowCuboid == cuboid) return;
        cuboid = nowCuboid;
        hasP1 = false;
        hasP2 = false;
    }

    public static synchronized void setPoint(int id, int x, int y, int z) {
        awaitingPoints = false;
        if (id == 0) { p1x = x; p1y = y; p1z = z; hasP1 = true; }
        else if (id == 1) { p2x = x; p2y = y; p2z = z; hasP2 = true; }
    }

    /**
     * Ends a shape announcement that brought no points with it: the selection was
     * cleared. Called once a tick, by which time the points of a real
     * re-announcement — sent as their own packets right behind the shape — are in.
     */
    public static synchronized void endPendingAnnouncement() {
        if (!awaitingPoints) return;
        awaitingPoints = false;
        hasP1 = false;
        hasP2 = false;
    }

    public static synchronized void clear() {
        hasP1 = false;
        hasP2 = false;
        cuboid = true;
        awaitingPoints = false;
    }

    public static boolean isComplete() {
        return cuboid && hasP1 && hasP2;
    }

    /**
     * One selection point on its own, so each shows up as soon as it is set rather
     * than only once the box is complete.
     *
     * @param id 0 for pos1, 1 for pos2
     * @return {x, y, z}, or null when that point is not set
     */
    public static synchronized int[] getPoint(int id) {
        if (id == 0) return hasP1 ? new int[] { p1x, p1y, p1z } : null;
        if (id == 1) return hasP2 ? new int[] { p2x, p2y, p2z } : null;
        return null;
    }

    /** @return {minX, minY, minZ, maxX, maxY, maxZ} (inclusive block coords), or null when incomplete. */
    public static synchronized int[] getBounds() {
        if (!isComplete()) return null;
        return new int[] {
                Math.min(p1x, p2x), Math.min(p1y, p2y), Math.min(p1z, p2z),
                Math.max(p1x, p2x), Math.max(p1y, p2y), Math.max(p1z, p2z)
        };
    }
}
