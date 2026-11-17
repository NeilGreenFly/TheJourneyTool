package tjTool.world.blocks.sandbox;

import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.entities.Puddles;
import mindustry.input.Placement;
import mindustry.world.Tile;
import tjTool.world.graphics.TjDraw;

import static mindustry.Vars.*;
import static tjTool.world.Range.*;

public class Sponge extends SandboxBlock {
    public int r = 4;

    public Sponge(String name) {
        super(name);
        solid = false;
        swapDiagonalPlacement = true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        TjDraw.lightPoly(pos(x), pos(y), 4, (r + 0.5f) * Mathf.sqrt2 * tilesize, TjDraw.rainbow, 45);
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        Placement.calculateNodes(points, this, rotation, (point, other) -> overlaps(world.tile(point.x, point.y), world.tile(other.x, other.y)));
    }

    public boolean overlaps(@Nullable Tile src, @Nullable Tile other) {
        if (src == null || other == null) return true;
        return Intersector.overlaps(Tmp.cr1.set(src.worldx() + offset, src.worldy() + offset, (r + 0.5f) * 2 * tilesize), Tmp.r1.setSize(size * tilesize).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    public class BuildType extends SandboxBlock.BuildType {
        @Override
        public void updateTile() {
            tile.setAir();
            placeEffect.at(this, r * 2 + 1);
            forRange(-r, r + 1, y -> forRange(-r, r + 1, x -> {
                var puddle = Puddles.get(tile.nearby(x, y));
                if (puddle != null) puddle.remove();
            }));
        }
    }
}
