package us.dot.its.jpo.ode.api.services.firmware;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;

/**
 * Parses Commsignia OTA firmware names:
 * {@code <variant>-<rw|ro>[-secureboot]-<release>.tar.sig}, where the variant is
 * {@code ob4-generic} or {@code rs4-generic} with an optional suffix.
 *
 * <p>This must accept and parse names exactly as {@code parse_firmware_name} in the
 * OBU OTA server's {@code commsignia_manifest.py}, which serves the same files.
 * Like that function, it splits the name into tokens instead of matching one
 * backtracking regular expression, so parsing time grows linearly with length.
 */
@Component
public class CommsigniaObuFirmwareFileNameParser implements ObuFirmwareFileNameParser {
    static final String EXTENSION = ".tar.sig";
    static final String PATTERN = "^(?:ob4|rs4)-generic(?:-[A-Za-z0-9._-]+)?-(?:rw|ro)(?:-secureboot)?"
            + "-[A-Za-z0-9][A-Za-z0-9._-]*\\.tar\\.sig$";
    private static final Pattern NAME_CHARACTERS = Pattern.compile("[A-Za-z0-9._-]++");
    private static final Map<String, ObuFirmwareDeviceType> DEVICE_TYPES = Map.of(
            "ob4", ObuFirmwareDeviceType.OBU, "rs4", ObuFirmwareDeviceType.RSU);
    private static final Map<String, Boolean> ACCESS_MODES = Map.of("rw", true, "ro", false);
    private static final String SECURE_BOOT_MARKER = "secureboot";

    @Override
    public String manufacturerName() {
        return "Commsignia";
    }

    @Override
    public String fileExtension() {
        return EXTENSION;
    }

    @Override
    public String fileNamePattern() {
        return PATTERN;
    }

    @Override
    public String exampleFileName() {
        return "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig";
    }

    @Override
    public Optional<ObuFirmwareFileMetadata> parse(String fileName) {
        if (fileName == null || fileName.length() > MAX_FILE_NAME_LENGTH || !fileName.endsWith(EXTENSION)) {
            return Optional.empty();
        }
        String stem = fileName.substring(0, fileName.length() - EXTENSION.length());
        if (!NAME_CHARACTERS.matcher(stem).matches()) {
            return Optional.empty();
        }

        String[] tokens = stem.split("-", -1);
        if (tokens.length < 4 || !DEVICE_TYPES.containsKey(tokens[0]) || !"generic".equals(tokens[1])) {
            return Optional.empty();
        }
        ObuFirmwareDeviceType deviceType = DEVICE_TYPES.get(tokens[0]);

        // Prefer the last rw/ro marker that leaves a valid release, so the variant is
        // as long as possible.
        for (int marker = tokens.length - 2; marker > 1; marker--) {
            Boolean writable = ACCESS_MODES.get(tokens[marker]);
            if (writable == null) {
                continue;
            }
            // A variant suffix after "-generic" must not be empty
            if (marker > 2 && join(tokens, 2, marker).isEmpty()) {
                continue;
            }

            String variant = join(tokens, 0, marker);
            if (SECURE_BOOT_MARKER.equals(tokens[marker + 1])) {
                String release = join(tokens, marker + 2, tokens.length);
                if (isRelease(release)) {
                    return Optional.of(new ObuFirmwareFileMetadata(deviceType, release, variant, writable, true));
                }
            }
            String release = join(tokens, marker + 1, tokens.length);
            if (isRelease(release)) {
                return Optional.of(new ObuFirmwareFileMetadata(deviceType, release, variant, writable, false));
            }
        }
        return Optional.empty();
    }

    private static String join(String[] tokens, int from, int to) {
        return String.join("-", Arrays.asList(tokens).subList(from, to));
    }

    private static boolean isRelease(String value) {
        // The name characters are already restricted to ASCII
        return !value.isEmpty() && Character.isLetterOrDigit(value.charAt(0));
    }
}
