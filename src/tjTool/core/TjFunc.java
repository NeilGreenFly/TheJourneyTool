package tjTool.core;

import arc.func.Intc;
import arc.struct.Seq;

@SuppressWarnings("unused")
public class TjFunc {

    public static int byBool(boolean b) {
        return b ? 1 : 0;
    }

    public static void forRange(int to, Intc cons) {
        for (int index = 0; index < to; index += 1) cons.get(index);
    }

    public static <T> void forEach(T[] it, ForCons<T> cons) {
        for (int index = 0; index < it.length; index += 1) cons.get(index, it[index]);
    }

    public static <T> void forEach(Seq<T> it, ForCons<T> cons) {
        for (int index = 0; index < it.size; index += 1) cons.get(index, it.get(index));
    }

}
