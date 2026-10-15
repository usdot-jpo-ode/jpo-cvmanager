package us.dot.its.jpo.ode.api.services.firmware;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/** Indexes OBU firmware naming conventions by manufacturer name. */
@Component
public class ObuFirmwareParserRegistry {
    private final Map<String, ObuFirmwareFileNameParser> parsers = new HashMap<>();

    public ObuFirmwareParserRegistry(List<ObuFirmwareFileNameParser> parsers) {
        // Fail fast so the parser used for a manufacturer never depends on bean order
        for (ObuFirmwareFileNameParser parser : parsers) {
            if (this.parsers.put(normalize(parser.manufacturerName()), parser) != null) {
                throw new IllegalStateException(
                        "Multiple OBU firmware parsers are registered for " + parser.manufacturerName());
            }
        }
    }

    public Optional<ObuFirmwareFileNameParser> find(String manufacturerName) {
        return manufacturerName == null ? Optional.empty()
                : Optional.ofNullable(parsers.get(normalize(manufacturerName)));
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
