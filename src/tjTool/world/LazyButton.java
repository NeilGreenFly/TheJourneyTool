package tjTool.world;

import arc.func.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import mindustry.ui.Styles;

import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyGetter.*;

/**
 * 这还不够 Lazy, 等我回来优化...
 * @author NeilGreenFly
 */
@SuppressWarnings("unused")
public final class LazyButton {

    public static Cell<ImageButton> add(Table table, ImageButton button) {
        return table.add(button).size(uiSize);
    }

    public static ImageButton button(TextureRegionDrawable icon, Boolp isChecked, @Nullable Boolc changed) {
        return button(icon, null, isChecked, changed);
    }

    public static ImageButton button(TextureRegionDrawable iconUp, TextureRegionDrawable iconChecked, Boolp isChecked, @Nullable Boolc changed) {
        return apply(new ImageButton(iconUp, Styles.clearNoneTogglei), it -> {
            it.getStyle().imageChecked = iconChecked;
            it.resizeImage(iconSize);
            it.update(() -> it.setChecked(isChecked.get()));
            if (changed != null) it.changed(() -> changed.get(it.isChecked()));
        });
    }

}
