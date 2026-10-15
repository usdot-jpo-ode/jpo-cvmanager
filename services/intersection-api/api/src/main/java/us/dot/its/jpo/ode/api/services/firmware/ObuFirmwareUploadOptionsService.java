package us.dot.its.jpo.ode.api.services.firmware;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadOptions;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/** Lists the manufacturers whose OBU firmware naming convention the API understands. */
@Service
@RequiredArgsConstructor
public class ObuFirmwareUploadOptionsService {
    private final ManufacturerRepository manufacturers;
    private final ObuFirmwareParserRegistry parsers;

    public UploadOptions getOptions() {
        var options = manufacturers.findAll(Sort.by("name")).stream()
                .filter(manufacturer -> !ObuFirmwareStorageLayout.isReservedName(manufacturer.getName()))
                .flatMap(manufacturer -> parsers.find(manufacturer.getName()).stream()
                        .map(parser -> new ManufacturerOption(manufacturer.getId(), manufacturer.getName(),
                                parser.fileExtension(), parser.fileNamePattern(),
                                ObuFirmwareFileNameParser.MAX_FILE_NAME_LENGTH, parser.exampleFileName())))
                .toList();
        return new UploadOptions(options);
    }
}
