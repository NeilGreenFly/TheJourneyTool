package tjTool.world.draw;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Block;

import static arc.Core.atlas;
import static arc.math.geom.Geometry.d4;

@SuppressWarnings("unused")
public class DrawRotation extends DrawBase {
    public TextureRegion region0;
    public TextureRegion region1;

    public DrawRotation() {}

    public DrawRotation(String suffix) {
        super(suffix);
    }

    @Override
    public void load(Block block) {
        region0 = atlas.find(block.name + suffix + "-0");
        region1 = atlas.find(block.name + suffix + "-1");
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return drawPlan ? new TextureRegion[]{region0} : new TextureRegion[0];
    }

    @Override
    public void draw(float x, float y, int rotation) {
        Draw.yscl = rotation % 2 == 0 ? 1 : -1;
        Draw.rect(rotation < 2 ? region0 : region1, x + offset * d4[rotation].x, y + offset * d4[rotation].y, rotation * 90);
        Draw.scl();
    }
}
