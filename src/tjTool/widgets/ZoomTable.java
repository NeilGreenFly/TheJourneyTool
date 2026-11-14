package tjTool.widgets;

import arc.Core;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Nullable;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;

import static mindustry.Vars.ui;
import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyGetter.*;

/**
 * @author NeilGreenFly
 * @param <T> Entry
 * @see ItemSelection#buildTable(Block, Table, Seq, Prov, Cons, boolean, int, int)
 */
public class ZoomTable<T extends ZoomTable.Entry> extends Table {
    public static int maxEntry = 500;
    public static TheAllEntry theAllEntry = new TheAllEntry();

    public @Nullable Seq<T> seq;
    public Seq<T> currSeq = new Seq<>(maxEntry);
    public Table content = new Table(Tex.paneLeft).margin(10).left();
    public TextField search;
    public boolean readonly;
    private int page;
    private int count;
    private int lineCounter;
    private Boolp searchByValue;
    private Boolp allEquals;

    public ZoomTable(boolean readonly) {
        this(null, readonly);
    }

    public ZoomTable(Seq<T> entrySeq) {
        this(entrySeq, true);
    }

    public ZoomTable(Seq<T> entrySeq, boolean readonly) {
        this.readonly = readonly;
        seq = entrySeq;
        table(zoom -> {
            zoom.image(Icon.zoom).padLeft(5).padRight(5);
            search = apply(zoom.field(null, this::rebuild).padBottom(4).left().growX().get(), it -> it.setMessageText("@players.search"));
            zoom.label(() -> count + " / " + seqSize()).color(Color.gray).pad(5);
            zoom.button(Icon.cancel, Styles.clearNonei, iconSize, () -> {
                search.setText("");
                refresh();
            }).size(uiSize).tooltip("@clear");
            zoom.add(apply(new TextButton("K", Styles.clearTogglet), it -> {
                searchByValue = it::isChecked;
                it.clicked(() -> {
                    it.setText(!it.isChecked() ? "K" : "V");
                    refresh();
                });
            })).size(uiSize).tooltip("将过滤器应用于: 键(K) / 值(V)", true);
            zoom.add(apply(new TextButton("A", Styles.clearTogglet), it -> {
                allEquals = it::isChecked;
                it.clicked(this::refresh);
            })).size(uiSize).tooltip("完全匹配", true);
            zoom.button(Icon.refreshSmall, Styles.clearNonei, this::refresh).size(uiSize);
        }).growX().row();
        collapser(p -> {
            p.button(Icon.leftOpen, Styles.clearNonei, iconSize, () -> changePageBy(-5)).size(uiSize).tooltip("-5", true).get().setDisabled(() -> page <= 0);
            p.button(Icon.leftOpen, Styles.clearNonei, iconSize, () -> changePageBy(-1)).size(uiSize).tooltip("-1", true).get().setDisabled(() -> page <= 0);
            p.label(() -> (page + 1) + " / " + (seqSize() / maxEntry + 1)).color(Color.gray).growX().pad(5).get().setAlignment(Align.center);
            p.button(Icon.rightOpen, Styles.clearNonei, iconSize, () -> changePageBy(1)).size(uiSize).tooltip("+1", true).get().setDisabled(() -> page >= seqSize() / maxEntry);
            p.button(Icon.rightOpen, Styles.clearNonei, iconSize, () -> changePageBy(5)).size(uiSize).tooltip("+5", true).get().setDisabled(() -> page >= seqSize() / maxEntry);
        }, () -> hasSeq() && seq.size > maxEntry).left().row();
        add(apply(new ScrollPane(content, Styles.smallPane), panel -> {
            panel.setScrollingDisabled(true, false);
            panel.setOverscroll(false, false);
            panel.exited(() -> clearScrollFocus(panel));
        })).growX().maxHeight(Core.graphics.getHeight()).row();
        refresh();
    }

    private void rebuild(String text) {
        if (page > seqSize() / maxEntry) page = 0;
        currSeq.clear();
        count = lineCounter = 0;
        content.clearChildren();
        if (!readonly && hasSeq() && seq.any()) addLine(theAllEntry, -1, -1);
        if (hasSeq()) seq.each(entry -> {
            int line = lineCounter++;
            int idx = -1;
            if (line < maxEntry * page || maxEntry * (page + 1) <= line) return;
            if (!text.isEmpty()) {
                var str = !searchByValue.get() ? entry.searchName : String.valueOf(entry.getValue());
                if (allEquals.get()) {
                    if (!str.equals(text)) return;
                } else {
                    if ((idx = str.toLowerCase().indexOf(text.toLowerCase())) == -1) return;
                    if (searchByValue.get()) idx = -1;
                }
            }
            count += 1;
            currSeq.add(entry);
            addLine(entry, line, idx);
        });
    }

    private void addLine(Entry entry, int line, int idx) {
        content.add(String.valueOf(line)).color(Color.gray).right().padRight(20).get().setAlignment(Align.right);
        content.stack(
                apply(new Button(Styles.cleari), it -> it.clicked(entry::clicked)),
                apply(new Label(idx == -1
                        ? entry.displayName + " "
                        : "[gray]" + entry.searchName.substring(0, idx) + "[accent]"
                          + entry.searchName.substring(idx, idx + search.getText().length()) + "[]"
                          + entry.searchName.substring(idx + search.getText().length())
                ), it -> it.touchable = Touchable.disabled)
        ).growX().tooltip(entry.tooltip(), true);
        var value = entry.getValue();
        if (value instanceof Boolean o) addValue(content, o, validColor(o));
        else if (value instanceof Integer o) addValue(content, o, Color.purple);
        else if (value instanceof Float o) addValue(content, o, Color.pink);
        else content.add();
        if (!readonly) content.button(Icon.cancel, Styles.clearNonei, () -> ui.showConfirm(entry.searchName, "@settings.clear.confirm", () -> {
            if (!hasSeq()) return;
            entry.onRemoved(this);
            if (entry.skipRemove) return;
            seq.as().remove(entry, true);
            refresh();
        })).padLeft(20).get().getStyle().imageUpColor = Pal.remove;
        content.row();
    }

    private void addValue(Table table, Object any, Color color) {
        table.add(any.toString()).right().padLeft(20).color(color);
    }

    public void refresh() {
        if (search != null) search.change();
    }

    public void changePageBy(int n) {
        page = Mathf.clamp(page + n, 0, seqSize() / maxEntry);
        refresh();
    }

    public boolean hasSeq() {
        return seq != null;
    }

    public int seqSize() {
        return hasSeq() ? seq.size : 0;
    }

    public static class Entry {
        public String searchName;
        public String displayName;
        public boolean skipRemove;

        public Entry(String name) {
            searchName = displayName = name;
        }

        public Object getValue() {
            return null;
        }

        public String tooltip() {
            return displayName;
        }

        /** Called when this entry is clicked. */
        public void clicked() {}

        /** Called when this entry is removed. */
        public <T extends Entry> void onRemoved(ZoomTable<T> table) {}
    }

    public static final class TheAllEntry extends Entry {
        public TheAllEntry() {
            super("< ALL >");
            skipRemove = true;
        }

        @Override
        public String tooltip() {
            return "Remove all current";
        }

        @Override
        public <T extends Entry> void onRemoved(ZoomTable<T> table) {
            if (table.hasSeq()) table.currSeq.each(it -> {
                it.onRemoved(table);
                if (!it.skipRemove)
                    table.seq.remove(it);
            });
            table.refresh();
        }
    }
}
