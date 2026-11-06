package tjTool.dialogs;

import arc.Core;
import arc.flabel.FLabel;
import arc.func.Cons;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.core.TjBundle;

import static tjTool.world.LazyGetter.apply;

public class UpdateDialog extends BaseDialog {
    public static Cons<Table> updateLog = table -> {
        table.add("更新日志").growX().left().color(Pal.accent).row();
        table.image().height(4).color(Pal.accent).growX().pad(5).padLeft(0).padRight(0).row();

        Table frame = new Table().top().left();
        table.add(apply(new ScrollPane(frame, Styles.smallPane), panel -> {
            panel.setScrollingDisabled(true, false);
            panel.setOverscroll(false, false);
            panel.exited(() -> {
                if (panel.hasScroll()) Core.scene.setScrollFocus(null);
            });
        })).growY().row();

        titleTable(frame, "v1.1.7 alpha", """
                新增 & 修复 >>
                - 修复任意源对管道错误的输出物品
                - 任意源新增独立输出配置
                - 任意源 & 任意黑洞 现在可以覆盖流体元件
                - 移除了任意源多余的介绍
                - 为 StatusEffects.invincible 覆盖新的精灵(原为空)
                - 超速源现在可以配置提速和范围
                """, false);
        titleTable(frame, "v1.1.7 alpha 其他方面", """
                新增 >>
                - 复合分类器
                - 塑钢交叉器
                - 定向流体路由器
                - 流体装卸器
                - 定向流体装卸器
                - 修复墙
                - 大型修复墙
                - 联合墙 (未完成)
                - [accent]复合工厂[]
                - [magenta]锻星砧[] (未完成)
                - [magenta]锻星砧接口占位符[] (未完成)
                - [magenta]锻星砧增幅器[] (未完成)
                """);
        titleTable(frame, "v1.1.6", """
                新增 >>
                - 任意源 现在可以设置单位移动指令
                - 定向源 部分消耗添加数量显示
                - 新增 任意黑洞
                """);
        titleTable(frame, "v1.1.5", """
                新增 >>
                - 护盾源
                - 基岩
                - 信标 (仅装饰, 无功能, 不能保存配置)
                - 弹药源 更名为 定向源
                    功能优化和扩展
                - 任意源 现在可以生成单位
                """);
        titleTable(frame, "v1.1.4", """
                回退 >>
                - 弹药源 因UI缺陷和严重崩溃等问题回退至上一版本
                新增 >>
                - 任意源 现在可以输出载荷
                """);
        titleTable(frame, "v1.1.3", """
                新增 >>
                - 弹药源 现在可以切换同尺寸的炮台类型
                """);
        titleTable(frame, "v1.1.2", """
                调整 >>
                - v157
                - 太阳能源 精灵微调
                - 弹药源
                    现在不可被快捷旋转
                    更新逻辑优化
                    配置面板全面重构
                    炮台和选项的角标分离绘制
                """);
        titleTable(frame, "v1.1.1", """
                新增 >>
                - 为 StatusEffects.none 覆盖新的精灵(原为空)
                - 为 太阳能源 添加无缝瓦片精灵
                - 新增更新日志
                覆盖 >>
                - 为 emanate(发散) 添加更多part
                - 为 逻辑显示单元 启用矩形放置
                """);
        titleTable(frame, "v1.1", """
                调整 >>
                - 完善语言包, 现已覆盖简体中文和英文
                - 完善建筑介绍
                修复 >>
                - 修复弹药源跨队伍供弹的BUG
                """);
        titleTable(frame, "v1.0.3", """
                调整 >>
                - 降低了 弹药源 的放置优先级
                """);
        titleTable(frame, "v1.0.2", """
                新增 >>
                - 新增 修理源 和 再生源
                - 弹药源 现在可以一键选择效率最高的液体和超速倍率
                调整 >>
                - 超速源 贴图重绘
                修复 >>
                - 弹药源 现在可以将超速图层绘制在合适的图层
                - 弹药源 现在消耗栏已不再可以被点击
                """);
        titleTable(frame, "v1.0.1", """
                新增 >>
                - 弹药源 添加超速配置
                修复 >>
                - 弹药源 现已兼容多人模式
                - 弹药源 现在复制可以保留配置
                - 紧急修复 弹药源 因索引越界导致的崩溃
                """);
        titleTable(frame, "v1.0", """
                新增 >>
                - 添加了4个便于沙盒测试的方块
                - 当 任意源 与炮台相邻时警告
                修复 >>
                - 修复 弹药源 因索引越界导致的崩溃
                """);
        table.add("彩蛋").growX().left().color(Pal.accent).row();
        table.image().height(3).color(Pal.accent).growX().pad(5).padLeft(0).padRight(0).row();
        // Note 外边必须套一层 Table, 否则在展开 Collapser 超出屏幕导致 ScrollPane 启用滑块后, FLabel 会变回白色, 原因不明.
        // Note 好我找到原因了, 是 .growX() 导致的, 这里不可以使用 grow 方法.
        table.add(new FLabel("""
                {rainbow=2;;0.25}\
                其实这一页都是彩蛋喵
                以及这一页是硬编码的
                所以使用其他语言也只能看到中文
                关注 Neil 谢谢喵\
                {/rainbow}""")).left().pad(8).row();
    };

    public UpdateDialog() {
        super(TjBundle.getThis("saying"));
        cont.button("=", SettingsDialog::show).top().left().get().color.a(0.2f); // debug
        cont.table(updateLog).growY();
        addCloseButton();
    }

    public static void titleTable(Table table, String title, String label) {
        titleTable(table, title, label, true);
    }

    public static void titleTable(Table table, String title, String label, boolean collapsed) {
        table.table(tag -> {
            tag.add(title).left().color(Pal.gray);
            tag.image().height(4).color(Pal.gray).growX().pad(5);
            Collapser coll = new Collapser(new Table().add("[lightgray]" + label).growX().left().getTable(), collapsed);
            coll.setDuration(0.1f);
            tag.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> {
                i.getStyle().imageUp = !coll.isCollapsed() ? Icon.upOpen : Icon.downOpen;
                i.getStyle().imageUpColor = i.getStyle().imageDownColor = Pal.gray;
            }).size(8f).color(Pal.gray).right().padLeft(10).padRight(10).row();
            tag.add(coll).growX().colspan(3).padTop(4).padLeft(20).padRight(20);
        }).growX().pad(4, 8, 4, 8).row();
    }
}
