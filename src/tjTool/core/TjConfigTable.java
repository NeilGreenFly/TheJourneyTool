package tjTool.core;

import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * "button": {"icon", "handle"}
 */
public class TjConfigTable {

    private static final float uiSize = 40f;

    public static <Type extends UnlockableContent> void rowTable(Building building, Table table, Image icon, String tip, Seq<Type> items, int favorite, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        rowTable(building, table, icon, tip, items.map(item -> item.uiIcon), items.map(item -> item.localizedName), favorite, holder, closeSelect, groupIndex);
    }

    public static void rowTable(Building building, Table table, Image icon, String tip, Seq<TextureRegion> regions, Seq<String> tips, int favorite, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        table.add(icon).size(uiSize).tooltip(tip, true).center();
        if (regions.any()) {
            table.table(configTable(building, regions, tips, holder, closeSelect, groupIndex)).left();
            table.add(imageButton(Icon.undo, () -> building.configure(new int[]{groupIndex, -1}))).size(uiSize).tooltip(TjBundle.get("table", "reset"), true).center();
            if (favorite > -1 && favorite < regions.size)
                table.add(imageButton(Icon.star, () -> building.configure(new int[]{groupIndex, favorite}))).size(uiSize).tooltip(TjBundle.get("table", "favorite"), true).center();
            table.row();
        } else table.image(Icon.cancel).size(uiSize).center().row();
    }

    public static <Type extends UnlockableContent> Cons<Table> configTable(Building building, Seq<Type> items, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        return configTable(building, items.map(item -> item.uiIcon), items.map(item -> item.localizedName), holder, closeSelect, groupIndex);
    }

    public static Cons<Table> configTable(Building building, Seq<TextureRegion> regions, Seq<String> tips, Prov<Integer> holder, boolean closeSelect, int groupIndex) {
        return table -> {
            table.clear();
            table.background(Styles.black6).left().defaults().size(uiSize);
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);

            for (int i = 0; i < regions.size; i += 1) {
                configButton(table, group, holder, building::configure, closeSelect, regions.get(i), tips.get(i), groupIndex, i);
            }
        };
    }

    private static ImageButton imageButton(Drawable icon, Runnable r) {
        return imageButton(icon, Styles.clearNonei, r);
    }

    private static ImageButton imageButton(Drawable icon, ImageButton.ImageButtonStyle style, Runnable r) {
        var button = new ImageButton(icon, style);
        button.clicked(r);
        return button;
    }

    private static void configButton(
            Table table, ButtonGroup<ImageButton> group, Prov<Integer> holder,
            Cons<int[]> consumer, boolean closeSelect,
            TextureRegion region, String tip,
            int groupIndex, final int buttonIndex
    ) {
        ImageButton button = table.button(Tex.whiteui, Styles.clearNoneTogglei, 24f, () -> {
            if (closeSelect) Vars.control.input.config.hideConfig();
        }).tooltip(tip, true).group(group).get();

        if (groupIndex != -1) button.changed(() -> consumer.get(new int[]{groupIndex, button.isChecked() ? buttonIndex : -1}));
        button.getStyle().imageUp = new TextureRegionDrawable(region);
        button.update(() -> button.setChecked(holder.get() == buttonIndex));
    }

    public static <Type extends UnlockableContent> void rowImageTable(Table table, Image icon, String tip, Seq<Type> items) {
        table.add(icon).size(uiSize).tooltip(tip, true).center();
        if (items.any())
            table.table(rowImage -> {
                for (var item : items)
                    rowImage.table(frame -> frame.image(item.uiIcon).center()).tooltip(item.localizedName, true).size(uiSize);
            }).left().row();
        else table.image(Icon.cancel).size(uiSize).center().row();
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
        titleTable(table, "v1.1.2", """
                >>
                """, false);
        titleTable(table, "v1.1.1", """
                新增 >>
                - 为 StatusEffects.none 覆盖新的精灵(原为空)
                - 为 太阳能源 添加无缝瓦片精灵
                - 新增更新日志
                覆盖 >>
                - 为 emanate(发散) 添加更多part
                - 为 逻辑显示单元 启用矩形放置
                """, false);
        titleTable(table, "v1.1", """
                调整 >>
                - 完善语言包, 现已覆盖简体中文和英文
                - 完善建筑介绍
                修复 >>
                - 修复弹药源跨队伍供弹的BUG
                """, false);
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
//        table.label(() -> TjDraw.rainbowStream("""
//                v1.1.1
//                - 为 StatusEffects.none 覆盖新的精灵(原为空)
//
//                v1.1
//                - 完善语言包, 现已覆盖简体中文和英文
//                - 完善建筑介绍
//                - 修复弹药源跨队伍供弹的BUG
//
//                v1.0.3
//                - 调整了弹药源的放置优先级
//
//                v1.0.2
//                - 修复图层绘制错误的问题
//                - 现在消耗栏已不再可以被点击
//                - 新增修理源和再生源
//                - 超速源贴图重绘
//                - 新增快速选择
//                  现在可以一键选择效率最高的液体和超速倍率
//
//                v1.0.1
//                - 修复弹药源不能兼容多人模式的问题
//                - 弹药源添加超速选项
//                - 修复复制无法保留配置等BUG
//                - 紧急修复因索引越界导致的崩溃
//
//                v1.0
//                - 添加了4个便于沙盒测试的方块
//                - 修复因索引越界导致的崩溃
//                - 新增当 任意源 与炮台相邻时警告
//                """)).growX().left().row();
    };

}
