package tjTool.world.draw;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Block;

import static arc.Core.atlas;
import static arc.math.geom.Geometry.d8edge;

@SuppressWarnings("unused")
public class DrawRotationAtlas extends DrawBase {
    public TextureRegion[] regions;

    public DrawRotationAtlas() {}

    public DrawRotationAtlas(String suffix) {
        super(suffix);
    }

    @Override
    public void load(Block block) {
        var region = atlas.find(block.name + suffix);
        int x = region.getX();
        int y = region.getY();
        int size = region.width / 2;
        regions = new TextureRegion[]{
                new TextureRegion(region.texture, x, y + size, size, size),
                new TextureRegion(region.texture, x + size, y + size, size, size),
                new TextureRegion(region.texture, x + size, y, size, size),
                new TextureRegion(region.texture, x, y, size, size)
        };
    }

    @Override
    public void draw(float x, float y, int rotation) {
        Draw.rect(regions[rotation], x + offset * d8edge[rotation].x, y + offset * d8edge[rotation].y);
    }
}
