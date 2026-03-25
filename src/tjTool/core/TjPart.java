package tjTool.core;

import arc.graphics.Color;
import mindustry.entities.part.*;
import mindustry.graphics.Layer;

public class TjPart {
    public static Color sharded = Color.valueOf("#FFBF7F");
    public static Color shardedDark = Color.valueOf("#FFD37F");

    public static class TjShapePart extends ShapePart {
        public TjShapePart() {
            super();
            color = sharded;
            colorTo = shardedDark;
            layer = Layer.effect;
        }
    }

    public static class TjHaloPart extends HaloPart {
        public TjHaloPart() {
            super();
            color = sharded;
            colorTo = shardedDark;
            layer = Layer.effect;
        }
    }

    public static class TjStaticShapePart extends ShapePart {
        @Override
        public void draw(PartParams params) {
            params.rotation = 90f;
            super.draw(params);
        }
    }

    public static class TjStaticHaloPart extends HaloPart {
        @Override
        public void draw(PartParams params) {
            params.rotation = 90f;
            super.draw(params);
        }
    }

}
