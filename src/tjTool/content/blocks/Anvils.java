package tjTool.content.blocks;

import mindustry.world.Block;
import tjTool.world.blocks.anvil.*;

import static mindustry.content.Items.surgeAlloy;
import static mindustry.type.Category.*;
import static tjTool.content.blocks.Blocks.staticInit;
import static tjTool.world.consumers.MultiStack.with;

public class Anvils {

    public static Block anvil, anvilEdge, anvilAmplifier;

    public static void load() {

        anvil = new Anvil("anvil") {{
            staticInit(this);
            requirements(effect, with(surgeAlloy, 300));
        }};

        anvilEdge = new AnvilEdge("anvil-edge") {{
            staticInit(this);
            requirements(effect, with(surgeAlloy, 300));
        }};

        anvilAmplifier = new AnvilAmplifier("anvil-amplifier") {{
            staticInit(this);
            requirements(effect, with(surgeAlloy, 300));
        }};

    }

}
