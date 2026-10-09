package tjTool.content;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import mindustry.ui.Fonts;
import tjTool.world.blocks.sandbox.*;
import tjTool.core.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.game.EventType.Trigger.*;
import static tjTool.core.TjVars.*;

public final class TjEvents {

    private static final Seq<Runnable> updates = new Seq<>();

    public static void update(Runnable run) {
        updates.add(run);
    }

    public static void load() {

        Events.run(update, () -> {
            TjDraw.update();
            SandboxBlock.input = control.input.block == null && !scene.hasMouse()
                    ? world.buildWorld(input.mouseWorld(control.input.getMouseX(), control.input.getMouseY()))
                    : null;
        });

        Events.run(beforeGameUpdate, () -> updates.each(Runnable::run));

        if (alwaysShowFPS) Events.run(uiDrawEnd, () -> {
            Fonts.outline.draw("FPS: [cyan]" + graphics.getFramesPerSecond(),
                    graphics.getWidth() / 4f,
                    graphics.getHeight() / 4f * 3);
            Draw.flush();
        });

    }

}
