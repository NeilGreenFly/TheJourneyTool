package tjTool.world;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.Liquid;

import static arc.Core.camera;

@SuppressWarnings("unused")
public class LazyGetter {

    public static final Vec2 v = new Vec2();

    public static <T extends UnlockableContent> short w(T t) {
        return t != null ? t.id : -1;
    }

    public static Color c(Item item) {
        return item != null ? item.color : Color.clear;
    }

    public static Color c(Liquid liquid) {
        return liquid != null ? liquid.color : Color.clear;
    }

    public static Vec2 getOffsetByHeight(float x, float y, float z) {
        float cx = camera.position.x;
        float cy = camera.position.y;
        return v.set(cx - (cx - x) * z, cy - (cy - y) * z);
    }

}
