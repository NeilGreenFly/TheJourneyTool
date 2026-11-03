package tjTool.world;

import arc.func.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.ui.Styles;

import static tjTool.core.TjTable.*;
import static tjTool.world.LazyGetter.*;

/**
 * 这还不够 Lazy, 等我回来优化...
 * @author NeilGreenFly
 */
@SuppressWarnings("unused")
public class LazyButton {

    public static Cell<ImageButton> add(Building building, Table table, TextureRegionDrawable icon, Boolp isChecked, @Nullable Boolc setChecked, @Nullable Prov<?> configure) {
        return table.add(apply(new ImageButton(icon, Styles.clearNoneTogglei), it -> {
            it.resizeImage(iconSize);
            it.update(() -> it.setChecked(isChecked.get()));
            it.changed(() -> {
                if (setChecked != null) setChecked.get(it.isChecked());
                if (configure != null) building.configure(configure.get());
            });
        })).size(uiSize);
    }

    public static ImageButton button(TextureRegionDrawable icon, Boolp isChecked, @Nullable Boolc changed) {
        return apply(new ImageButton(icon, Styles.clearNoneTogglei), it -> {
            it.resizeImage(iconSize);
            it.update(() -> it.setChecked(isChecked.get()));
            if (changed != null) it.changed(() -> changed.get(it.isChecked()));
        });
    }

}
