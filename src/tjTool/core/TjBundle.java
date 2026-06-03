package tjTool.core;

import arc.Core;
import mindustry.ctype.UnlockableContent;

public class TjBundle {

    public static String get(String namespace, String attribute) {
        return Core.bundle.get(namespace + "." + attribute);
    }

    public static String get(String namespace, String path, String attribute) {
        return Core.bundle.get(namespace + "." + path + "." + attribute);
    }

    public static String getThis(String path) {
        return get("tj-tool", path);
    }

    public static String getBlock(String name, String attribute) {
        return get("block", name, attribute);
    }

    public static String description(UnlockableContent content, String name) {
        return (content.description != null ? content.description + "\n" : "")
                + get(content.getContentType().toString(), name, "description");
    }

    public static String details(UnlockableContent content, String name) {
        return get(content.getContentType().toString(), name, "details")
                + (content.details != null ? "\n\n" + content.details : "");
    }

}
