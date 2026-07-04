package tjTool.content;

import arc.Events;
import tjTool.content.blocks.sandbox.*;
import tjTool.core.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.game.EventType.*;

public class ThisEvents {

    public static void load() {

        Events.run(Trigger.update, () -> {
            TjDraw.update();
            SandboxBlock.input = control.input.block == null && !scene.hasMouse()
                    ? world.buildWorld(input.mouseWorld(control.input.getMouseX(), control.input.getMouseY()))
                    : null;
        });

    }

}
