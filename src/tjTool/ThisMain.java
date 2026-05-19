package tjTool;

import mindustry.mod.*;
import tjTool.content.*;

@SuppressWarnings("unused")
public class ThisMain extends Mod {

    public ThisMain() {
        // Meow
    }

    @Override
    public void loadContent() {
        ThisBlocks.load();
        ThisOverride.load();
    }

}
