package tjTool.world.blocks.anvil;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import tjTool.world.blocks.anvil.Anvil.AnvilBuild;
import tjTool.world.draw.DrawRotation;

import static arc.math.geom.Geometry.d4;

public class AnvilEdge extends AnvilAddon {
    public AnvilEdge(String name) {
        super(name);
        size = 3;
        update = true;
        drawer = new DrawMulti(new DrawRotation(), new DrawRotation("-interface") {
            @Override
            public void draw(Building build) {
                if (((AnvilEdgeBuild) build).anvil != null) {
                    Draw.z(Layer.blockOver - .1f);
                    super.draw(build);
                }
            }
        }.drawPlan(false).offset(18.5f), new DrawRegion("-top"));
    }

    @Override
    public AnvilBuild checkCore(Tile tile, Team team, int rotation) {
        var t = tile.nearby(
                (size / 2 + 1) * d4[rotation].x,
                (size / 2 + 1) * d4[rotation].y);
        if (t.build != null && (rotation % 2 != 0 ? tile.x - t.build.tile.x : tile.y - t.build.tile.y) % size != 0) return null;
        return super.checkCore(t, team, 0);
    }

    @SuppressWarnings("unused")
    public class AnvilEdgeBuild extends AnvilAddonBuild {
        @Override
        public void updateTile() {
            if (anvil != null && Mathf.chanceDelta(0.005)) anvilEffect.at(x, y, 0, color, anvil);
        }
    }
}
