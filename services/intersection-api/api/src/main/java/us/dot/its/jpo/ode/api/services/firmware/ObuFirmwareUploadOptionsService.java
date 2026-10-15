package us.dot.its.jpo.ode.api.services.firmware;

import java.util.Comparator;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadOptions;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;

/** Lists the manufacturers whose OBU firmware naming convention the API understands. */
@Service
@RequiredArgsConstructor
public class ObuFirmwareUploadOptionsService {
    private final ManufacturerRepository manufacturers;
    private final ObuFirmwareParserRegistry parsers;

    public UploadOptions getOptions() {
        // Only manufacturers with a naming convention can be offered, so start from
        // the parsers and include each one whose manufacturer exists
        var options = parsers.parsers().stream()
                .flatMap(parser -> manufacturers.findFirstByNameIgnoreCaseOrderByIdAsc(parser.manufacturerName())
                        .stream()
                        .map(manufacturer -> new ManufacturerOption(manufacturer.getId(), manufacturer.getName(),
                                parser.fileExtension(), parser.fileNamePattern(),
                                ObuFirmwareFileNameParser.MAX_FILE_NAME_LENGTH, parser.exampleFileName())))
                .sorted(Comparator.comparing(ManufacturerOption::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new UploadOptions(options);
    }
}
