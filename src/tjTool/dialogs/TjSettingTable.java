package tjTool.dialogs;

import arc.flabel.FLabel;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.style.Drawable;
import arc.scene.ui.TextArea;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import tjTool.core.TjStat;
import tjTool.world.Range;

import static arc.Core.settings;
import static mindustry.Vars.ui;
import static tjTool.TheJourney.theJourney;
import static tjTool.world.LazyGetter.apply;

/**
 * @see SettingsTable
 * @see SettingsMenuDialog#addCategory(String, Drawable, Cons)
 */
public class TjSettingTable {

    public static final Setting autoShowLoad = new Setting("auto-show-load");
    public static final Setting alwaysShowFPS = new Setting("always-show-fps");
    public static final Setting buttonDrop = new Setting("button-drop");

    public static void load() {
        // 原本是可以直接调用 addCategory(...) 的, 但是我需要在后面追加一些不属于设置的部分,
        // 原本直接追加也是可以的因为 rebuild() 不再调用, 但是在 X 端这样是不行的, 我不得不重写 rebuild() 并手动调用一次, 原因不明.
        // 尽管这些也本不应该出现在设置中, 不过现阶段不做考虑.
        ui.settings.getCategories().add(apply(new SettingsCategory("@" + theJourney.name + ".displayName", Icon.wrench, Setting::build) {{ builder.get(table = new SettingsTable() {
            @Override
            public void rebuild() {
                super.rebuild();
                row().table(t -> {
                    t.button("TJ", Styles.cleart, TjStat.updateDialog::show).right().row();
                    float h = 40;
                    var to = new FLabel("Click the button to refresh ->");
                    var from = new TextArea("{rainbow}DEFAULT-TEXT{/rainbow} ");
                    to.setWrap(true);
                    t.table(Styles.black6, title -> title.left().labelWrap("FLabel Debug").color(Color.darkGray)).margin(5).growX().row();
                    t.add(to).growX().padTop(5).row();
                    t.image().color(Color.darkGray).height(5).growX().padTop(5).row();
                    t.add(from).height(200).growX().row();
                    t.table(buttons -> {
                        var skip = apply(buttons.button("skip", Styles.clearTogglet, () -> {}).size(h).get(), v -> v.setChecked(true));
                        var restart = buttons.button(Icon.upOpen, Styles.clearNonei, h, () -> {
                            to.restart(from.getText());
                            if (skip.isChecked()) to.skipToTheEnd();
                        }).growX().get();
                        var end = apply(buttons.button("/t", Styles.clearTogglet, () -> {}).tooltip("自动追加结束 /token").size(h).get(), v -> v.setChecked(true));
                        String[] effects = new String[]{"ease", "jump", "shake", "sick", "wave", "wind", "rainbow", "gradient", "fade", "blink"};
                        buttons.row().table(q -> Range.forEach(effects, (i, token) -> {
                            q.button(token, Styles.cleart, () -> {
                                from.setText(from.getText() + "{" + token + "}" + (end.isChecked() ? " T {/" + token + "} " : ""));
                                restart.fireClick();
                            }).margin(5).growX().tooltip(tooltip -> tooltip.background(Tex.paneLeft)
                                    .add(new FLabel("{" + token + "}" + token + "-" + token)));
                            if (i % 2 == 1) q.row();
                        })).colspan(2).growX().row();
                    }).growX().row();
                }).top().growX().padTop(50);
            }
        }); }}, v -> v.table.rebuild()));
    }

    public static class Setting {
        public static final Seq<Setting> booleanSettings = new Seq<>();

        private final String name;
        private final boolean def;
        private boolean staticValue;

        public Setting(String name) {
            this(name, false);
        }

        public Setting(String name, boolean def) {
            this.name = name;
            this.def = def;
            booleanSettings.add(this);
        }

        public String key() {
            return theJourney.name + "-" + name;
        }

        public boolean getBool() {
            return staticValue;
        }

        public static void build(SettingsTable table) {
            booleanSettings.each(v -> {
                table.checkPref(v.key(), v.def, b -> v.staticValue = b);
                v.staticValue = settings.getBool(v.key());
            });
        }
    }

}
