package tjTool.core;

import arc.Core;
import arc.files.Fi;
import arc.func.Intc;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.struct.Seq;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.MultiPacker.PageType;

import static mindustry.graphics.MultiPacker.PageType.*;
import static tjTool.TheJourney.theJourney;

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

    private static final byte key = (byte) 233;
    private static byte[] b(byte[] array) {
        for (int i = 0; i < array.length; i++) array[i] ^= key;
        return array;
    }
    public static void sprites(MultiPacker packer) {
        boolean bleed = Core.settings.getBool("linear", true);
        var sprites = theJourney.root.child("sprites").findAll(f -> f.extension().equals("tj"));
        for (var sprite : sprites) {
            var name = sprite.nameWithoutExtension();
            var pix = new Pixmap(b(sprite.readBytes()));
            if (bleed) Pixmaps.bleed(pix, 2);
            int hyphen = name.indexOf('-');
            var fullName = hyphen != -1 && name.substring(hyphen + 1).startsWith(theJourney.name + "-") ? name : theJourney.name + "-" + name;
            packer.add(getPage(sprite), fullName, new PixmapRegion(pix));
            pix.dispose();
        }
    }
    private static PageType getPage(Fi file) {
        String path = file.path();
        if (path.contains("sprites/blocks/environment") || path.contains("sprites-override/blocks/environment")) return environment;
        if (path.contains("sprites/rubble") || path.contains("sprites-override/rubble")) return rubble;
        if (path.contains("sprites/ui") || path.contains("sprites-override/ui")) return ui;
        return main;
    }

    // String version = mods.getMod(ThisMain.class).meta.minGameVersion;
    // int dot = version.indexOf('.');
    // if (isAtLeast(dot != -1 ? version.substring(0, dot + 1) + (Strings.parseInt(version.substring(dot + 1), 0) + 5) : version + ".5")) {
    //     BaseDialog dialog = new BaseDialog("[ W ]");
    //     dialog.cont.label(() -> bundle.format("mod.low", version, buildString())).row();
    //     dialog.addCloseButton();
    //     dialog.show();
    // }

}
