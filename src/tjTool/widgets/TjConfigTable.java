package tjTool.widgets;

import arc.func.*;
import arc.math.Mathf;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.consumers.*;

import static tjTool.widgets.TjTable.*;
import static tjTool.world.Range.*;

/**
 * @deprecated 早期的冗余设计, 因错误地将用户面与逻辑相耦合而极难维护, 遂弃用.
 * @since 1.0
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("unused")
public final class TjConfigTable {

    public static class Pack {
        protected Seq<BaseContent> contents = new Seq<>();
        protected Building building;
        protected Intp prefix;

        public Pack(Building building) {
            this.building = building;
        }

        public Pack prefix(Intp prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Pack with(BaseContent... contents) {
            for (var content : contents) (content.pack = this).contents.add(content);
            return this;
        }

        public int[] config() {
            int i = Mathf.num(prefix != null);
            var contents = this.contents.select(BaseContent::isEnabled);
            int[] items = new int[contents.size + i];
            if (prefix != null) items[0] = prefix.get();
            forEach(contents, (idx, item) -> items[idx + i] = item.getConfig());
            return items;
        }

        public Cons<Table> build() {
            return table -> contents.each(BaseContent::shouldBuild, content -> content.build.get(table).row());
        }
    }

    public static abstract class BaseContent {
        protected Pack pack;
        protected int config = -2;

        protected Func<Table, Table> build;

        protected Intp favorite;
        protected boolean alwaysBuild = false;
        protected Boolp lock;

        abstract public boolean isEnabled();
        abstract protected int getConfig();

        public boolean shouldBuild() {
            return (lock == null || lock.get()) && allowBuild();
        }

        public boolean allowBuild() {
            return alwaysBuild;
        }

        protected void call(int config) {
            this.config = config;
            pack.building.configure(pack.config());
            this.config = -2;
        }

        public BaseContent setAlwaysBuild(boolean alwaysBuild) {
            this.alwaysBuild = alwaysBuild;
            return this;
        }

        public BaseContent setLock(Boolp lock) {
            this.lock = lock;
            return this;
        }
    }

    public static class IntContent extends BaseContent {
        protected Seq<TextureRegionDrawable> buttonRegion = new Seq<>();
        protected Seq<String> buttonTip = new Seq<>();
        protected @Nullable Intp holder;

        public IntContent(
                Prov<Image> icon,
                Prov<String> tip,
                Intp holder) {
            this.holder = holder;
            this.build = table -> {
                table.add(icon.get()).size(uiSize).tooltip(tip.get(), true).center();
                table.table(holder != null ? Styles.black6 : Styles.black3, t -> {
                    t.defaults().size(uiSize);
                    ButtonGroup<ImageButton> group = new ButtonGroup<>();
                    group.setMinCheckCount(0);
                    forRange(buttonRegion.size, i -> {
                        if (holder != null) {
                            ImageButton button = t.button(buttonRegion.get(i), Styles.clearNoneTogglei, iconSize, () -> {
                            }).tooltip(buttonTip.get(i), true).group(group).get();
                            button.changed(() -> call(button.isChecked() ? i : -1));
                            button.update(() -> button.setChecked(holder.get() == i));
                        } else {
                            t.table(f -> f.image(buttonRegion.get(i)).tooltip(buttonTip.get(i), true).maxSize(iconSize).center());
                        }
                        if (i % 8 == 7) t.row();
                    });
                }).left();
                if (holder != null) {
                    table.button(Icon.undo, Styles.clearNonei, iconSize, () -> call(-1)).size(uiSize).tooltip("@table.reset", true).center();
                    if (favorite != null)
                        table.button(Icon.star, Styles.clearNonei, iconSize, () -> call(favorite.get())).size(uiSize).tooltip("@table.favorite", true).center();
                }
                return table;
            };
        }

        public IntContent add(TextureRegionDrawable buttonRegion, String buttonTip) {
            this.buttonRegion.add(buttonRegion);
            this.buttonTip.add(buttonTip);
            return this;
        }

        @Override
        protected int getConfig() {
            return config != -2 ? config : holder.get();
        }

        @Override
        public boolean isEnabled() {
            return holder != null;
        }

        @Override
        public boolean allowBuild() {
            return super.allowBuild() || buttonRegion.any();
        }

        public IntContent setFavorite(int favorite) {
            this.favorite = () -> favorite;
            return this;
        }
    }

    public static class TypeContent<Type> extends BaseContent {
        protected Prov<Seq<Type>> items;
        protected @Nullable Prov<Type> holder;
        protected Intf<Type> value;

        public TypeContent(
                Prov<Image> icon,
                Prov<String> tip,
                Prov<Seq<Type>> items,
                Func<Type, TextureRegionDrawable> buttonRegion,
                Func<Type, String> buttonTip,
                Prov<Type> holder, Intf<Type> value) {
            this.items = items;
            this.holder = holder;
            this.value = value;
            this.build = table -> {
                table.add(icon.get()).size(uiSize).tooltip(tip.get(), true).center();
                if (items.get().any()) {
                    table.table(holder != null ? Styles.black6 : Styles.black3, t -> {
                        t.defaults().size(uiSize);
                        ButtonGroup<ImageButton> group = new ButtonGroup<>();
                        group.setMinCheckCount(0);
                        forEach(items.get(), (i, item) -> {
                            if (holder != null) {
                                ImageButton button = t.button(buttonRegion.get(item), Styles.clearNoneTogglei, iconSize, () -> {
                                }).tooltip(buttonTip.get(item), true).group(group).get();
                                button.changed(() -> call(button.isChecked() ? value.get(item) : -1));
                                button.update(() -> button.setChecked(holder.get() == item));
                            } else {
                                t.table(f -> f.image(buttonRegion.get(item)).tooltip(buttonTip.get(item), true).maxSize(iconSize).center());
                            }
                            if (i % 8 == 7) t.row();
                        });
                    }).left();
                    if (holder != null) {
                        table.button(Icon.undo, Styles.clearNonei, iconSize, () -> call(-1)).size(uiSize).tooltip("@table.reset", true).center();
                        if (favorite != null)
                            table.button(Icon.star, Styles.clearNonei, iconSize, () -> call(favorite.get())).size(uiSize).tooltip("@table.favorite", true).center();
                    }
                } else table.image(Icon.cancel).size(iconSize).center();
                return table;
            };
        }

        public static TypeContent<UnlockableContent> unlockableContent(Prov<Image> icon, Prov<String> tip, Prov<Seq<UnlockableContent>> items, Prov<UnlockableContent> holder) {
            return unlockableContent(icon, tip, items, item -> item.localizedName, holder);
        }

        public static TypeContent<UnlockableContent> unlockableContent(Prov<Image> icon, Prov<String> tip, Prov<Seq<UnlockableContent>> items, Func<UnlockableContent, String> buttonTip, Prov<UnlockableContent> holder) {
            return new TypeContent<>(icon, tip, items, item -> new TextureRegionDrawable(item.uiIcon), buttonTip, holder, item -> item.id);
        }

        @Override
        protected int getConfig() {
            if (config != -2) return config;
            Type item = holder.get();
            return item != null ? value.get(item) : -1;
        }

        @Override
        public boolean allowBuild() {
            return super.allowBuild() || items.get().any();
        }

        @Override
        public boolean isEnabled() {
            return holder != null;
        }

        public TypeContent<Type> setFavorite(Floatf<Type> favorite) {
            this.favorite = () -> value.get(items.get().max(favorite));
            return this;
        }

        // public <T extends Comparable<T>> tContent<Type> setFavorite(Function<Type, T> favorite) {
        //     this.favorite = value.get(items.get().max(Comparator.comparing(favorite)));
        //     return this;
        // }
    }

    /**
     * 这个类是为弥补{@code TypeContent}在构建不可选行时无法显示数字而特化出来的一个类, 仅用于显示传入建筑的消耗器列表(仅部分).<p>
     * 但是这个类是极难维护的, 由于原版对于消耗器极其差异化的实现, 不同基类的元素也没有统一的方法获取消耗数量,
     * 似乎{@code Anuke}只是在需要的位置临时实现了对数量的获取, 而不考虑其他位置可能也需要用到.
     * 总之如果您也对这段实现感到窒息, 我们为此感到抱歉, 但我们绝无可能再回来优化它, 除非我们找到了更好的方法.<p>
     * 虽然原版相关的方法是公开的, 如果我们十分摒弃重复造轮子也的确可以直接使用, 但很不幸的是我们使用的展示尺寸与原版方法是不一样的(是的, 原版方法直接把尺寸写死了)...
     */
    public static class StackContent extends BaseContent {
        protected Prov<Seq<UnlockableContent>> items;

        public StackContent(Prov<Image> icon, Prov<String> tip, Prov<Seq<UnlockableContent>> items, Prov<Block> block) {
            this.items = items;
            this.build = table -> {
                table.add(icon.get()).size(uiSize).tooltip(tip.get(), true).center();
                if (items.get().any()) {
                    table.table(Styles.black3, row -> {
                        row.defaults().size(uiSize);
                        forEach(items.get(), (i, item) -> {
                            boolean b = false;
                            for (var c : block.get().consumers) if (item instanceof Item v && c instanceof ConsumeItems cItems) {
                                for (var cItem : cItems.items) if (v == cItem.item) {
                                    b = true;
                                    stack(row, cItem).tooltip(cItem.item.localizedName, true);
                                    break;
                                }
                                if (b) break;
                            } else if (item instanceof Liquid v) {
                                if (c instanceof ConsumeLiquid cLiquid && v == cLiquid.liquid) {
                                    b = true;
                                    stack(row, new LiquidStack(cLiquid.liquid, cLiquid.amount)).tooltip(cLiquid.liquid.localizedName);
                                    break;
                                } else if (c instanceof ConsumeLiquids cLiquids) {
                                    for (var cLiquid : cLiquids.liquids) if (v == cLiquid.liquid) {
                                        b = true;
                                        stack(row, cLiquid).tooltip(cLiquid.liquid.localizedName);
                                        break;
                                    }
                                }
                                if (b) break;
                            }
                            if (!b) stack(row, new Image(item.uiIcon), "").tooltip(item.localizedName);
                            if (i % 8 == 7) row.row();
                        });
                    }).left();
                } else table.image(Icon.cancel).size(iconSize).center();
                return table;
            };
        }

        @Override
        public boolean allowBuild() {
            return super.allowBuild() || items.get().any();
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        protected int getConfig() {
            return -1;
        }
    }

}
