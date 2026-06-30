package tjTool.core;

import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import java.util.Objects;

import static tjTool.core.TjTable.*;

@SuppressWarnings("unused")
public class TjConfigTable {

    public static Func<UnlockableContent, Option> optionMapper = item -> new Option(item.uiIcon, item.localizedName);

    public static class ConfigPack {
        public Building building;
        public Seq<ConfigContent> contents;

        public ConfigPack(Building building) {
            this.building = building;
            this.contents = new Seq<>();
        }

        public ConfigPack add(ConfigContent content) {
            contents.add(content.setPack(this));
            return this;
        }

        public ConfigPack with(ConfigContent... contents) {
            for (var content : contents)
                add(content);
            return this;
        }

        public ConfigContent get(int index) {
            return contents.get(index);
        }

        public int getIndex(int index) {
            return get(index).getIndex();
        }

        public <Type> Type from(Seq<Type> content, int index) {
            int idx = getIndex(index);
            return idx != -1 && idx < content.size
                    ? content.get(idx)
                    : null;
        }

        public void reset() {
            for (var content : contents)
                content.reset();
        }

        public void clear() {
            for (var content : contents)
                content.clear();
        }

        public int[] config() {
            return config(true);
        }

        public int[] config(boolean save) {
            int[] items = new int[contents.size];
            for (int i = 0; i < contents.size; ++i)
                items[i] = !save || contents.get(i).save
                        ? contents.get(i).index
                        : -1;
            return items;
        }

        public void configure() {
            building.configure(config(false));
        }

        public void receive(int[] v) {
            for (int i = 0; i < contents.size; ++i)
                contents.get(i).index = v[i];
        }

        public void write(Writes write) {
            for (var content : contents)
                if (content.save)
                    write.i(content.index);
        }

        public void read(Reads read) {
            for (var content : contents)
                if (content.save)
                    content.index = read.i();
        }

        public Table build(Table table) {
            return table.table(t -> {
                for (var content : contents)
                    content.build(t);
            }).get();
        }

        public Table build(Table table, ConfigPage... pages) {
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            Table panel = new Table();
            table.table(list -> {
                for (var page : pages)
                    list.button(page.icon, Styles.clearNoneTogglei, iconSize, () -> {
                        panel.clearChildren();
                        for (var idx : page.contents)
                            contents.get(idx).build(panel);
                        table.pack();
                    }).size(uiSize).group(group).row();
                list.getChildren().get(0).fireClick();
            }).top();
            table.image().width(5).color(Pal.gray).growY().padRight(10);
            table.add(panel).top();
            return table;
        }
    }

    public static class ConfigPage {
        public Drawable icon;
        public int[] contents;

        public ConfigPage(Drawable icon, int[] contents) {
            this.icon = icon;
            this.contents = contents;
        }
    }

    public static class ConfigContent {
        protected ConfigPack pack;
        public boolean lock;
        public boolean alwaysBuild;
        public boolean isStatic;
        public boolean unReset;
        public boolean save;
        protected Image icon;
        protected String tip;

        public Seq<Option> options;
        protected int favorite;
        protected int index;

        public ConfigContent() {
            this(new Image(Icon.cancel), "=w=");
        }

        public ConfigContent(Image icon, String tip) {
            this(icon, tip, new Seq<>());
        }

        public ConfigContent(Image icon, String tip, Seq<Option> options) {
            this.icon = icon;
            this.tip = tip;
            this.lock = false;
            this.alwaysBuild = true;
            this.isStatic = false;
            this.unReset = false;
            this.save = true;
            this.options = options;
            this.favorite = -1;
            this.index = -1;
        }

        private ConfigContent setPack(ConfigPack pack) {
            this.pack = pack;
            return this;
        }

        public ConfigContent setLock(boolean lock) {
            this.lock = lock;
            return this;
        }

        public ConfigContent setAlwaysBuild(boolean alwaysBuild) {
            this.alwaysBuild = alwaysBuild;
            return this;
        }

        public ConfigContent setStatic(boolean aStatic) {
            isStatic = aStatic;
            return this;
        }

        public ConfigContent setUnReset(boolean unReset) {
            this.unReset = unReset;
            return this;
        }

        public ConfigContent setSave(boolean save) {
            this.save = save;
            return this;
        }

        public ConfigContent setIcon(Image icon) {
            this.icon = icon;
            return this;
        }

        public ConfigContent setTip(String tip) {
            this.tip = tip;
            return this;
        }

        public ConfigContent setOptions(Seq<Option> options) {
            this.options = options;
            return this;
        }

        public ConfigContent setFavorite(int favorite) {
            this.favorite = favorite;
            return this;
        }

        public void setIndex(int index) {
            index = Mathf.clamp(index, -1, options.size - 1);
            if (this.index != index) {
                this.index = index;
                pack.configure();
            }
        }

        public int getIndex() {
            return index;
        }

        public int indexOf(Option option) {
            for (int i = 0; i < options.size; i++)
                if (options.get(i).equals(option))
                    return i;
            return -1;
        }

        public void reset() {
            if (!unReset)
                index = -1;
        }

        public void clear() {
            if (!isStatic)
                options = new Seq<>();
        }

        public void add(TextureRegion region, String tip) {
            options.add(new Option(region, tip));
        }

        public Cons<Table> table() {
            return !lock
            ? table -> {
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                if (save) group.setMinCheckCount(0);
                table.background(Styles.black6).defaults().size(uiSize);
                for (int i = 0; i < options.size; ++i) {
                    final int idx = i;
                    Option option = options.get(idx);
                    ImageButton button = table.button(new TextureRegionDrawable(option.region), Styles.clearNoneTogglei, iconSize, () -> {
                    }).tooltip(option.tip).group(group).get();
                    button.changed(() -> setIndex(button.isChecked() ? idx : -1));
                    button.update(() -> button.setChecked(getIndex() == idx));
                    if (i % 8 == 7) table.row();
                }
            }
            : table -> {
                table.background(Styles.black3).defaults().size(uiSize);
                int count = 0;
                for (var option : options) {
                    table.table(t -> t.image(option.region).tooltip(option.tip).maxSize(iconSize).center());
                    if (++count % 8 == 0) table.row();
                }
            };
        }

        public void build(Table table) {
            if (alwaysBuild || options.any()) {
                table.add(icon).size(uiSize).tooltip(tip, true).center();
                if (options.any()) {
                    table.table(table()).left();
                    if (!lock) {
                        if (save)
                            table.button(Icon.undo, Styles.clearNonei, iconSize, () -> setIndex(-1)).size(uiSize).tooltip("@table.reset").center();
                        if (favorite > -1 && favorite < options.size)
                            table.button(Icon.star, Styles.clearNonei, iconSize, () -> setIndex(favorite)).size(uiSize).tooltip("@table.favorite").center();
                    }
                } else table.image(Icon.cancel).size(iconSize).center();
                table.left().row();
            }
        }
    }

    public static class Option {
        public TextureRegion region;
        public String tip;

        public Option(TextureRegion region, String tip) {
            this.region = region;
            this.tip = tip;
        }

        public boolean equals(Option other) {
            return other.region == region && Objects.equals(other.tip, tip);
        }
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
