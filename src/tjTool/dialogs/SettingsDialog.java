package tjTool.dialogs;

import arc.Core;
import arc.flabel.FLabel;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.world.graphics.TjColor;
import tjTool.world.graphics.TjDraw;

import static tjTool.world.LazyGetter.*;

@SuppressWarnings("unused")
public class SettingsDialog {

    public static BaseDialog dialog;

    private static KeySeq[] keySeq;
    private static Seq<ModSetting> modSeq;
    private static ModSetting currMod;
    private static Seq<DefSetting> defaultSeq;

    public static void show() {
        if (dialog == null) init();
        rebuild();
        dialog.show();
    }

    private static void init() {
        dialog = new BaseDialog("Settings\n这里非常危险, 在没有把握的情况下请立刻离开这里 !");
        dialog.addCloseButton();
        dialog.buttons.button("@quit", Icon.exit, Core.app::exit).size(210, 64);
        dialog.cont.top().left();

        keySeq = new KeySeq[]{
                new KeySeq("mod", 10, key -> key.startsWith("mod-")),
                new KeySeq("save", 100, key -> key.startsWith("save-")),
                new KeySeq("req", 1000, key -> key.startsWith("req-")),
                new KeySeq("unlocked", 1000, key -> key.endsWith("-unlocked")),
                new KeySeq("hiscore", 1000, key -> key.startsWith("hiscore")),
                new KeySeq("default", 1000, ignore -> true)};

        Core.settings.keys().forEach(key -> {
            for (var keys : keySeq) if (keys.b.get(key)) {
                keys.keys.add(key); break;
            }
        });
        for (var keys : keySeq) keys.keys.sort();

        modSeq = apply(new Seq<String>(), seq -> keySeq[0].keys.each(mod ->
                seq.addUnique(mod.substring(mod.indexOf('-') + 1, mod.lastIndexOf('-'))))).map(name ->
                apply(new ModSetting("mod-" + name + "-enabled"), mod -> {
                    mod.searchName = mod.displayName = name;
                    if (name.equals("mindustryx")) mod.displayName = TjDraw.gradient(name, Color.orange, Pal.accent);
                    else if (name.equals("kotlin")) mod.displayName = TjDraw.gradient(name, TjColor.kotlin, Color.violet);
                }));
        keySeq[0].keys.each(v -> {
            for (var mod : modSeq) if (v.startsWith("mod-" + mod.searchName + "-")) {
                mod.mod.add(new DefSetting(v)); break;
            }
        });
        keySeq[2].keys.each(v -> {
            for (var mod : modSeq) if (v.startsWith("req-" + mod.searchName + "-")) {
                mod.req.add(new DefSetting(v)); break;
            }
        });
        keySeq[3].keys.each(v -> {
            for (var mod : modSeq) if (v.startsWith(mod.searchName + "-")) {
                mod.unlocked.add(new DefSetting(v)); break;
            }
        });
        defaultSeq = new Seq<>();
        keySeq[5].keys.each(v -> defaultSeq.add(new DefSetting(v)));
    }

    private static void rebuild() {
        dialog.cont.clearChildren();
        // other();
        setup();
    }

    private static void other() {
        dialog.cont.table(table -> {
            var to = new FLabel("Click the button to refresh ->");
            var from = new TextArea(TjDraw.gradient("NeilGreenFly", Color.cyan, Color.purple));
            // var from = new TextArea("{rainbow}TEXT");
            table.add(to).growX().row();
            table.add(from).height(100).growX().row();
            table.button(Icon.upOpen, Styles.clearNonei, 32, () -> {
                to.restart(from.getText());
                to.skipToTheEnd();
            }).growX().row();
        }).top().width(500);
    }

    private static void setup() {

        if (currMod == null) add(dialog.cont, defaultSeq, v -> {});
        else dialog.cont.button(Icon.undo, Styles.clearNonei, () -> {
            currMod = null;
            rebuild();
        }).top();
        add(dialog.cont, modSeq, v -> {
            currMod = v;
            rebuild();
        }, true);
        if (currMod == null) return;
        dialog.cont.add(apply(new ScrollPane(apply(new Table().top().left(), t -> {
            if (currMod.mod.any()) add(t, currMod.mod, v -> {});
            if (currMod.req.any()) add(t, currMod.req, v -> {});
            if (currMod.unlocked.any()) add(t, currMod.unlocked, v -> {});

            t.table(table -> {
                if (currMod.req.any()) table.button("移除全部 req 值为 0 的条目", () -> {
                    currMod.req.each(setting -> {
                        var v = Core.settings.get(setting.name, null);
                        if (v instanceof Integer o && o == 0) Core.settings.remove(setting.name);
                    });
                    currMod.req = currMod.req.select(setting -> Core.settings.get(setting.name, null) != null);
                    rebuild();
                }).width(400).row();
                if (currMod.unlocked.any()) table.button("移除全部 unlocked 条目", () -> {
                    currMod.unlocked.each(setting -> Core.settings.remove(setting.name));
                    currMod.unlocked.clear();
                    rebuild();
                }).width(400).row();
            }).top().padLeft(20);
        }), Styles.smallPane), panel -> {
            panel.setScrollingDisabled(false, true);
            panel.setOverscroll(false, false);
            panel.exited(() -> clearScrollFocus(panel));
        })).grow();
    }

    public static class KeySeq {
        public Seq<String> keys;
        public String name;
        public Boolf<String> b;

        public KeySeq(String name, int capacity, Boolf<String> b) {
            this.keys = new Seq<>(capacity);
            this.name = name;
            this.b = b;
        }
    }

    public static abstract class Setting {
        public String name;
        public String searchName;
        public String displayName;

        public Setting(String name) {
            this.name = this.searchName = this.displayName = name;
        }

        public abstract String tooltip();
    }

    public static class ModSetting extends Setting {
        public Seq<DefSetting> mod = new Seq<>();
        public Seq<DefSetting> req = new Seq<>(1000);
        public Seq<DefSetting> unlocked = new Seq<>(100);

        public ModSetting(String name) {
            super(name);
        }

        @Override
        public String tooltip() {
            var v = Core.settings.get(name, null);
            return v instanceof Boolean o ? (o ? "@mod.enabled" : "@mod.disabled") : String.valueOf(v);
        }
    }

    public static class DefSetting extends Setting {
        public DefSetting(String name) {
            super(name);
        }

        @Override
        public String tooltip() {
            return "[accent]" + Core.settings.get(name, "null...");
        }
    }

    public static <T extends Setting> Cell<Table> add(Table table, Seq<T> seq, Cons<T> clicked) {
        return add(table, seq, clicked, false);
    }

    public static <T extends Setting> Cell<Table> add(Table table, Seq<T> seq, Cons<T> clicked, boolean readonly) {
        Table content = new Table().margin(10).left();
        int[] i = {0};

        return table.table(root -> {
            root.table(zoom -> {
                zoom.image(Icon.zoom).padLeft(5).padRight(5);
                TextField[] search = new TextField[1];
                search[0] = zoom.field(null, ignore -> {
                    content.clearChildren();
                    var text = search[0].getText();
                    i[0] = 0;
                    seq.each(setting -> {
                        int idx = -1;
                        if (!text.isEmpty() && (idx = setting.searchName.toLowerCase().indexOf(text.toLowerCase())) == -1) return;
                        content.add(String.valueOf(i[0]++)).right().padRight(20).get().setAlignment(Align.right);
                        content.stack(
                                apply(new Button(Styles.cleari), v -> v.clicked(() -> clicked.get(setting))),
                                apply(new Label(idx == -1
                                        ? setting.displayName + " "
                                        : "[gray]" + setting.searchName.substring(0, idx) + "[accent]"
                                          + setting.searchName.substring(idx, idx + text.length()) + "[]"
                                          + setting.searchName.substring(idx + text.length())
                                ), v -> v.touchable = Touchable.disabled)
                        ).growX().tooltip(setting.tooltip(), true);
                        var v = Core.settings.get(setting.name, null);
                        if (v instanceof Boolean o) content.add(o.toString()).right().padLeft(20).color(validColor(o));
                        else if (v instanceof Integer o) content.add(o.toString()).right().padLeft(20).color(Color.purple);
                        else if (v instanceof Float o) content.add(o.toString()).right().padLeft(20).color(Color.pink);
                        else content.add();
                        if (!readonly) content.button(Icon.cancel, Styles.clearNonei, () -> {
                            Core.settings.remove(setting.name);
                            rebuild();
                        }).color(Pal.remove).padLeft(20).get().getStyle().imageUpColor = Pal.remove;
                        content.row();
                    });
                }).padBottom(4).left().growX().get();
                search[0].setMessageText("@players.search");
                search[0].change();
                zoom.label(() -> i[0] + " / " + seq.size).color(Color.gray);
            }).fillX().row();
            root.add(apply(new ScrollPane(content, Styles.smallPane), panel -> {
                panel.setScrollingDisabled(true, false);
                panel.setOverscroll(false, false);
                panel.exited(() -> clearScrollFocus(panel));
            })).grow().row();
        }).top();
    }

}
