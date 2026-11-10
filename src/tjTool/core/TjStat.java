package tjTool.core;

import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.event.*;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import mindustry.world.meta.*;
import tjTool.dialogs.UpdateDialog;
import tjTool.world.consumers.MultiConsumer;

import static arc.util.Strings.autoFixed;
import static mindustry.gen.Tex.*;
import static mindustry.world.meta.StatCat.*;
import static mindustry.world.meta.StatValues.withTooltip;
import static tjTool.world.Range.*;
import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyGetter.apply;

public final class TjStat {

    public static final Stat config = new Stat("config", function);
    public static final Stat multiConsumersConfig = new Stat("config", crafting);

    public static BaseDialog updateDialog = new UpdateDialog();

    public static StatValue acknowledgements(TextureRegion region) {
        return table -> {
            table.row();
            newConfigStats(table, region, TjBundle.getThis("acknowledgements"), TjBundle.getThis("saying"), new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    updateDialog.show();
                }
            });
        };
    }

    public static StatValue multiConsumersConfig(MultiConsumer multiConsumers) {
        return table -> table.row().table(paneLeft, multi -> multi.stack(
                new Table(bg -> forRange(multiConsumers.consumers.length, i -> bg.image().update(v -> v.color.a(Mathf.absin(i * 2 - Time.globalTime / 20, 1, 0.5f))).color(Color.darkGray).grow().row())),
                new Table(content -> {
                    content.marginLeft(20).marginRight(20);
                    for (var consumer : multiConsumers.consumers) {
                        content.table(input -> {
                            input.left();
                            for (var v : consumer.input.items) withTooltip(stack(input, v, consumer.craftTime).get(), v.item, true);
                            for (var v : consumer.input.liquids) withTooltip(stack(input, v).get(), v.liquid, true);
                            if (consumer.consPower()) stack(input, new Image(Icon.power), autoFixed(consumer.usage * 60f, 3) + "[gray]/s[]", Pal.accent);
                            if (consumer.consHeat()) stack(input, new Image(Icon.waves), String.valueOf((int) consumer.heatRequirement), Pal.remove);
                        }).growX();
                        content.image(Icon.rightOpen).padLeft(10).padRight(10);
                        content.add(autoFixed(consumer.craftTime / 60f, 3) + "[gray]s[]");
                        content.image(Icon.rightOpen).padLeft(10).padRight(10);
                        content.table(output -> {
                            output.right();
                            for (var v : consumer.output.items) withTooltip(stack(output, v, consumer.craftTime).get(), v.item, true);
                            for (var v : consumer.output.liquids) withTooltip(stack(output, v).get(), v.liquid, true);
                        }).growX().padTop(10).padBottom(10).row();
                    }
                })
        )).padTop(5).get().margin(0, 10, 0, 0);
    }

    public static void newConfigStats(Table table, TextureRegion region, String name, String description) {
        newConfigStats(table, region, name, description, null);
    }

    public static void newConfigStats(Table table, TextureRegion region, String name, String description, EventListener listener) {
        table.table(Styles.grayPanel, frame -> {
            Image image = new Image(region);
            if (listener != null) image.addListener(listener);
            float multiple = 40f / Math.max(region.width, region.height);
            frame.table(in -> in.add(image).tooltip(name, true).size(region.width * multiple, region.height * multiple)).size(40f).pad(12f).left().top();
            frame.table(label -> {
                label.add(apply(new FLabel("{rainbow=10;;0.25}" + name), FLabel::skipToTheEnd)).growX().left().row();
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
