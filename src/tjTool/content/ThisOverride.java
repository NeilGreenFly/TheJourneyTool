package tjTool.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.entities.part.DrawPart;
import tjTool.core.*;

import java.util.Objects;

import static mindustry.content.UnitTypes.emanate;
import static mindustry.entities.part.DrawPart.PartProgress.warmup;

public class ThisOverride {

    public static void load() {

        // Anuken 把 logicDisplayTile 改成 tileLogicDisplay 了导致没办法直接用, 如果直接用会导致近几个版本不兼容.
        // 所幸资源名称没改, 我真的会谢...
        // 版本跨度: v154.3 - v156
        for (var block : mindustry.Vars.content.blocks())
            if (Objects.equals(block.name, "tile-logic-display")) {
                block.allowRectanglePlacement = true;
                break;
            }

        StatusEffects.none.color = Color.green;

        final float haloY = -16f;
        emanate.parts.add(new DrawPart[]{
//                new TjPart.TjHaloPart() {{
//                    color = Color.valueOf("#FF7FFF");
//                    colorTo = Color.valueOf("#FFBFFF");
//                    tri = false;
//                    hollow = true;
//                    moveY = 16;
//                    sides = 3;
//                    shapes = 2;
//                    radius = 32;
//                    radiusTo = 8;
//                    stroke = 0;
//                    strokeTo = 2;
//                    haloRadius = 0;
//                    rotateSpeed = 4;
//                }},
                new TjPart.TjShapePart() {{
                    circle = true;
                    hollow = true;
                    y = haloY;
                    radius = 10;
                    stroke = 0;
                    strokeTo = 2;
                }},
                new TjPart.TjShapePart() {{
                    circle = true;
                    hollow = true;
                    y = haloY;
                    radius = 4;
                    stroke = 0;
                    strokeTo = 1.5f;
                }},
                new TjPart.TjHaloPart() {{
                    progress = warmup;
                    tri = true;
                    y = haloY;
                    shapes = 4;
                    radius = 0;
                    radiusTo = 8;
                    triLength = 0;
                    triLengthTo = 4;
                    haloRadius = 10;
                    haloRotateSpeed = 1;
                }},
                new TjPart.TjHaloPart() {{
                    progress = warmup;
                    tri = true;
                    y = haloY;
                    shapes = 4;
                    radius = 8;
                    triLength = 0;
                    triLengthTo = 6;
                    haloRadius = 16;
                    haloRotateSpeed = -1;
                }},
                new TjPart.TjHaloPart() {{
                    progress = warmup;
                    tri = true;
                    y = haloY;
                    shapes = 4;
                    radius = 8;
                    triLength = 0;
                    triLengthTo = 2;
                    haloRadius = 16;
                    shapeRotation = 180;
                    haloRotateSpeed = -1;
                }},
                new TjPart.TjHaloPart() {{
                    progress = warmup;
                    tri = true;
                    y = haloY;
                    shapes = 2;
                    radius = 4;
                    triLength = 0;
                    triLengthTo = 20;
                    haloRadius = 16;
                    haloRotation = 90;
                }},
                new TjPart.TjHaloPart() {{
                    progress = warmup;
                    tri = true;
                    y = haloY;
                    shapes = 2;
                    radius = 4;
                    triLength = 0;
                    triLengthTo = 5;
                    haloRadius = 16;
                    haloRotation = 90;
                    shapeRotation = 180;
                }}
        });

    }

}
