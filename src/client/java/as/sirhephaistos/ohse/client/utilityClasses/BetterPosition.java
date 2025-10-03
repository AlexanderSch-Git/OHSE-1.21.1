package as.sirhephaistos.ohse.client.utilityClasses;

import com.sun.jna.platform.win32.WinNT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;


public class BetterPosition {
    private BlockPos pos;
    private final Direction face;

    public BetterPosition(int x, int y, int z , Direction face) {
        this.pos = new BlockPos(x, y, z);
        this.face = face;
    }
    public BetterPosition(int x, int y, int z , int face) {
        this.pos = new BlockPos(x, y, z);
        this.face = Direction.byId(face);
    }

    public BetterPosition(BlockPos pos, Direction face) {
        this.pos = pos.toImmutable(); // copie immuable
        this.face = face;
    }

    public BetterPosition(BlockPos pos, int faceId) {
        this.pos = pos.toImmutable(); // copie immuable
        this.face = Direction.byId(faceId);
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getX() { return pos.getX(); }
    public int getY() { return pos.getY(); }
    public int getZ() { return pos.getZ(); }

    public void setY(int newY) {
        System.out.println("old Y: " + pos.getY());
        this.pos = new BlockPos(pos.getX(), newY, pos.getZ());
        System.out.println("new Y: " + pos.getY());
    }

    public Direction getFace() { return face; }
}
