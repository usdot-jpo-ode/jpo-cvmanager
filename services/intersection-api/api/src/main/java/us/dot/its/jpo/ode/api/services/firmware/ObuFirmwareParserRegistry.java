package us.dot.its.jpo.ode.api.services.firmware;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/** Indexes OBU firmware naming conventions by manufacturer name. */
@Component
public class ObuFirmwareParserRegistry {
    private final Map<String, ObuFirmwareFileNameParser> parsers = new LinkedHashMap<>();

    public ObuFirmwareParserRegistry(List<ObuFirmwareFileNameParser> parsers) {
        // Fail fast so the parser used for a manufacturer never depends on bean order
        for (ObuFirmwareFileNameParser parser : parsers) {
            if (ObuFirmwareStorageLayout.isReservedName(parser.manufacturerName())) {
                throw new IllegalStateException(
                        "OBU firmware parser uses the reserved manufacturer name " + parser.manufacturerName());
            }
            if (this.parsers.put(normalize(parser.manufacturerName()), parser) != null) {
                throw new IllegalStateException(
                        "Multiple OBU firmware parsers are registered for " + parser.manufacturerName());
            }
        }
    }

    public Collection<ObuFirmwareFileNameParser> parsers() {
        return List.copyOf(parsers.values());
    }

    public Optional<ObuFirmwareFileNameParser> find(String manufacturerName) {
        return manufacturerName == null ? Optional.empty()
                : Optional.ofNullable(parsers.get(normalize(manufacturerName)));
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
