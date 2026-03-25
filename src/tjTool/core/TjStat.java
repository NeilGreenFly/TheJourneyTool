package tjTool.core;

import arc.graphics.g2d.TextureRegion;
import arc.scene.event.*;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class TjStat {
    public static final Stat config = new Stat("config", StatCat.function);

    public static void acknowledgements(Table table, TextureRegion region) {
        newConfigStats(table, region, TjBundle.getThis("acknowledgements"), TjBundle.getThis("saying"), new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BaseDialog dialog = new BaseDialog(TjBundle.getThis("saying"));
                dialog.cont.table(TjConfigTable.updateLog);
                dialog.addCloseButton();
                dialog.show();
            }
        });
    }

    public static void newConfigStats(Table table, TextureRegion region, String name, String description) {
        newConfigStats(table, region, name, description, null);
    }

    public static void newConfigStats(Table table, TextureRegion region, String name, String description, EventListener listener) {
        table.table(Styles.grayPanel, frame -> {
            Image image = new Image(region);
            if (listener != null) image.addListener(listener);
            frame.add(image).tooltip(name, true).size(40f).pad(12f).left().top();
            frame.table(label -> {
                label.label(() -> TjDraw.toString(TjDraw.rainbow()) + name).growX().left().row();
                label.label(() -> description).growX().left().row();
            }).growX().pad(12f).padLeft(0f).row();
        }).growX().pad(5f).row();
    }

    public static String boosters(ReloadTurret turret, boolean baseReload, Liquid liquid) {
        float reload = turret.reload;
        float maxUsed = turret.coolant.amount;
        float multiplier = turret.coolantMultiplier;
        float reloadRate = (baseReload ? 1f : 0f) + maxUsed * multiplier * liquid.heatCapacity;
        float standardReload = baseReload ? reload : reload / (maxUsed * multiplier * 0.4f);
        float result = standardReload / (reload / reloadRate);
        return "[stat]" + Strings.autoFixed(result * 100, 2) + "%";
    }

}
