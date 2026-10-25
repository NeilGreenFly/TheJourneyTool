package tjTool.world;

import arc.func.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.ui.Styles;

import static tjTool.core.TjTable.*;
import static tjTool.world.LazyGetter.*;

@SuppressWarnings("unused")
public class LazyButton {

    public static Cell<ImageButton> add(Building building, Table table, TextureRegionDrawable icon, Boolp isChecked, Boolc setChecked, Prov<?> configure) {
        return table.add(apply(new ImageButton(icon, Styles.clearNoneTogglei), it -> {
            it.resizeImage(iconSize);
            it.update(() -> it.setChecked(isChecked.get()));
            it.changed(() -> {
                setChecked.get(it.isChecked());
                building.configure(configure.get());
            });
        })).size(uiSize);
    }

}
