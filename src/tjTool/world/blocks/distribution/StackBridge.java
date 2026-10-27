package tjTool.world.blocks.distribution;

import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.StackConveyor;

/**
 * 如你所见这个类是空的，由于我希望桥能够与塑钢带无缝衔接，这意味着我不得不让桥继承自塑钢带，于是乎我还得自己实现桥的逻辑，
 * 这不如重新写一个塑钢带来得划算... 不这么做完全是对兼容原版的妥协... 就这样吧，等我想写了再回来写.
 */
@SuppressWarnings("unused")
public class StackBridge extends StackConveyor {
    public StackBridge(String name) {
        super(name);
        unloadable = false;
        solid = true;
    }

    @SuppressWarnings("unused")
    public class BuildingType extends Building {
        @Override
        public void draw() { // 随便对外部类字段引用一下避免 IJ 警告
            Draw.rect(region, x, y);
        }
    }
}
