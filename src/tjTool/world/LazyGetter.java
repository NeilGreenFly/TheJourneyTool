package tjTool.world;

import arc.graphics.Color;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.Liquid;

@SuppressWarnings("unused")
public class LazyGetter {

    public static <T extends UnlockableContent> short w(T t) {
        return t != null ? t.id : -1;
    }

    public static Color c(Item item) {
        return item != null ? item.color : Color.clear;
    }

    public static Color c(Liquid liquid) {
        return liquid != null ? liquid.color : Color.clear;
    }

}
