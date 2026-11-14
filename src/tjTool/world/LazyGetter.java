package tjTool.world;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Strings;
import mindustry.ctype.UnlockableContent;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;

/**
 * @author NeilGreenFly
 */
@SuppressWarnings("unused")
public final class LazyGetter {

    public static final boolean[] boolMap = new boolean[]{false, true};

    public static <T extends UnlockableContent> int w(T t) {
        return t != null ? t.id : -1;
    }

    public static Color c(Item item) {
        return item != null ? item.color : Color.clear;
    }

    public static Color c(Liquid liquid) {
        return liquid != null ? liquid.color : Color.clear;
    }

    public static Color validDarkGray(boolean valid) {
        return valid ? Pal.accent : Color.darkGray;
    }

    public static Color validColor(boolean valid) {
        return valid ? Pal.accent : Pal.remove;
    }

    public static String cFmt(Color color) {
        return "[#" + color + "]";
    }

    public static String autoFixed(float v) {
        return Strings.autoFixed(v, 3);
    }

    public static boolean parseBool(String n) {
        return !n.equals("0");
    }

    public static <T> T apply(T any, Cons<T> it) {
        it.get(any);
        return any;
    }

    public static void clearScrollFocus(Element element) {
        if (element.hasScroll()) Core.scene.setScrollFocus(null);
    }

}
