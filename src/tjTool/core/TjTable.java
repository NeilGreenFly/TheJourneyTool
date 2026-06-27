package tjTool.core;

import arc.Core;
import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;

import static mindustry.Vars.control;

/**
 * - 该部分暂未完善 -<p>
 * - 该部分暂未完善 -<p>
 * - 该部分暂未完善 -<p>
 * 由于旧的 {@code TjConfigTable} 将用户面和数据过度耦合导致极难扩展, 所以我们需要一种新的设计, 但是显然这需要一点时间...<p>
 * For example:
 * <blockquote><pre> {@code
 *     public Item item = null;
 *     public Liquid liquid = null;
 *     public Block block = null;
 *     public UnitType unit = null;
 *     public Layout layout = new Layout(this::configure).with(
 *             new Page(Icon.box).with(Selection.unlockableContent(
 *                     () -> content.items().as(),
 *                     () -> item).setValue(v -> (int) v.id)),
 *             new Page(Icon.liquid).with(Selection.unlockableContent(
 *                     () -> content.liquids().as(),
 *                     () -> liquid).setValue(v -> (int) v.id)),
 *             new Page(Icon.crafting).with(Selection.unlockableContent(
 *                     () -> content.blocks().select(this::canProduce).as(),
 *                     () -> block).setValue(v -> (int) v.id)),
 *             new Page(Icon.units).with(Selection.unlockableContent(
 *                     () -> content.units().select(this::canProduce).as(),
 *                     () -> unit).setValue(v -> (int) v.id))
 *     );
 * } </pre></blockquote>
 * 在构造方法中注册 :
 * <blockquote><pre>
 *     config(int[].class, (MyBuild tile, int[] config) -> {
 *         tile.item = content.item(config[0]);
 *         tile.liquid = content.liquid(config[1]);
 *         tile.block = content.block(config[2]);
 *         tile.unit = content.unit(config[3]);
 *     });
 * </pre></blockquote>
 * @author NeilGreenFly
 * @see ItemSelection#buildTable(Block, Table, Seq, Prov, Cons, boolean, int, int)
 */

@SuppressWarnings("unused")
public class TjTable {
    public static final float uiSize = 44f;
    public static final float iconSize = 32f;

    public static <T> void forEach(T[] it, ForCons<T> cons) {
        for (int index = 0; index < it.length; index += 1) cons.get(index, it[index]);
    }

    public static <T> void forEach(Seq<T> it, ForCons<T> cons) {
        for (int index = 0; index < it.size; index += 1) cons.get(index, it.get(index));
    }

    public static class Layout {
        public Seq<Page> pages = new Seq<>();
        public Cons<Object> configure;

        public Layout(Cons<Object> configure) {
            this.configure = configure;
        }

        public Layout add(Page page) {
            pages.add(page);
            page.contents.each(content -> content.layout = this);
            return this;
        }

        public Layout with(Page... pages) {
            for (var page : pages) add(page);
            return this;
        }

        public void configure() {
            configure.get(config());
        }

        public int[] config() {
            Seq<Content<?>> contents = pages.flatMap(page -> page.contents.select(content -> content.value != null));
            int[] items = new int[contents.size];
            forEach(contents, (idx, item) -> items[idx] = item.getConfig());
            return items;
        }

        /**
         * For example:
         * <blockquote><pre>
         *     {@code @Override}
         *     public void buildConfiguration({@code Table table}) {
         *         layout.build(block, table.background(Tex.pane), false);
         *     }
         * </pre></blockquote>
         * @author NeilGreenFly
         */
        public void build(@Nullable Block block, Table table, boolean closeSelect) {
            table.left();
            Table frame = new Table().top().left(); // .background(Styles.black6)
            ScrollPane panel = new ScrollPane(frame, Styles.smallPane);
            panel.setScrollingDisabled(true, false);
            panel.setOverscroll(false, false);

            panel.exited(() -> {
                if (panel.hasScroll()) Core.scene.setScrollFocus(null);
            });
            if (block != null) {
                panel.setScrollYForce(block.selectScroll);
                panel.update(() -> block.selectScroll = panel.getScrollY());
            }

            table.table(list -> {
                ButtonGroup<ImageButton> listGroup = new ButtonGroup<>();
                pages.each(page -> list.button(page.icon, Styles.clearNoneTogglei, iconSize, () -> {
                    frame.clearChildren();
                    page.contents.each(content -> frame.table(content.build(closeSelect)));
                }).size(uiSize).group(listGroup).row());
                list.getChildren().get(0).fireClick();
            }).top();
            table.image().width(5).color(Pal.gray).growY().padRight(10);
            table.add(panel).size(uiSize * 8 + 10, uiSize * 6);
        }
    }

    public static class Page {
        public Seq<Content<?>> contents = new Seq<>();
        public Drawable icon;

        public Page(Drawable icon) {
            this.icon = icon;
        }

        public Page() {
            this(Icon.star);
        }

        // public Page setIcon(Drawable icon) {
        //     this.icon = icon;
        //     return this;
        // }

        public Page with(Content<?>... contents) {
            this.contents.add(contents);
            return this;
        }
    }

    public static abstract class Content<Type> {
        public Intf<Type> value;
        protected Layout layout;
        protected int config = -2;
        // public boolean save;

        abstract public Cons<Table> build(boolean closeSelect);
        abstract protected void call(int config);
        abstract protected int getConfig();
    }

    public static class Selection<Type> extends Content<Type> {
        public Prov<Seq<Type>> items;
        public Func<Type, TextureRegion> buttonRegion;
        public Func<Type, String> buttonTip;
        public Prov<Type> holder;

        public Selection(Prov<Seq<Type>> items, Func<Type, TextureRegion> buttonRegion, Func<Type, String> buttonTip, Prov<Type> holder) {
            this.items = items;
            this.buttonRegion = buttonRegion;
            this.buttonTip = buttonTip;
            this.holder = holder;
        }

        public Selection<Type> setValue(Intf<Type> value) {
            this.value = value;
            return this;
        }

        public static Selection<UnlockableContent> unlockableContent(Prov<Seq<UnlockableContent>> items, Prov<UnlockableContent> holder) {
            return new Selection<>(items, item -> item.uiIcon, item -> item.localizedName, holder);
        }

        @Override
        public void call(int config) {
            // config = Mathf.clamp(config, -1, items.size);
            this.config = config;
            layout.configure();
            this.config = -2;
        }

        @Override
        public int getConfig() {
            if (config != -2) return config;
            Type item = holder.get();
            return item != null ? value.get(item) : -1;
        }

        @Override
        public Cons<Table> build(boolean closeSelect) {
            return table -> {
                table.top().left().defaults().size(uiSize);
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                group.setMinCheckCount(0);
                forEach(items.get(), (idx, item) -> {
                    ImageButton button = table.button(new TextureRegionDrawable(buttonRegion.get(item)), Styles.clearNoneTogglei, iconSize, () -> {
                        if (closeSelect) control.input.config.hideConfig();
                    }).tooltip(buttonTip.get(item)).group(group).get();
                    button.changed(value == null
                            ? () -> layout.configure.get(button.isChecked() ? item : null)
                            : () -> call(button.isChecked() ? value.get(item) : -1));
                    button.update(() -> button.setChecked(holder.get() == item));
                    if (idx % 8 == 7) table.row();
                });
            };
        }
    }

}
