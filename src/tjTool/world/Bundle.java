package tjTool.world;

import static arc.Core.bundle;

/**
 * @author NeilGreenFly
 * @see mindustry.ctype.ContentType
 */
public interface Bundle {
    String dot = ".";
    String at = "@";

    String namespace();
    String specificKey();

    default String getKey() {
        return namespace() + dot + specificKey();
    }

    default String getKey(String attribute) {
        return getKey() + dot + attribute;
    }

    default String atKey() {
        return at + getKey();
    }

    default String atKey(String attribute) {
        return at + getKey(attribute);
    }

    default String bundle() {
        return bundle.get(getKey());
    }

    default String bundle(String attribute) {
        return bundle.get(getKey(attribute));
    }
}
