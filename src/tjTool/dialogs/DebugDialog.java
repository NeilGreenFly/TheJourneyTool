package tjTool.dialogs;

import arc.Core;
import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import tjTool.core.TjVars;

import static arc.Core.settings;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class DebugDialog {

    public static BaseDialog dialog;

    public static Runnable clearResearch = () -> ui.showConfirm(
            "@confirm", "@settings.clearresearch.confirm", () -> {
                universe.clearLoadoutInfo();
                TechTree.all.each(TechTree.TechNode::reset);
                content.each(c -> {
                    if (c instanceof UnlockableContent u) u.clearUnlock();
                });
                settings.remove("unlocks");
            });

    public static void show() {
        if (dialog == null) init();
        rebuild();
        dialog.show();
    }

    private static void init() {
        dialog = new BaseDialog("Debug");
        dialog.addCloseButton();
        dialog.buttons.button("@quit", Icon.exit, Core.app::exit).size(210, 64);
    }

    private static void rebuild() {
        dialog.cont.clearChildren();
        dialog.cont.table(TjVars.frame, table -> {
            table.defaults().width(200);
            table.button("@database", Icon.book, ui.database::show).row();
            table.button("@research", Icon.tree, ui.research::show).row();
            table.button("@settings.clearresearch", Icon.trash, clearResearch).row();
            table.button("unlock", Icon.zoom, () -> content.each(c -> {
                if (c instanceof UnlockableContent u) u.unlock();
            })).row();
        }).margin(66).row();
    }

}
