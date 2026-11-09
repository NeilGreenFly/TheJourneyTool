package tjTool.content;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import mindustry.ui.Fonts;
import tjTool.dialogs.TjSettingTable;
import tjTool.world.blocks.sandbox.*;
import tjTool.world.graphics.TjDraw;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.game.EventType.*;
import static mindustry.game.EventType.Trigger.*;
import static tjTool.dialogs.TjSettingTable.alwaysShowFPS;

public final class TjEvents {

    private static final Seq<Runnable> updates = new Seq<>();

    public static void update(Runnable run) {
        updates.add(run);
    }

    public static void load() {

        Events.on(ClientLoadEvent.class, event -> {
            if (TjSettingTable.autoShowLoad.getBool()) ui.load.show();
        });

        Events.run(update, () -> {
            TjDraw.update();
            SandboxBlock.input = control.input.block == null && !scene.hasMouse()
                    ? world.buildWorld(input.mouseWorld(control.input.getMouseX(), control.input.getMouseY()))
                    : null;
        });

        Events.run(beforeGameUpdate, () -> updates.each(Runnable::run));

        Events.run(uiDrawEnd, () -> {
            if (!alwaysShowFPS.getBool() || !ui.hudfrag.shown) return;
            Fonts.outline.draw("FPS: [cyan]" + graphics.getFramesPerSecond(),
                    graphics.getWidth() / 4f,
                    graphics.getHeight() / 4f * 3);
            Draw.flush();
        });

    }

}
