package tjTool.core;

import arc.func.*;
import arc.graphics.Color;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;

import static mindustry.Vars.content;
import static tjTool.world.LazyGetter.*;

/**
 * Useless
 * @author NeilGreenFly
 * @see Vars#content
 */
@SuppressWarnings("unused")
public final class ColorizedContent {
    private static <T extends UnlockableContent> Cons<T> builder(Func<T, Color> getter) {
        return any -> any.localizedName = cFmt(getter.get(any)) + any.localizedName;
    }

    public static void load() {
        content.blocks().each(v -> v.hasColor, builder(v -> v.mapColor));
        content.items().each(builder(v -> v.color));
        content.liquids().each(builder(v -> v.color));
        content.statusEffects().each(builder(v -> v.color));
        content.planets().each(builder(v -> v.atmosphereColor));
        content.sectors().each(builder(v -> v.planet.atmosphereColor));
        // content.weathers().each(builder(v -> v.color)); // 天气的 color 并不在基类中
    }
}
