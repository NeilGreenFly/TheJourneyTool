package tjTool;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import tjTool.content.ThisBlocks;

public class ThisMain extends Mod{

    public ThisMain(){
        // Meow
    }

    @Override
    public void loadContent(){
        ThisBlocks.load();
    }

}
