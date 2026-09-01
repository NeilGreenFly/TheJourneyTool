package tjTool.world.blocks.anvil;

import arc.graphics.g2d.Draw;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.draw.DrawMulti;
import tjTool.world.blocks.anvil.Anvil.AnvilBuild;
import tjTool.world.draw.DrawRotationAtlas;

import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;
import static tjTool.world.blocks.anvil.Anvil.coreSize;

public class AnvilAmplifier extends AnvilAddon {
    public AnvilAmplifier(String name) {
        super(name);
        size = 5;
        drawArrow = false;
        drawer = new DrawMulti(new DrawRotationAtlas("-atlas"), new DrawRotationAtlas("-interface") {
            @Override
            public void draw(Building build) {
                if (((AnvilAmplifierBuild) build).anvil != null) {
                    Draw.z(Layer.blockOver - .2f);
                    super.draw(build);
                }
            }
        }.drawPlan(false).offset(23.5f));
    }

    @Override
    public AnvilBuild checkCore(Tile tile, Team team, int rotation) {
        var t = tile.nearby(
                (size / 2 + 1) * d8edge[rotation].x,
                (size / 2 + 1) * d8edge[rotation].y);
        int d = (size + coreSize) / 2;
        if (t.build != null && !(Math.abs(tile.x - t.build.tile.x) == d && Math.abs(tile.y - t.build.tile.y) == d)) return null;
        return super.checkCore(t, team, 0);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Drawf.dashRect(validColor(valid),
                (x + d8edge[rotation].x * (size + coreSize) / 2f - coreSize / 2f) * tilesize,
                (y + d8edge[rotation].y * (size + coreSize) / 2f - coreSize / 2f) * tilesize,
                9 * tilesize, 9 * tilesize);
        control.input.drawArrow(this, x, y, rotation, valid);
        control.input.drawArrow(this, x, y, rotation + 1, valid);
    }

    @SuppressWarnings("unused")
    public class AnvilAmplifierBuild extends AnvilAddonBuild {}
}
