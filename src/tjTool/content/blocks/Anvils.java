package tjTool.content.blocks;

import mindustry.world.Block;
import tjTool.world.blocks.anvil.*;

import static mindustry.content.Items.surgeAlloy;
import static mindustry.type.Category.*;
import static tjTool.content.blocks.Blocks.staticInit;
import static tjTool.world.LazyGetter.apply;
import static tjTool.world.consumers.MultiStack.with;

public class Anvils {

    public static Block anvil, anvilEdge, anvilAmplifier;

    public static void load() {

        anvil = apply(new Anvil("anvil"), it -> {
            staticInit(it);
            it.requirements(effect, with(surgeAlloy, 300));
            it.health = 8000;
        });

        anvilEdge = apply(new AnvilEdge("anvil-edge"), it -> {
            staticInit(it);
            it.requirements(effect, with(surgeAlloy, 300));
            it.health = 4000;
        });

        anvilAmplifier = apply(new AnvilAmplifier("anvil-amplifier"), it -> {
            staticInit(it);
            it.requirements(effect, with(surgeAlloy, 300));
            it.health = 4000;
        });

    }

}
