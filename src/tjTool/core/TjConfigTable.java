package tjTool.core;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import static tjTool.core.TjTable.*;

@SuppressWarnings("unused")
public class TjConfigTable {

    public static class Pack {
        protected Seq<BaseContent> contents = new Seq<>();
        protected Cons<Object> configure;
        protected Intp prefix;

        public Pack(Cons<Object> configure) {
            this.configure = configure;
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
            int i = prefix != null ? 1 : 0;
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
            return (lock == null || lock.get()) && build();
        }

        public boolean build() {
            return alwaysBuild;
        }

        protected void call(int config) {
            this.config = config;
            pack.configure.get(pack.config());
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
                    for (int i = 0; i < buttonRegion.size; i += 1) {
                        final int item = i;
                        if (holder != null) {
                            ImageButton button = t.button(buttonRegion.get(item), Styles.clearNoneTogglei, iconSize, () -> {
                            }).tooltip(buttonTip.get(item)).group(group).get();
                            button.changed(() -> call(button.isChecked() ? item : -1));
                            button.update(() -> button.setChecked(holder.get() == item));
                        } else {
                            t.table(f -> f.image(buttonRegion.get(item)).tooltip(buttonTip.get(item)).maxSize(iconSize).center());
                        }
                        if (i % 8 == 7) t.row();
                    }
                }).left();
                if (holder != null) {
                    table.button(Icon.undo, Styles.clearNonei, iconSize, () -> call(-1)).size(uiSize).tooltip("@table.reset").center();
                    if (favorite != null)
                        table.button(Icon.star, Styles.clearNonei, iconSize, () -> call(favorite.get())).size(uiSize).tooltip("@table.favorite").center();
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
        public boolean build() {
            return super.build() || buttonRegion.any();
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
                                }).tooltip(buttonTip.get(item)).group(group).get();
                                button.changed(() -> call(button.isChecked() ? value.get(item) : -1));
                                button.update(() -> button.setChecked(holder.get() == item));
                            } else {
                                t.table(f -> f.image(buttonRegion.get(item)).tooltip(buttonTip.get(item)).maxSize(iconSize).center());
                            }
                            if (i % 8 == 7) t.row();
                        });
                    }).left();
                    if (holder != null) {
                        table.button(Icon.undo, Styles.clearNonei, iconSize, () -> call(-1)).size(uiSize).tooltip("@table.reset").center();
                        if (favorite != null)
                            table.button(Icon.star, Styles.clearNonei, iconSize, () -> call(favorite.get())).size(uiSize).tooltip("@table.favorite").center();
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
        public boolean build() {
            return super.build() || items.get().any();
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

    public static void titleTable(Table table, String title, String label) {
        titleTable(table, title, label, true);
    }

    public static void titleTable(Table table, String title, String label, boolean collapsed) {
        table.table(tag -> {
            tag.add(title).left().color(Pal.gray);
            tag.image().height(4).color(Pal.gray).growX().pad(5);
            Collapser coll = new Collapser(new Table() {{
                add(label).color(Pal.lightishGray).growX().left();
            }}, collapsed);
            coll.setDuration(0.1f);
            tag.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> {
                i.getStyle().imageUp = !coll.isCollapsed() ? Icon.upOpen : Icon.downOpen;
                i.getStyle().imageUpColor = i.getStyle().imageDownColor = Pal.gray;
            }).size(8f).color(Pal.gray).right().row();
            tag.add(coll).growX().colspan(3).padTop(4).padLeft(20).padRight(20);
        }).growX().pad(4, 8, 4, 8).row();
    }

    public static Cons<Table> updateLog = table -> {
        table.add("更新日志").growX().left().color(Pal.accent).row();
        table.image().height(4).color(Pal.accent).growX().pad(5).padLeft(0).padRight(0).row();
        titleTable(table, "v1.1.5", """
                新增 >>
                - 护盾源
                - 基岩
                - 信标
                - 弹药源 更名为 定向源 功能恢复+新增
                
                为了避免碎片化的更新, 在 115 我们会尽可能多得添加内容,
                因此版本的更新会停滞一段时间.
                """, false);
        titleTable(table, "v1.1.4", """
                回退 >>
                - 弹药源 因UI缺陷和严重崩溃等问题回退至上一版本
                新增 >>
                - 任意源 现在可以输出载荷
                """);
        titleTable(table, "v1.1.3", """
                新增 >>
                - 弹药源 现在可以切换同尺寸的炮台类型
                """);
        titleTable(table, "v1.1.2", """
                调整 >>
                - v157
                - 太阳能源 精灵微调
                - 弹药源
                    现在不可被快捷旋转
                    更新逻辑优化
                    配置面板全面重构
                    炮台和选项的角标分离绘制
                """);
        titleTable(table, "v1.1.1", """
                新增 >>
                - 为 StatusEffects.none 覆盖新的精灵(原为空)
                - 为 太阳能源 添加无缝瓦片精灵
                - 新增更新日志
                覆盖 >>
                - 为 emanate(发散) 添加更多part
                - 为 逻辑显示单元 启用矩形放置
                """);
        titleTable(table, "v1.1", """
                调整 >>
                - 完善语言包, 现已覆盖简体中文和英文
                - 完善建筑介绍
                修复 >>
                - 修复弹药源跨队伍供弹的BUG
                """);
        titleTable(table, "v1.0.3", """
                调整 >>
                - 降低了 弹药源 的放置优先级
                """);
        titleTable(table, "v1.0.2", """
                新增 >>
                - 新增 修理源 和 再生源
                - 弹药源 现在可以一键选择效率最高的液体和超速倍率
                调整 >>
                - 超速源 贴图重绘
                修复 >>
                - 弹药源 现在可以将超速图层绘制在合适的图层
                - 弹药源 现在消耗栏已不再可以被点击
                """);
        titleTable(table, "v1.0.1", """
                新增 >>
                - 弹药源 添加超速配置
                修复 >>
                - 弹药源 现已兼容多人模式
                - 弹药源 现在复制可以保留配置
                - 紧急修复 弹药源 因索引越界导致的崩溃
                """);
        titleTable(table, "v1.0", """
                新增 >>
                - 添加了4个便于沙盒测试的方块
                - 当 任意源 与炮台相邻时警告
                修复 >>
                - 修复 弹药源 因索引越界导致的崩溃
                """);
        table.add("彩蛋").growX().left().color(Pal.accent).row();
        table.image().height(3).color(Pal.accent).growX().pad(5).padLeft(0).padRight(0).row();
        table.label(() -> TjDraw.rainbowStream("""
                其实这一页都是彩蛋喵
                以及这一页是硬编码的
                所以使用其他语言也只能看到中文
                关注 Neil 谢谢喵
                """)).growX().left().pad(8).row();
    };

}
