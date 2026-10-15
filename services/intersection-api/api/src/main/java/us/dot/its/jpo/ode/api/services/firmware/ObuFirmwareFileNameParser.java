package us.dot.its.jpo.ode.api.services.firmware;

import java.util.Optional;

import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;

/**
 * A manufacturer's OBU firmware naming convention. OBUs choose firmware from the
 * OTA server manifest by file name, so uploads keep the vendor's original name
 * and its metadata is read from that name.
 */
public interface ObuFirmwareFileNameParser {
    /** Matches the obu_firmware file_name and obu_ota_requests version columns. */
    int MAX_FILE_NAME_LENGTH = 128;

    /** The manufacturer name as stored in the manufacturers table. */
    String manufacturerName();

    String fileExtension();

    /** A JavaScript-compatible regular expression accepting the same names as {@link #parse}. */
    String fileNamePattern();

    String exampleFileName();

    /** Returns the metadata encoded in a file name, or empty when it does not follow the convention. */
    Optional<ObuFirmwareFileMetadata> parse(String fileName);
}
