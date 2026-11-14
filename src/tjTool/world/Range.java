package tjTool.world;

import arc.func.Intc;
import arc.func.Intc2;
import arc.struct.Seq;
import tjTool.func.ForCons;

@SuppressWarnings("unused")
public final class Range {

    public static void forRange(int to, Intc cons) {
        forRange(0, to, cons);
    }

    public static void forRange(int from, int to, Intc cons) {
        forRange(from, to, 1, cons);
    }

    public static void forRange(int from, int to, int step, Intc cons) {
        if (step == 0) throw new StackOverflowError("Step can not be 0.");
        for (int index = from; index < to; index += step) cons.get(index);
    }

    public static <T> void forEach(Seq<T> it, ForCons<T> cons) {
        for (int index = 0; index < it.size; index += 1) cons.get(index, it.get(index));
    }

    public static <T> void forEach(T[] it, ForCons<T> cons) {
        for (int index = 0; index < it.length; index += 1) cons.get(index, it[index]);
    }

    public static void forEach(int[] it, Intc2 cons) {
        for (int index = 0; index < it.length; index += 1) cons.get(index, it[index]);
    }

}
