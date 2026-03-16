package tjTool.core;

import arc.Core;

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

}
