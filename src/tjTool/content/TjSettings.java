package tjTool.content;

import arc.Events;
import mindustry.game.EventType;

import static arc.Core.settings;

public final class TjSettings {

    public static StaticSetting arcChoiceUiIcon = new StaticSetting("block.arcChoiceUiIcon");

    public static void load() {
        Events.on(EventType.BuildSelectEvent.class, event -> arcChoiceUiIcon.update());
        arcChoiceUiIcon.update();
    }

    public static class StaticSetting {
        private final String name;
        private boolean staticValue;

        public StaticSetting(String name) {
            this.name = name;
        }

        public boolean getBool() {
            return staticValue;
        }

        public void update() {
            staticValue = settings.getBool(name);
        }
    }

}
