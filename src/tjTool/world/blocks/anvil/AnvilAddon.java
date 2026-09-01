package tjTool.world.blocks.anvil;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.util.Nullable;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.graphics.BlockRenderer;
import mindustry.world.Tile;
import tjTool.world.blocks.TjBlock;
import tjTool.world.blocks.anvil.Anvil.AnvilBuild;

import static arc.math.Angles.randLenVectors;
import static tjTool.core.TjDraw.beacon;

public abstract class AnvilAddon extends TjBlock {
    public static Effect anvilEffect = new Effect(240, e -> randLenVectors(e.id, 2, e.finpow() * 20, (x, y) -> {
        var build = (AnvilBuild) e.data;
        var f = e.finpowdown();
        var a = e.foutpowdown();
        var cx = e.x + x + (build.x - e.x - x) * f;
        var cy = e.y + y + (build.y - e.y - y) * f;
        var r = 2 * e.fin();
        Draw.color(e.color);
        Draw.alpha(a * 2);
        Fill.poly(cx, cy, 4, r);
        beacon(cx, cy, r, e.color, 0.3f * f);
    }));

    public Color color = Color.valueOf("#FFBFFF");

    public AnvilAddon(String name) {
        super(name);
        customShadow = true;
        rotate = true;
        quickRotate = false;
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public AnvilBuild checkCore(Tile tile, Team team, int rotation) {
        return tile.build instanceof AnvilBuild build && build.team == team ? build : null;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return checkCore(tile, team, rotation) != null;
    }

    @Override
    public void drawShadow(Tile tile) {
        if (tile.build == null) return;
        Draw.color(0f, 0f, 0f, BlockRenderer.shadowColor.a);
        Draw.rect(customShadowRegion, tile.build.x, tile.build.y, tile.build.rotation * 90);
        Draw.color();
    }

    public abstract class AnvilAddonBuild extends TjBuilding {
        public @Nullable AnvilBuild anvil;

        @Override
        public void onProximityUpdate() {
            anvil = checkCore(tile, team, rotation);
        }
    }
}
