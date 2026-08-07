package tjTool.content;

import arc.graphics.Color;
import mindustry.entities.Effect;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

import static tjTool.core.TjEffect.*;

/** Stay tuned */
@SuppressWarnings("unused")
public class ThisStatus {

    public static StatusEffect dissociation;

    public static void load() {

        dissociation = new StatusEffect("dissociation") {{
            color = Color.black;
            damage = 10 / 60f;
            speedMultiplier = 1.1f;
            buildSpeedMultiplier = 0.5f;
            parentizeEffect = true;
            effect = new Effect(0, e -> {
                if (!(e.data instanceof Unit unit)) return;
                callDissociation.at(e.x, e.y, unit.hitSize / 8, unit.team.color, unit.vel.cpy());
            }).followParent(false);
        }};

    }

}
