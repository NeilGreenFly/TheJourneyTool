package tjTool.world.blocks.sandbox;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Lod;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.meta.*;
import tjTool.world.graphics.TjDraw;
import tjTool.world.LazyButton;
import tjTool.world.graphics.TjColor;

import static mindustry.Vars.*;
import static tjTool.world.graphics.TjDraw.rainbow;
import static tjTool.widgets.TjTable.*;
import static tjTool.core.TjVars.halfSize;
import static tjTool.world.LazyGetter.*;

/**
 * @see OverdriveProjector.OverdriveBuild
 */
public class OverdriveSource extends SandboxBlock {
    public static float defaultRange = 200;

    public static boolean alwaysShowRange = true;
    public static int sort;

    public static BuildType configBuild;
    public static float configRange;

    public TextureRegion regionTop;
    public float reload = 60;

    public Color baseColor = TjColor.overdrive;
    public Color phaseColor = TjColor.phaseColor;

    public Effect changedEffect = new Effect(40, e -> {
        Draw.color(phaseColor, baseColor, e.fin());
        Lines.stroke(4 * e.fout());
        Lines.circle(e.x, e.y, e.rotation + e.fin());
    });

    public Effect waveEffect = new Effect(90, e -> {
        Draw.color(phaseColor, baseColor, e.fin());
        Draw.alpha(Mathf.clamp(e.fout() * 5 / 2));
        Lines.stroke(4);
        Lines.circle(e.x, e.y, e.rotation * Mathf.clamp(e.fin() * 5 / 3));
    });

    public OverdriveSource(String name) {
        super(name);
        size = 3;
        noUpdateDisabled = false;
        configurable = true;
        saveConfig = true;
        emitLight = true;
        lightRadius = 50;
        ambientSound = Sounds.loopCircuit;
        ambientSoundVolume = 0.13f;
        group = BlockGroup.projectors;
        envEnabled |= Env.space;
    }

    @Override
    protected void config() {
        config(String.class, (BuildType build, String v) -> {
            var arr = v.split(";");
            if (arr.length > 0) build.overdriveConfig.setSpeed(Strings.parseFloat(arr[0]));
            if (arr.length > 1) build.overdriveConfig.setRange(Strings.parseFloat(arr[1]));
            if (alwaysShowRange) changedEffect.at(build, build.smoothRange);
        });
    }

    @Override
    public void load() {
        super.load();
        regionTop = Core.atlas.find(name + "-top");
    }

    @Override
    public void setStats() {
        super.setStats();
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("boost", (BuildType build) -> new Bar(
                () -> Core.bundle.format("bar.boost", Mathf.round(Mathf.maxZero(build.overdriveConfig.speed * 100 - 100))),
                () -> Pal.accent,
                () -> 1));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        float range = defaultRange;
        if (lastConfig instanceof String config) {
            var arr = config.split(";");
            if (arr.length > 1) range = Strings.parseFloat(arr[1]);
        }
        Drawf.dashCircle(pos(x), pos(y), range, baseColor);
        indexer.eachBlock(player.team(), pos(x), pos(y), range, this::selectBy, this::selectDraw);
    }

    public boolean selectBy(Building other) {
        return other.block.canOverdrive;
    }

    public void selectDraw(Building other) {
        Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
    }

    public String speedString(float v) {
        return "+" + (int) (v * 100 - 100) + "%";
    }

    public String rangeString(float v) {
        return Strings.autoFixed(v / tilesize, 2) + StatUnit.blocks.localized();
    }

    public class BuildType extends SandboxBlock.BuildType implements Ranged {
        public OverdriveConfig overdriveConfig = new OverdriveConfig(2.5f, defaultRange);

        public float charge = Mathf.random(reload);
        public float smoothRange;
        public float smoothEfficiency;

        @Override
        public float range() {
            return overdriveConfig.range;
        }

        @Override
        public void placed() {
            super.placed();
            waveEffect.at(this, overdriveConfig.range);
        }

        @Override
        public void updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            smoothRange = Mathf.lerpDelta(smoothRange, enabled ? overdriveConfig.range : 0, 0.08f);

            if ((charge += smoothEfficiency * Time.delta) < reload) return;
            charge = 0;
            if (overdriveConfig.range > 0 && overdriveConfig.speed > 1) indexer.eachBlock(this, overdriveConfig.range, OverdriveSource.this::selectBy,
                    other -> other.applyBoost(overdriveConfig.speed, reload + 1f));
        }

        @Override
        public void buildConfiguration(Table table) {
            table.background(Tex.paneLeft);
            Seq<OverdriveConfig> configs = new Seq<>(3);
            content.blocks().select(OverdriveProjector.class::isInstance).<OverdriveProjector>as().each(block -> {
                configs.add(new OverdriveConfig(block, false, block.speedBoost, block.range));
                if (block.hasBoost) configs.add(new OverdriveConfig(block, true, block.speedBoost + block.speedBoostPhase, block.range + block.phaseRangeBoost));
            });
            if (sort == 1) configs.sort(v -> v.speed);
            else if (sort == 2) configs.sort(v -> v.range);

            Slider speedSlider = apply(new Slider(1, 4, 0.05f, false), it -> {
                it.setValue(overdriveConfig.speed);
                it.released(() -> configure(overdriveConfig.setSpeed(it.getValue()).serialize()));
            });
            Slider rangeSlider = apply(new Slider(0, 40 * tilesize, halfSize, false), it -> {
                it.setValue(overdriveConfig.range);
                it.changed(() -> configRange = it.getValue());
                it.released(() -> {
                    configure(overdriveConfig.setRange(it.getValue()).serialize());
                    configBuild = null;
                    configRange = 0;
                });
                it.tapped(() -> {
                    configBuild = this;
                    configRange = it.getValue();
                });
            });

            table.table(t -> {
                t.defaults().size(uiSize);
                t.add(LazyButton.button(Icon.eyeSmall, Icon.eyeOffSmall, () -> alwaysShowRange, v -> alwaysShowRange = v));
                configs.each(config -> {
                    if (!config.boost) t.button(Icon.info, Styles.clearNonei, iconSize, () -> {
                        ui.content.show(config.block);
                        Events.fire(new EventType.BlockInfoEvent());
                    }).tooltip("@info.title", true);
                    else t.add();
                });
                t.row().image(Icon.effect);
                configs.each(current -> {
                    var button = new Table(b -> b.add(LazyButton.button(new TextureRegionDrawable(current.block.uiIcon), () -> false, ignore -> {
                        configure(current.serialize());
                        speedSlider.setValue(current.speed);
                        rangeSlider.setValue(current.range);
                    })).size(uiSize).tooltip(tip -> {
                        tip.background(Tex.paneLeft).image(current.block.uiIcon).size(iconSize).padLeft(5).padRight(10);
                        tip.label(() -> current.block.localizedName + (current.boost ? (" [accent]" + Iconc.star + "[]\n") : "\n") + current.block.description);
                    }));
                    if (!current.boost) t.add(button);
                    else t.stack(
                            button,
                            new Table(image -> apply(image.bottom().right().image(Icon.star).color(Color.darkGray).get(), it -> {
                                it.setSize(iconSize / 2);
                                it.touchable = Touchable.disabled;
                            })),
                            new Table(image -> apply(image.bottom().right().image(Icon.star).color(Pal.accent).padBottom(4).get(), it -> {
                                it.setSize(iconSize / 2);
                                it.touchable = Touchable.disabled;
                            }))
                    );
                });
                t.row().image(Icon.upOpen);
                configs.each(current -> t.add(LazyButton.button(
                        Icon.hammer, Icon.ok, () -> current.speed == overdriveConfig.speed,
                        ignore -> configure(overdriveConfig.setSpeed(current.speed).serialize())))
                        .tooltip(speedString(current.speed), true).get()
                        .changed(() -> speedSlider.setValue(current.speed)));
                t.add(LazyButton.button(Icon.tag, Icon.tags, () -> sort == 1, v -> {
                    sort = v ? 1 : 0;
                    rebuild(table);
                })).size(uiSize).tooltip("@block." + name + ".sort-by-speed", true);
                t.row().image(Icon.resize);
                configs.each(current -> t.add(LazyButton.button(
                        Icon.hammer, Icon.ok, () -> current.range == overdriveConfig.range,
                        ignore -> configure(overdriveConfig.setRange(current.range).serialize())))
                        .tooltip(rangeString(current.range), true).get()
                        .changed(() -> rangeSlider.setValue(current.range)));
                t.add(LazyButton.button(Icon.tag, Icon.tags, () -> sort == 2, v -> {
                    sort = v ? 2 : 0;
                    rebuild(table);
                })).size(uiSize).tooltip("@block." + name + ".sort-by-range", true);
            }).row();
            table.table(t -> {
                sliderPack(t, speedSlider, Stat.speedIncrease.localized(), OverdriveSource.this::speedString);
                sliderPack(t, rangeSlider, Stat.range.localized(), OverdriveSource.this::rangeString);
            }).growX();
        }

        private void sliderPack(Table table, Slider slider, String name, FloatString getValue) {
            Label value = new Label("", Styles.outlineLabel);

            Runnable setText = () -> value.setText(getValue.get(slider.getValue()));
            slider.changed(setText);
            slider.released(setText);
            table.stack(slider, new Table(content -> {
                content.add(name, Styles.outlineLabel).growX();
                content.add(value).padLeft(10);
                content.touchable = Touchable.disabled;
            }).margin(3, 33, 3, 33)).growX().row();
            slider.change();
        }

        @Override
        public void draw() {
            super.draw();
            if (!alwaysShowRange) {
                if (Lod.l2) {
                    Draw.color(baseColor, smoothEfficiency * Mathf.absin(Time.time, 50f / Mathf.PI2, 1) * 0.5f * Lod.alpha2);
                    Draw.rect(regionTop, x, y);
                    Draw.alpha(Lod.alpha2);
                    TjDraw.overdrive(this, baseColor, phaseColor, smoothEfficiency);
                }
            } else {
                Draw.z(Layer.shields);
                Draw.alpha(1);
                if (enabled) Draw.color(baseColor, phaseColor, Mathf.clamp(Math.abs(overdriveConfig.range - smoothRange) / 160));
                else Draw.color(BlockStatus.logicDisable.color, baseColor, Mathf.clamp(smoothRange / overdriveConfig.range));
                Fill.poly(x, y, 32, smoothRange, Time.time / 10);
            }
            Draw.reset();

            if (configBuild != this || configRange == 0) return;
            Draw.z(Layer.overlayUI);
            indexer.eachBlock(this, configRange, OverdriveSource.this::selectBy,
                    other -> Drawf.selected(other, Tmp.c1.set(rainbow).a(Mathf.absin(4f, 1f))));
            Drawf.dashCircle(x, y, configRange, rainbow);
            Draw.reset();
        }

        @Override
        public void drawSelect() {
            indexer.eachBlock(this, overdriveConfig.range, OverdriveSource.this::selectBy, OverdriveSource.this::selectDraw);
            Drawf.dashCircle(x, y, overdriveConfig.range, baseColor);
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
        }

        @Override
        public Object config() {
            return overdriveConfig.serialize();
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(overdriveConfig.speed);
            write.f(overdriveConfig.range);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 0) {
                read.f(); // OverdriveProjector.OverdriveBuild.heat
                read.f(); // OverdriveProjector.OverdriveBuild.phaseHeat
            } else if (revision == 1) {
                overdriveConfig.speed = read.f();
                overdriveConfig.range = read.f();
                smoothRange = overdriveConfig.range;
            }
        }
    }

    public static class OverdriveConfig {
        public @Nullable OverdriveProjector block;
        public boolean boost;
        public float speed;
        public float range;

        public OverdriveConfig(OverdriveProjector block, boolean boost, float speed, float range) {
            this.block = block;
            this.boost = boost;
            this.speed = speed;
            this.range = range;
        }

        public OverdriveConfig(float speed, float range) {
            this(null, false, speed, range);
        }

        public OverdriveConfig setSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        public OverdriveConfig setRange(float range) {
            this.range = range;
            return this;
        }

        public String serialize() {
            return String.join(";", autoFixed(speed), autoFixed(range));
        }
    }

    public interface FloatString {
        String get(float f);
    }
}
