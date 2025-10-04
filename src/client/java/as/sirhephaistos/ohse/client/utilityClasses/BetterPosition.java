package as.sirhephaistos.ohse.client.utilityClasses;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Direction;

@Getter @Setter
public class BetterPosition {
    private double x;
    private double y;
    private double z;
    private final Direction face;

    public BetterPosition(double x, double y, double z, Direction face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }
    public BetterPosition(double x, double y, double z, int faceId) {
        this(x, y, z, Direction.byId(faceId));
    }
}