package tjTool.core;

import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.ui.Styles;

/**
 * "button": {"icon", "handle"}
 */
public class TjConfigTable {

    public static <Type extends UnlockableContent> void rowTable(Building building, Table table, Image icon, String tip, Seq<Type> items, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        rowTable(building, table, icon, tip, items.map(item -> item.fullIcon), items.map(item -> item.localizedName), holder, closeSelect, groupIndex);
    }

    public static void rowTable(Building building, Table table, Image icon, String tip, Seq<TextureRegion> regions, Seq<String> tips, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        table.add(icon).size(40f).tooltip(tip).center();
        if (regions.any()) {
            table.table(configTable(building, regions, tips, holder, closeSelect, groupIndex)).left();
            var button = new ImageButton(Icon.undo, Styles.clearNonei);
            button.clicked(() -> building.configure(new int[]{groupIndex, -1}));
            table.add(button).size(40f).tooltip("Reset").center().row();
        } else table.add(new Image(Icon.cancel)).size(40f).center().row();
    }

    public static <Type extends UnlockableContent> Cons<Table> configTable(Building building, Seq<Type> items, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        return configTable(building, items.map(item -> item.fullIcon), items.map(item -> item.localizedName), holder, closeSelect, groupIndex);
    }

    public static Cons<Table> configTable(Building building, Seq<TextureRegion> regions, Seq<String> tips, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        return table -> {
            table.clear();
            table.background(Styles.black6).left().defaults().size(40f);
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);

            for (int i = 0; i < regions.size; i += 1) {
                configButton(table, group, holder, building::configure, closeSelect, regions.get(i), tips.get(i), groupIndex, i);
            }
        };
    }

    private static void configButton(
            Table table, ButtonGroup<ImageButton> group, Prov<Integer> holder,
            Cons<int[]> consumer, boolean closeSelect,
            TextureRegion region, String tip,
            int groupIndex, final int buttonIndex
    ) {
        ImageButton button = table.button(Tex.whiteui, Styles.clearNoneTogglei, 24f, () -> {
            if (closeSelect) Vars.control.input.config.hideConfig();
        }).tooltip(tip).group(group).get();

        if (groupIndex != -1) button.changed(() -> consumer.get(new int[]{groupIndex, button.isChecked() ? buttonIndex : -1}));
        button.getStyle().imageUp = new TextureRegionDrawable(region);
        button.update(() -> button.setChecked(holder.get() == buttonIndex));
    }

}
