package tjTool.dialogs;

import arc.Core;
import arc.func.Boolf;
import arc.func.Boolf2;
import arc.func.Func;
import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.widgets.ZoomTable;
import tjTool.world.graphics.TjColor;
import tjTool.world.graphics.TjDraw;

import static mindustry.Vars.ui;
import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyGetter.*;

@SuppressWarnings("unused")
public class SettingsViewerDialog {

    public static BaseDialog dialog;
    public static boolean showMods = true;

    private static Seq<ModEntry> modSeq;
    private static Seq<SettingEntry> saveSeq;
    private static Seq<SettingEntry> hiscoreSeq;
    private static Seq<SettingEntry> usidSeq;
    private static Seq<SettingEntry> defaultSeq;

    public static void show() {
        if (dialog == null) {
            init();
            build();
        }
        dialog.show();
    }

    private static void init() {
        dialog = new BaseDialog("Settings\n这里非常危险, 在没有把握的情况下请立刻离开这里 !");
        dialog.addCloseButton();
        dialog.buttons.button("@quit", Icon.exit, Core.app::exit).size(210, 64);

        var preMod = new KeySeq("mod", 10, key -> key.startsWith("mod-"));
        var preReq = new KeySeq("req", 1000, key -> key.startsWith("req-"));
        var preUnlocked = new KeySeq("unlocked", 1000, key -> key.endsWith("-unlocked"));

        var preSave = new KeySeq("save", 100, key -> key.startsWith("save-"));
        var preHiscore = new KeySeq("hiscore", 1000, key -> key.startsWith("hiscore"));
        var preUsid = new KeySeq("usid", 1000, key -> key.startsWith("usid-"));

        var preDef = new KeySeq("default", 1000, ignore -> true);

        Core.settings.keys().forEach(key -> {
            var target = KeySeq.all.find(keys -> keys.b.get(key));
            if (target != null) target.keys.add(new KeyPreEntry(key));
        });
        KeySeq.all.each(keys -> keys.keys.sortComparing(it -> it.key));

        modSeq = apply(new Seq<String>(), seq -> preMod.keys.each(pre ->
                seq.addUnique(pre.key.substring(pre.key.indexOf('-') + 1, pre.key.lastIndexOf('-'))))).map(ModEntry::new);

        select(preMod, mod -> mod.mod, (mod, key) -> key.startsWith("mod-" + mod + "-"));
        select(preReq, mod -> mod.req, (mod, key) -> key.startsWith("req-" + mod + "-"));
        select(preUnlocked, mod -> mod.unlocked, (mod, key) -> key.startsWith(mod + "-"));

        saveSeq = preSave.keys.map(SettingEntry::new);
        hiscoreSeq = preHiscore.keys.map(SettingEntry::new);
        usidSeq = preUsid.keys.map(SettingEntry::new);

        defaultSeq = preDef.keys.map(SettingEntry::new);

        modSeq.add(apply(new ModEntry("< null >"), it -> {
            preReq.keys.each(pre -> !pre.ign, pre -> it.req.add(new OtherEntry(pre)));
            preUnlocked.keys.each(pre -> !pre.ign, pre -> it.unlocked.add(new OtherEntry(pre)));
        }));
    }

    private static void select(KeySeq from, Func<ModEntry, Seq<SettingEntry>> to, Boolf2<String, String> b) {
        from.keys.each(pre -> {
            var it = modSeq.find(mod -> b.get(mod.searchName, pre.key));
            if (it != null) {
                to.get(it).add(new SettingEntry(pre));
                pre.ign = true;
            }
        });
    }

    private static void build() {
        dialog.cont.clearChildren();
        dialog.cont.top().left().button(Icon.undo, Styles.clearNonei, iconSize, () -> showMods = !showMods).top().left();
        build(dialog.cont.table().get().top().left());
    }

    private static void build(Table content) {
        content.defaults().top().left();
        content.collapser(table -> {
            table.defaults().top().left();
            table.add(new ZoomTable<>(modSeq));
            table.image().color(Color.darkGray).width(5).growY().pad(5);
            table.add(apply(new ScrollPane(apply(new Table().top().left(), panel -> {
                panel.defaults().top().left();
                panel.collapser(ModEntry.modTable, () -> ModEntry.modTable.hasSeq() && ModEntry.modTable.seq.any());
                panel.collapser(ModEntry.reqTable, () -> ModEntry.reqTable.hasSeq() && ModEntry.reqTable.seq.any());
                panel.collapser(ModEntry.unlockedTable, () -> ModEntry.unlockedTable.hasSeq() && ModEntry.unlockedTable.seq.any());
            }), Styles.smallPane), it -> {
                it.setScrollingDisabled(false, true);
                it.setOverscroll(false, false);
                it.exited(() -> clearScrollFocus(it));
            })).maxWidth(Core.graphics.getWidth()).grow();
        }, () -> showMods).row();
        content.collapser(table -> {
            table.defaults().top().left();
            table.add(apply(new ScrollPane(apply(new Table().top().left(), panel -> {
                panel.defaults().top().left();
                panel.add(new ZoomTable<>(defaultSeq, false));
                panel.add(new ZoomTable<>(saveSeq, false));
                panel.add(new ZoomTable<>(hiscoreSeq, false));
                panel.add(new ZoomTable<>(usidSeq, false));
            }), Styles.smallPane), it -> {
                it.setScrollingDisabled(false, true);
                it.setOverscroll(false, false);
                it.exited(() -> clearScrollFocus(it));
            })).maxWidth(Core.graphics.getWidth()).grow();
        }, () -> !showMods).row();
    }

    private static String enabled(boolean b) {
        return b ? "@mod.enabled" : "@mod.disabled";
    }

    public static class KeyPreEntry {
        String key;
        boolean ign;

        public KeyPreEntry(String key) {
            this.key = key;
        }
    }

    public static class KeySeq {
        public static Seq<KeySeq> all = new Seq<>();

        public Seq<KeyPreEntry> keys;
        public String name;
        public Boolf<String> b;

        public KeySeq(String name, int capacity, Boolf<String> b) {
            this.keys = new Seq<>(capacity);
            this.name = name;
            this.b = b;
            all.add(this);
        }
    }

    public static class SettingEntry extends ZoomTable.Entry {
        public String key;

        public SettingEntry(KeyPreEntry pre) {
            this(pre.key);
        }

        public SettingEntry(String name) {
            super(name);
            key = name;
        }

        @Override
        public Object getValue() {
            return Core.settings.get(key, null);
        }

        @Override
        public String tooltip() {
            return "[accent]" + getValue();
        }

        @Override
        public <T extends ZoomTable.Entry> void onRemoved(ZoomTable<T> table) {
            Core.settings.remove(key);
        }
    }

    public static class OtherEntry extends SettingEntry {
        public OtherEntry(KeyPreEntry pre) {
            super(pre);
        }

        public OtherEntry(String name) {
            super(name);
        }

        @Override
        public void clicked() {
            ui.showConfirm("创建新的模组条目 (需要重启)", "模组条目的识别依赖于以\"mod-\"起始的配置，但是由于部分相关配置不会在加载模组后自动注册，因此部分模组即使加载过也不能被识别到，所幸它们的相关配置仍然保留于此，您可以手动注册模组启用配置，在重启后即可查看此模组条目的相关配置了。\n当然您也可以放弃注册，通过搜索过滤查询该模组的相关配置。\n放心，注册名称填写错误后您可以随时回来移除它们...但是请确保名称是合法的，故意使用一些可能导致异常的特殊字符我们概不负责。\n\n仅需填入模组名称，我们会帮您补全\"mod-[accent]name[]-enabled\"\n[red]自动填充的名称可能并不完整，如有缺失请自行补全。[]", () -> {
                String def = "";
                if (key.startsWith("req-")) {
                    def = key.substring(4);
                    def = def.substring(0, def.indexOf("-"));
                } else if (key.endsWith("-unlocked")) {
                    def = key.substring(0, key.indexOf("-"));
                }
                ui.showTextInput(key, "Mod Name: ", 50, def, it -> Core.settings.put("mod-" + it + "-enabled", true));
            });
        }
    }

    public static class ModEntry extends SettingEntry {
        public static ModEntry currMod;
        public static ZoomTable<SettingEntry> modTable = new ZoomTable<>(false);
        public static ZoomTable<SettingEntry> reqTable = new ZoomTable<>(false);
        public static ZoomTable<SettingEntry> unlockedTable = new ZoomTable<>(false);

        public Seq<SettingEntry> mod = new Seq<>();
        public Seq<SettingEntry> req = new Seq<>(1000);
        public Seq<SettingEntry> unlocked = new Seq<>(100);

        public ModEntry(String name) {
            super(name);
            key = "mod-" + name + "-enabled";
            if (name.equals("mindustryx")) displayName = TjDraw.gradient(name, Color.orange, Pal.accent);
            else if (name.equals("kotlin")) displayName = TjDraw.gradient(name, TjColor.kotlin, Color.violet);
        }

        @Override
        public String tooltip() {
            var v = getValue();
            return v instanceof Boolean o ? enabled(o) : String.valueOf(v);
        }

        @Override
        public void clicked() {
            if ((currMod = currMod != this ? this : null) != null) {
                modTable.seq = currMod.mod;
                reqTable.seq = currMod.req;
                unlockedTable.seq = currMod.unlocked;
                modTable.refresh();
                reqTable.refresh();
                unlockedTable.refresh();
            } else {
                modTable.seq = null;
                reqTable.seq = null;
                unlockedTable.seq = null;
            }
        }
    }

}
