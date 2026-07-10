package tjTool;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.content.*;

import static mindustry.core.Version.build;

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
            if (build > 158) {
                BaseDialog dialog = new BaseDialog("[ W ]");
                dialog.cont.label(() -> "当前游戏版本过高, [sky]tj-tool[]模组可能将不再适配, 请避免在不确定是否可能损坏存档的情况下进入过去的存档.\n\n模组适配版本: [accent]157 ~ 158[]\n当前游戏版本: [red]" + build + "[]").row();
                dialog.addCloseButton();
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
