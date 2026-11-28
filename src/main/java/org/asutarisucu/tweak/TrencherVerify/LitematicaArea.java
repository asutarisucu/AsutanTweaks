package org.asutarisucu.tweak.TrencherVerify;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;

//#if MC < 260100
import net.minecraft.util.math.BlockPos;
//#else
//$$ import net.minecraft.core.BlockPos;
//#endif

import java.util.List;

/**
 * Reads Litematica's area selection. Kept apart from TrencherVerifier so the
 * Litematica classes load only when Litematica is installed.
 *
 * API checked in the litematica jars of every supported version (0.14.7 to 0.28.6):
 * DataManager.getSelectionManager(), SelectionManager.getCurrentSelection(),
 * AreaSelection.getSelectedSubRegionBox() / getAllSubRegionBoxes(), Box.getPos1() / getPos2().
 */
final class LitematicaArea {

    private LitematicaArea() {}

    /** @return {x1, z1, x2, z2} of the selected sub-region box, or null when there is none */
    static int[] corners() {
        AreaSelection sel = DataManager.getSelectionManager().getCurrentSelection();
        if (sel == null) return null;
        Box box = sel.getSelectedSubRegionBox();
        if (box == null) {
            List<Box> all = sel.getAllSubRegionBoxes();
            if (all.isEmpty()) return null;
            box = all.get(0);
        }
        BlockPos p1 = box.getPos1(), p2 = box.getPos2();
        if (p1 == null || p2 == null) return null;
        return new int[] { p1.getX(), p1.getZ(), p2.getX(), p2.getZ() };
    }
}
