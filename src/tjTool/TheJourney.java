package tjTool;

import arc.Core;
import mindustry.graphics.MultiPacker;
import mindustry.mod.*;
import tjTool.content.TjEvents;
import tjTool.world.graphics.*;

import static mindustry.Vars.*;
import static tjTool.core.TjFunc.*;

@SuppressWarnings("unused")
public final class TheJourney extends Mod {
    public static Mods.LoadedMod theJourney;

    public TheJourney() {
        TjEvents.load();
    }

    @Override
    public void init() {
        theJourney.meta.displayName = TjDraw.gradient(Core.bundle.get(theJourney.name + ".displayName"), TjColor.n, TjColor.anvil);
        theJourney.meta.subtitle = Core.bundle.get(theJourney.name + ".subtitle");
        theJourney.meta.description = Core.bundle.get(theJourney.name + ".description");
        Register.init();
    }

    @Override
    public void loadContent() {
        theJourney = mods.getMod(this.getClass());
        Register.loadContent();
    }

    @Override
    public void packSprites(MultiPacker packer) {
        sprites(packer);
    }

}
