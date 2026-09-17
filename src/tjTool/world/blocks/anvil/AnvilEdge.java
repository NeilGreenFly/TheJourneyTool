package tjTool.world.blocks.anvil;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import tjTool.world.blocks.anvil.Anvil.AnvilBuild;
import tjTool.world.draw.DrawRotation;

import static arc.math.geom.Geometry.d4;

public class AnvilEdge extends AnvilAddon {
    public float outEnergy = 1;

    public AnvilEdge(String name) {
        super(name);
        size = 3;
        update = true;
        hasPower = true;
        consumePower(0.5f);
    }

    @Override
    protected DrawBlock defaultDrawer() {
        return new DrawMulti(new DrawRotation(), new DrawRotation("-interface") {
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
        var t = tileNearby(tile, d4, rotation);
        return t != null && t.build != null && (rotation % 2 != 0 ? tile.x - t.build.tile.x : tile.y - t.build.tile.y) % size == 0 ? super.checkCore(t, team, 0) : null;
    }

    @SuppressWarnings("unused")
    public class AnvilEdgeBuild extends AnvilAddonBuild {
        @Override
        public boolean shouldConsume() {
            return enabled && anvil != null && anvil.acceptEnergy() > 0;
        }

        @Override
        public void anvilUpdateTile() {
            if (Mathf.chanceDelta(0.005)) anvilEffect.at(x, y, 0, color, anvil);
            if (efficiency > 0) anvil.handleEnergy(Math.min(outEnergy * getProgressIncrease(1), anvil.acceptEnergy()));
        }

        @Override
        public void drawStatus() {}
    }
}
