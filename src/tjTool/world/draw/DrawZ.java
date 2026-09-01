package tjTool.world.draw;

import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.world.draw.DrawBlock;

@SuppressWarnings("unused")
public class DrawZ extends DrawBlock {
    public float layer;

    public DrawZ(float layer) {
        this.layer = layer;
    }

    @Override
    public void draw(Building build) {
        Draw.z(layer);
    }
}
