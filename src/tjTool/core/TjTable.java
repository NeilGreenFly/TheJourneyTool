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

import java.util.Iterator;

import static mindustry.Vars.control;

/**
 * - 该部分暂未完善 -<p>
 * - 该部分暂未完善 -<p>
 * - 该部分暂未完善 -<p>
 * 由于旧的 TjConfigTable 将用户面和数据过度耦合导致极难扩展, 所以我们需要一种新的设计, 但是显然这需要一点时间...<p>
 * For example:
 * <blockquote><pre> {@code
 *     public Item item = null;
 *     public Liquid liquid = null;
 *     public Block block = null;
 *     public UnitType unit = null;
 *     public Layout layout = new Layout(this::configure).with(
 *             new Content&lt;&gt;(content.items(),
 *                           v -> v.uiIcon, v -> v.localizedName,
 *                           () -> item, v -> (int) v.id).setIcon(Icon.box),
 *             new Content&lt;&gt;(content.liquids(),
 *                           v -> v.uiIcon, v -> v.localizedName,
 *                           () -> liquid, v -> (int) v.id).setIcon(Icon.liquid),
 *             new Content&lt;&gt;(content.blocks().select(this::canProduce),
 *                           v -> v.uiIcon, v -> v.localizedName,
 *                           () -> block, v -> (int) v.id).setIcon(Icon.crafting),
 *             new Content&lt;&gt;(content.units().select(this::canProduce),
 *                           v -> v.uiIcon, v -> v.localizedName,
 *                           () -> unit, v -> (int) v.id).setIcon(Icon.units)
 *     );
 * } </pre></blockquote>
 * 在构造方法中注册 :
 * <blockquote><pre>
 *     config(int[].class, (BeaconBuild tile, int[] config) -> {
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

    public static Func<? extends UnlockableContent, TextureRegion> getRegion = v -> v.uiIcon;
    public static Func<? extends UnlockableContent, String> getTip = v -> v.localizedName;

    public static <T> void forEach(Iterator<T> it, ForCons<T> cons) {
        for (int index = 0; it.hasNext(); index += 1) cons.get(index, it.next());
    }

    public static <T> void forEach(T[] it, ForCons<T> cons) {
        for (int index = 0; index < it.length; index += 1) cons.get(index, it[index]);
    }

    public static <T> void forEach(Seq<T> it, ForCons<T> cons) {
        for (int index = 0; index < it.size; index += 1) cons.get(index, it.get(index));
    }

    public static class Layout {
        public Cons<int[]> configure;
        public Seq<Content> contents = new Seq<>();

        public Layout(Cons<int[]> configure) {
            this.configure = configure;
        }

        public Layout add(Content content) {
            contents.add(content);
            return content.layout = this;
        }

        public Layout with(Content... contents) {
            for (var content : contents) add(content);
            return this;
        }

        public void configure() {
            configure.get(config());
        }

        public int[] config() {
            int[] items = new int[contents.size];
            forEach(contents, (idx, item) -> {
                if (item instanceof ConfigurableContent cItem)
                    items[idx] = cItem.getConfig();
            });
            return items;
        }

//        public int[] config(boolean save) {
//            int[] items = new int[contents.size];
//            for (int i = 0; i < contents.size; ++i)
//                items[i] = !save || contents.get(i).save
//                        ? contents.get(i).config
//                        : -1;
//            return items;
//        }

        /**
         * For example:
         * <blockquote><pre>
         *     {@code @Override}
         *     public void buildConfiguration({@code Table table}) {
         *         table.background(Tex.pane).left();
         *         layout.build(block, table, false);
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
                contents.each(content -> list.button(content.icon, Styles.clearNoneTogglei, iconSize, () -> {
                    frame.clearChildren();
                    frame.table(content.build(closeSelect));
                }).size(uiSize).group(listGroup).row());
                list.getChildren().get(0).fireClick();
            }).top();
            table.image().width(5).color(Pal.gray).growY().padRight(10);
            table.add(panel).size(uiSize * 8 + 10, uiSize * 6);
        }
    }

    abstract public static class Content {
        protected Layout layout;
        protected Drawable icon = Icon.star;

        abstract protected Cons<Table> build(boolean closeSelect);

        public Content setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }
    }

    abstract public static class ConfigurableContent extends Content {
        protected int config = -2;
        public boolean save;

        abstract protected void configure(int config);
        abstract protected int getConfig();
    }

    public static class Selection<Type> extends ConfigurableContent {
        public Seq<Type> items;
        public Func<Type, TextureRegion> buttonRegion;
        public Func<Type, String> buttonTip;
        public Prov<Type> holder;
        public Boolf2<Type, Type> comparer;
        public Func<Type, Integer> configure;

        public Selection(Seq<Type> items, Func<Type, TextureRegion> buttonRegion, Func<Type, String> buttonTip, Prov<Type> holder, Boolf2<Type, Type> comparer, Func<Type, Integer> configure) {
            this.items = items;
            this.buttonRegion = buttonRegion;
            this.buttonTip = buttonTip;
            this.holder = holder;
            this.comparer = comparer;
            this.configure = configure;
        }

        public Selection(Seq<Type> items, Func<Type, TextureRegion> buttonRegion, Func<Type, String> buttonTip, Prov<Type> holder, Func<Type, Integer> configure) {
            this(items, buttonRegion, buttonTip, holder, (a, b) -> a == b, configure);
        }

        @Override
        protected void configure(int config) {
            // config = Mathf.clamp(config, -1, items.size);
            this.config = config;
            layout.configure();
            this.config = -2;
        }

        @Override
        protected int getConfig() {
            if (config != -2) return config;
            Type item = holder.get();
            return item != null ? configure.get(item) : -1;
        }

        @Override
        protected Cons<Table> build(boolean closeSelect) {
            return table -> {
                table.top().left().defaults().size(uiSize);
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                group.setMinCheckCount(0);
                forEach(items, (idx, item) -> {
                    ImageButton button = table.button(new TextureRegionDrawable(buttonRegion.get(item)), Styles.clearNoneTogglei, iconSize, () -> {
                        if (closeSelect) control.input.config.hideConfig();
                    }).tooltip(buttonTip.get(item)).group(group).get();
                    button.changed(() -> configure(button.isChecked() ? configure.get(item) : -1));
                    button.update(() -> button.setChecked(comparer.get(holder.get(), item)));
                    if (idx % 8 == 7) table.row();
                });
            };
        }
    }

}
