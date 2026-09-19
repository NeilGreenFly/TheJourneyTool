package tjTool.content;

import arc.Events;
import arc.struct.Seq;
import tjTool.world.blocks.sandbox.*;
import tjTool.core.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.game.EventType.Trigger.*;

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

    }

}
