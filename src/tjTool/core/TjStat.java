package tjTool.core;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class TjStat {
    public static final Stat config = new Stat("config", StatCat.function);

    public static void newConfigStats(Table table, TextureRegion region, String name, String description) {
        table.table(Styles.grayPanel, frame -> {
            frame.image(region).size(40f).pad(12f).left().top();
            frame.table(label -> {
                label.label(() -> TjDraw.toString(TjDraw.rainbow()) + name).growX().left().row();
                label.label(() -> description).growX().left().row();
            }).growX().pad(12f).padLeft(0f).row();
        }).growX().pad(5f).row();
    }
}
