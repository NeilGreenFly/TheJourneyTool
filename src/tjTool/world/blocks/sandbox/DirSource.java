package tjTool.world.blocks.sandbox;

import arc.Core;
import arc.Events;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.InputHandler;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import tjTool.core.*;
import tjTool.world.graphics.*;

import static mindustry.Vars.*;
import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyButton.*;
import static tjTool.world.LazyGetter.*;
import static tjTool.world.Range.*;

public class DirSource extends BaseSource {
    public static boolean shown = true;

    public DirSource(String name) {
        super(name);
        rotate = true;
        commandable = true;
    }

    @Override
    protected DrawBlock defaultDrawer() {
        return new DrawMulti(new DrawDefault(), new DrawHeatOutput());
    }

    @Override
    protected void config() {
        config(4, (BuildType build, String[] arr) -> {
            if (build.getTarget(it -> it.id != Strings.parseInt(arr[0]))) return;
            int id = Strings.parseInt(arr[1]);
            build.ammo = build.getTarget(ItemTurret.class::isInstance) ? content.item(id) : content.liquid(id);
            build.coolant = content.liquid(Strings.parseInt(arr[2]));
            build.overdrive = Strings.parseInt(arr[3]);
        });
        config(Block.class, (BuildType build, Block v) -> {
            lastConfig = null;
            if (v.isAir()) {
                build.commandPos = null;
                if (build.targetBuild instanceof Turret.TurretBuild turret)
                    turret.logicControlTime = 0;
                return;
            }
            if (!(build.targetBuild instanceof BaseTurret.BaseTurretBuild turret)) return;
            InputHandler.unitClear(turret instanceof ControlBlock controlBlock ? controlBlock.unit().getPlayer() : null);
            float rotation = turret.rotation;
            turret.tile.setBlock(v, build.team);
            turret = (BaseTurret.BaseTurretBuild) build.targetBuild;
            turret.rotation = rotation;
            turret.block.placeEffect.at(turret, turret.block.size);
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, table -> {
            table.row();
            addStats(table, Icon.turret.getRegion(), "ammo");
            addStats(table, Icon.star.getRegion(), "boost");
            addStats(table, Icon.download.getRegion(), "consumes");
            addStats(table, Icon.effect.getRegion(), "overdrive");
        });
    }

    private void addStats(Table table, TextureRegion region, String str) {
        TjStat.newConfigStats(table, region, bundle(str = "config-" + str), bundle(str + "-description"));
    }

    public class BuildType extends BaseSource.BuildType {
        public @Nullable Building targetBuild = null;
        public @Nullable UnlockableContent ammo = null;
        public @Nullable Liquid coolant = null;
        public @Nullable Vec2 commandPos;
        public int overdrive = -1;
        public Seq<Item> items = new Seq<>();
        public Seq<Liquid> liquids = new Seq<>();
        public Seq<UnlockableContent> ammoTypes = new Seq<>();
        public Seq<Liquid> coolants = new Seq<>();
        public Seq<UnlockableContent> consumes = new Seq<>();
        public float[] overdrives = new float[]{1.5f, 2.25f, 2.5f};
        public float heat;

        public Layout layout = new Layout(this).with(
                new Page(Icon.wrench).with(Selection.unlockableContent(() -> content.blocks().select(block -> targetBuild instanceof BaseTurret.BaseTurretBuild && block instanceof BaseTurret && block.size == getTarget().size).as(), this::getTarget)), // use ::as will throw NullPointerException.
                new Page(Icon.link).with(new EmptyContent<>(table -> {
                    BaseDialog dialog = new BaseDialog("@openlink");
                    dialog.cont.label(() -> "即将打开链接").center().row();
                    dialog.cont.button("QQ Group: 1078329722", Icon.link, Styles.grayt, () -> Core.app.openURI("https://qm.qq.com/q/ZfZKpzUzaU"))
                            .tooltip("@openlink", true).margin(8f).size(300f, 50f).pad(10f).row();
                    dialog.cont.label(() -> "由于拦截不良账号的需要，在入群申请中请输入答案: [sky]DeepSpace[]").padTop(50f).center().row();
                    dialog.cont.label(() -> "不仅仅是BUG, 如果您有新的[accent]需求[]或[accent]想法[], 也可以向我们提交它们!").padTop(50f).center().row();
                    dialog.addCloseButton();
                    table.label(() -> "遇到了[accent]BUG[]? 向我们提交它们!").padTop(20f).center().row();
                    table.button("Submit", Icon.link, Styles.grayt, dialog::show)
                            .tooltip("@openlink", true).margin(8f).size(300f, 50f).pad(10f);
                })));

        public Block getTarget() {
            return targetBuild != null ? targetBuild.block : null;
        }

        public boolean getTarget(Boolf<Block> by) {
            return targetBuild != null && by.get(targetBuild.block);
        }

        public void drawItemSelections(Building building, Seq<UnlockableContent> selections) {
            float dx = building.x - (building.block.size * 8) / 2f;
            float dy = building.y + (building.block.size * 8) / 2f;
            for (var selection : selections)
                if (selection != null) {
                    float s = 6f * selection.fullIcon.ratio();
                    float h = 6f;
                    Draw.mixcol(Color.darkGray, 1f);
                    Draw.rect(selection.fullIcon, dx, dy - 1f, s, h);
                    Draw.reset();
                    Draw.rect(selection.fullIcon, dx, dy, s, h);
                    dy -= 7f;
                }
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.blockAdditive);
            TjDraw.overdrive(this, Color.valueOf(Tmp.c1, "#D2F0FF"), Color.valueOf(Tmp.c2, "#CBA3FF"), heat);
            if (checkBuild(targetBuild)) TjDraw.overdrive(targetBuild, TjColor.phaseColor, heat);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (targetBuild != null) {
                TjDraw.drawPlace(getTarget(), targetBuild.tile.x, targetBuild.tile.y, true);
                drawItemSelection(getTarget());
                drawItemSelections(targetBuild, Seq.with(ammo, coolant));
            }
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();
            drawSelect();
        }

        @Override
        public void onProximityUpdate() {
            items.clear();
            liquids.clear();
            ammoTypes.clear();
            coolants.clear();
            if (!checkBuild(targetBuild = front())) {
                deselect();
                targetBuild = null;
                ammo = null;
                coolant = null;
                overdrive = -1;
                return;
            }

            var target = getTarget();

            if (target.hasItems) content.items().each(target::consumesItem, items::add);
            if (target.hasLiquids) content.liquids().each(target::consumesLiquid, liquids::add);

            if (target instanceof BaseTurret baseTurret) {
                if (target instanceof ItemTurret) {
                    ammoTypes.add(items);
                    items.clear();
                } else if (target instanceof LiquidTurret || target instanceof ContinuousLiquidTurret) {
                    ammoTypes.add(liquids);
                    liquids.clear();
                }

                if (baseTurret.coolant != null) {
                    liquids.each(baseTurret.coolant::consumes, coolants::add);
                    coolants.each(liquids::remove);
                }
            }
            consumes.clear().add(items).add(liquids);
        }

        @Override
        public void updateTile() {
            heat = Mathf.lerpDelta(heat, Mathf.num(overdrive > -1), 0.08f);
            if (targetBuild == null) return;

            items.each(v -> targetBuild.acceptItem(this, v), v -> targetBuild.handleStack(v, targetBuild.acceptStack(v, 1000000, this), this));
            liquids.each(v -> targetBuild.acceptLiquid(this, v), v -> targetBuild.liquids.set(v, Math.max(getTarget().liquidCapacity, targetBuild.liquids.get(v))));
            if (overdrive > -1) targetBuild.applyBoost(overdrives[overdrive], 61);
            if (getTarget().acceptsPayload) content.blocks().each(BaseSource::canProduce,
                    v -> payloadPool(v, team, BaseSource.dumpPayload(this, targetBuild)));
            if (getTarget().acceptsUnitPayloads) content.units().each(BaseSource::canProduce,
                    v -> payloadPool(v, team, BaseSource.dumpPayload(this, targetBuild)));

            if (getTarget(BaseTurret.class::isInstance)) {
                if (targetBuild instanceof ItemTurret.ItemTurretBuild build) {
                    if (ammo instanceof Item item) {
                        if (build.ammo.size == 1 && ((ItemTurret.ItemEntry) build.ammo.first()).item == item) {
                            build.ammo.first().amount = build.totalAmmo = ((ItemTurret) build.block).maxAmmo;
                        } else {
                            build.ammo.clear();
                            build.handleItem(this, item);
                        }
                    } else {
                        build.ammo.clear();
                        build.totalAmmo = 0;
                        build.reloadCounter = 0;
                    }
                } else {
                    if (ammo instanceof Liquid liquid) targetBuild.liquids.set(liquid, getTarget().liquidCapacity);
                    else ammoTypes.each(Liquid.class::isInstance, (Liquid liquid) -> targetBuild.liquids.set(liquid, 0));
                }
                if (coolant != null) targetBuild.liquids.set(coolant, getTarget().liquidCapacity);
                else coolants.each(liquid -> targetBuild.liquids.set(liquid, 0));
            }

            if (targetBuild instanceof Turret.TurretBuild && commandPos != null)
                targetBuild.control(LAccess.shoot, commandPos.x / tilesize, commandPos.y / tilesize, 1, 0);
        }

        private void content(Table table) {
            table.top().table(Styles.black3, in -> {
                if (getTarget(BaseTurret.class::isInstance)) {
                    var icon = getTarget() != null ? new TextureRegionDrawable(getTarget().uiIcon) : Icon.turret;
                    var tooltip = getTarget() != null ? getTarget().localizedName : bundle("ammo-config");
                    if (!ammoTypes.any()) {
                        in.image(icon).size(uiSize).tooltip(tooltip, true);
                        in.image(Icon.cancel);
                        in.add();
                    } else singleConfig(in, icon, tooltip, single -> eachRow(single, ammoTypes, it -> single.add(lazyButton(
                            new TextureRegionDrawable(it.uiIcon), () -> it == ammo, v -> configAll(() -> ammo = v ? it : null)))
                            .tooltip(it.emoji() + it.localizedName, true)),
                            () -> ammo = null, null);
                    in.add(lazyButton(Icon.cancel, neverChecked, ign -> configAll(Blocks.air))).size(uiSize)
                            .visible(() -> commandPos != null).tooltip("@stance.stop").row();
                }
                if (coolants.any()) {
                    singleConfig(in, Icon.star, bundle("config-boost"), single -> eachRow(single, coolants, it -> single.add(lazyButton(
                            new TextureRegionDrawable(it.uiIcon), () -> it == coolant, v -> configAll(() -> coolant = v ? it : null)))
                            .tooltip(getTarget() instanceof ReloadTurret reloadTurret ? it.emoji() + it.localizedName + "\n" + TjStat.boosters(reloadTurret, true, it) : it.localizedName, true)),
                            () -> coolant = null, () -> coolant = coolants.max(v -> v.heatCapacity)).row();
                }
                if (consumes.any()) {
                    singleConfig(in, Icon.download, bundle("config-consumes"), single -> eachRow(single, consumes, showConsumes(single, getTarget())), null, null).row();
                }
                if (getTarget(it -> it.canOverdrive)) {
                    singleConfig(in, Icon.effect, bundle("config-overdrive"), single -> {
                        single.add(lazyButton(new TextureRegionDrawable(Blocks.overdriveProjector.uiIcon), () -> overdrive == 0, v -> configAll(() -> overdrive = v ? 0 : -1))).tooltip("150%", true);
                        single.add(lazyButton(new TextureRegionDrawable(Blocks.overdriveProjector.uiIcon), () -> overdrive == 1, v -> configAll(() -> overdrive = v ? 1 : -1))).tooltip("225%", true);
                        single.add(lazyButton(new TextureRegionDrawable(Blocks.overdriveDome.uiIcon), () -> overdrive == 2, v -> configAll(() -> overdrive = v ? 2 : -1))).tooltip("250%", true);
                    }, () -> overdrive = -1, () -> overdrive = 2).row();
                }
            }).touchable(Touchable.enabled);
        }

        private Table singleConfig(Table table, Drawable drawable, String tooltip, Cons<Table> builder, @Nullable Runnable reset, @Nullable Runnable changed) {
            table.image(drawable).size(uiSize).tooltip(tooltip, true);
            table.table(reset != null ? Styles.black5 : Styles.black3, t -> builder.get(apply(t.left(), it -> it.defaults().size(uiSize)))).grow();
            if (reset != null) table.add(lazyButton(Icon.undo, neverChecked, ign -> configAll(reset))).size(uiSize).tooltip("@table.reset", true);
            if (changed != null) table.add(lazyButton(Icon.star, neverChecked, ign -> configAll(changed))).size(uiSize).tooltip("@table.favorite", true);
            return table;
        }

        private static <T> void eachRow(Table table, Seq<T> seq, Cons<T> cons) {
            final int count = 8;
            forEach(seq, (i, t) -> {
                cons.get(t);
                if (i % count == count - 1) table.row();
            });
        }

        /**
         * 摘自{@code TjConfigTable} {@code @deprecated}<p>
         */
        private static Cons<UnlockableContent> showConsumes(Table table, Block block) {
            return item -> {
                if (block == null) return;
                boolean b = false;
                for (var c : block.consumers) if (item instanceof Item v && c instanceof ConsumeItems cItems) {
                    for (var cItem : cItems.items) if (v == cItem.item) {
                        b = true;
                        stack(table, cItem).tooltip(cItem.item.localizedName, true);
                        break;
                    }
                    if (b) break;
                } else if (item instanceof Liquid v) {
                    if (c instanceof ConsumeLiquid cLiquid && v == cLiquid.liquid) {
                        b = true;
                        stack(table, new LiquidStack(cLiquid.liquid, cLiquid.amount)).tooltip(cLiquid.liquid.localizedName);
                        break;
                    } else if (c instanceof ConsumeLiquids cLiquids) {
                        for (var cLiquid : cLiquids.liquids) if (v == cLiquid.liquid) {
                            b = true;
                            stack(table, cLiquid).tooltip(cLiquid.liquid.localizedName);
                            break;
                        }
                    }
                    if (b) break;
                }
                if (!b) stack(table, new Image(item.uiIcon), "").tooltip(item.localizedName);
            };
        }

        private Cell<ImageButton> rebuildButton(Table table, Table root) {
            return table.button(shown ? Icon.eyeSmall : Icon.eyeOffSmall, Styles.clearNonei, () -> {
                shown = !shown;
                rebuild(root);
            }).tooltip(shown ? "@table.shown" : "@table.unshown", true).size(uiSize);
        }

        @Override
        public void buildConfiguration(Table table) {
            if (targetBuild == null) return;
            if (!shown) {
                leftList(table, t -> rebuildButton(t, table));
                table.table(Tex.pane, this::content);
                return;
            }
            table.background(Tex.pane);
            table.table(frame -> {
                frame.table(Styles.black6, list -> {
                    list.button(Icon.info, Styles.clearNonei, () -> {
                        ui.content.show(getTarget());
                        Events.fire(new EventType.BlockInfoEvent());
                    }).tooltip("@info.title", true).size(uiSize).row();
                    list.button(Icon.wrench, Styles.clearNonei, () -> {
                        table.background(null).clear();
                        leftList(table, t -> t.button(Icon.undo, Styles.clearNonei, () -> rebuild(table)).tooltip("@back", true).size(uiSize));
                        table.table(Tex.pane, t -> layout.build(block, t, false));
                        table.pack();
                    }).tooltip(getTarget(BaseTurret.class::isInstance) ? Core.bundle.format("table.change-turret", getTarget().size) : "...", true).size(uiSize).row();
                    rebuildButton(list, table).row();
                }).padRight(8).top();
                frame.image(new TextureRegion(getTarget().uiIcon)).tooltip(getTarget().localizedName, true).size(100f).pad(10f);
                frame.image().color(Pal.gray).width(4).pad(-9, 8, -9, 8).growY();
                frame.table(this::content).grow().row();
            }).left().row();
            table.image().color(Pal.gray).height(4).pad(8, -9, 8, -9).growX().row();
            table.table(bars -> {
                bars.defaults().growX().minWidth(300).height(18).pad(4);
                targetBuild.displayBars(bars);
            }).growX().row();
        }

        @Override
        public void onCommand(Vec2 target) {
            commandPos = target;
        }

        @Override
        public Vec2 getCommandPosition() {
            return commandPos;
        }

        @Override
        public boolean configureBy(Building building) {
            return building instanceof BuildType type && type.getTarget() == getTarget();
        }

        @Override
        public Object config() {
            return String.join(";", String.valueOf(w(getTarget())), String.valueOf(w(ammo)), String.valueOf(w(coolant)), String.valueOf(overdrive));
        }

        @Override
        public float heat() {
            return targetBuild != null ? super.heat() : 0f;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(ammo instanceof Liquid);
            write.s(w(ammo));
            write.s(w(coolant));
            write.b(overdrive);
            TypeIO.writeVecNullable(write, commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 0) {
                ammo = !read.bool()
                        ? content.item(read.s())
                        : content.liquid(read.s());
                coolant = content.liquid(read.s());
                overdrive = read.b();
            } else if (revision == 1) {
                ammo = !read.bool()
                        ? content.item(read.s())
                        : content.liquid(read.s());
                coolant = content.liquid(read.s());
                overdrive = read.b();
                commandPos = TypeIO.readVecNullable(read);
            }
        }
    }
}
