package tjTool;

import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.graphics.MultiPacker;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.content.*;

import static mindustry.Vars.*;
import static tjTool.core.TjFunc.sprites;

@SuppressWarnings("unused")
public final class TheJourney extends Mod {
    public static Mods.LoadedMod theJourney;
    public Seq<Exception> err = new Seq<>();

    public TheJourney() {
        Events.on(ClientLoadEvent.class, event -> {
            if (!err.any()) return;
            BaseDialog dialog = new BaseDialog("ERROR");
            err.each(e -> dialog.cont.label(e::toString).row());
            dialog.cont.button("Sure", dialog::hide).size(100f, 50f);
            dialog.show();
        });
    }

    @Override
    public void loadContent() {
        theJourney = mods.getMod(this.getClass());
        try {
            TjBlocks.load();
            TjOverride.load();
            TjEvents.load();
        } catch (Exception e) {
            Log.err(e);
            err.add(e);
        }
    }

    @Override
    public void packSprites(MultiPacker packer) {
        sprites(packer);
    }

}
