package tjTool;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.content.*;

@SuppressWarnings("unused")
public final class ThisMain extends Mod {
    public Exception err = null;

    public ThisMain() {
        // Meow
        Events.on(ClientLoadEvent.class, e -> {
            if (err != null) {
                BaseDialog dialog = new BaseDialog("ERROR");
                dialog.cont.label(err::toString).row();
                dialog.cont.button("Sure", dialog::hide).size(100f, 50f);
                dialog.show();
            }
            // String version = mods.getMod(ThisMain.class).meta.minGameVersion;
            // int dot = version.indexOf('.');
            // if (isAtLeast(dot != -1 ? version.substring(0, dot + 1) + (Strings.parseInt(version.substring(dot + 1), 0) + 5) : version + ".5")) {
            //     BaseDialog dialog = new BaseDialog("[ W ]");
            //     dialog.cont.label(() -> bundle.format("mod.low", version, buildString())).row();
            //     dialog.addCloseButton();
            //     dialog.show();
            // }
        });
    }

    @Override
    public void loadContent() {
        try {
            ThisBlocks.load();
            ThisOverride.load();
            ThisEvents.load();
        } catch (Exception e) {
            Log.err(e);
            err = e;
        }
    }

}
