package tjTool;

import arc.Core;
import arc.Events;
import mindustry.game.EventType.*;
import mindustry.graphics.MultiPacker;
import mindustry.mod.*;
import tjTool.content.*;
import tjTool.core.*;
import tjTool.world.graphics.*;

import static mindustry.Vars.*;
import static tjTool.core.TjFunc.*;

@SuppressWarnings("unused")
public final class TheJourney extends Mod {
    public static Mods.LoadedMod theJourney;

    public TheJourney() {
        Events.on(ClientLoadEvent.class, event -> {
            var setting = theJourney.name + "-auto-show-load";
            ui.settings.graphics.checkPref(setting, false);
            if (Core.settings.getBool(setting)) ui.load.show();
        });
    }

    @Override
    public void init() {
        theJourney.meta.displayName = TjDraw.gradient(Core.bundle.get(theJourney.name + ".displayName"), TjColor.n, TjColor.anvil);
        theJourney.meta.subtitle = Core.bundle.get(theJourney.name + ".subtitle");
        theJourney.meta.description = Core.bundle.get(theJourney.name + ".description");
        TjVars.load();
    }

    @Override
    public void loadContent() {
        theJourney = mods.getMod(this.getClass());
        MultiModel.load();
        TjBlocks.load();
        TjOverride.load();
        TjEvents.load();
    }

    @Override
    public void packSprites(MultiPacker packer) {
        sprites(packer);
    }

}
