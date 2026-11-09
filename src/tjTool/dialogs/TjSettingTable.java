package tjTool.dialogs;

import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;

import static arc.Core.settings;
import static mindustry.Vars.ui;
import static tjTool.TheJourney.theJourney;

/**
 * @see SettingsTable
 */
public class TjSettingTable {

    public static final Setting autoShowLoad = new Setting("auto-show-load");
    public static final Setting alwaysShowFPS = new Setting("always-show-fps");
    public static final Setting buttonDrop = new Setting("button-drop");

    public static void load() {
        ui.settings.addCategory("@" + theJourney.name + ".displayName", Icon.wrench, Setting::build);
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
