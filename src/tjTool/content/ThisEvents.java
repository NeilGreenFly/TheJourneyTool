package tjTool.content;

import arc.Events;
import tjTool.core.*;

import static mindustry.game.EventType.*;

public class ThisEvents {

    public static void load() {

        Events.run(Trigger.update, TjDraw::update);

    }

}
