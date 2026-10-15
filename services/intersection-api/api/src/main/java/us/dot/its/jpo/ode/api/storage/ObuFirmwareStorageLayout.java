package us.dot.its.jpo.ode.api.storage;

import java.util.Locale;

/**
 * Object-storage layout for OBU firmware. Every OBU firmware object is stored
 * beneath the reserved {@code ota/} root, which the RSU firmware workflow must
 * neither list nor write. Older deployments also registered OBU firmware under
 * a manufacturer named {@code ota}, so that name is reserved as well.
 */
public final class ObuFirmwareStorageLayout {
    public static final String ROOT = "ota";
    public static final String PREFIX = ROOT + "/";

    private ObuFirmwareStorageLayout() {
    }

    /** Returns whether a manufacturer name or object path segment is the reserved root. */
    public static boolean isReservedName(String name) {
        return name != null && ROOT.equals(name.trim().toLowerCase(Locale.ROOT));
    }

    /** Returns whether an object name is stored beneath the reserved root. */
    public static boolean isObuObjectName(String objectName) {
        if (objectName == null) {
            return false;
        }
        int separator = objectName.indexOf('/');
        return isReservedName(separator < 0 ? objectName : objectName.substring(0, separator));
    }

    /**
     * Builds {@code ota/<manufacturer>/<release>/<file name>}. Callers validate each
     * segment, and the original file name is kept because OBUs download by name.
     */
    public static String objectName(String manufacturer, String releaseVersion, String fileName) {
        return String.join("/", ROOT, manufacturer, releaseVersion, fileName);
    }
}
