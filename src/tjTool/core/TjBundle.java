package tjTool.core;

import mindustry.ctype.UnlockableContent;

import static arc.Core.bundle;
import static java.lang.String.join;
import static tjTool.world.Bundle.dot;

public final class TjBundle {

    public static String getThis(String path) {
        return bundle.get("tj-tool." + path);
    }

    public static String description(UnlockableContent content, String name) {
        return (content.description != null ? content.description + "\n" : "")
                + bundle.get(join(dot, content.getContentType().toString(), name, "description"));
    }

    public static String details(UnlockableContent content, String name) {
        return bundle.get(join(dot, content.getContentType().toString(), name, "details"))
                + (content.details != null ? "\n\n" + content.details : "");
    }

}
