package tjTool;

import tjTool.content.*;
import tjTool.dialogs.TjSettingTable;
import tjTool.widgets.TjDrawable;
import tjTool.world.graphics.MultiModel;

public final class Register {

    public static void loadContent() {
        MultiModel.load();
        TjBlocks.load();
        TjOverride.load();
    }

    public static void init() {
        TjDrawable.load();
        TjSettingTable.load();
    }

}
