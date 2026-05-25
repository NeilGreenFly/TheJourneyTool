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
