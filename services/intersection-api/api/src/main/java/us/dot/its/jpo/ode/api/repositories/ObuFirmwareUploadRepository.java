package us.dot.its.jpo.ode.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;

@Repository
public interface ObuFirmwareUploadRepository extends UploadLifecycleRepository<ObuFirmwareUpload> {

    /** The verified upload for each object, or its newest attempt when none is verified. */
    @Query(value = """
            select distinct on (object_name) * from obu_firmware_uploads
            where storage_provider = :provider and storage_container = :container
            order by object_name, (status = 'VERIFIED') desc, created_at desc, upload_id
            """, nativeQuery = true)
    List<ObuFirmwareUpload> findListingUploads(@Param("provider") String provider,
            @Param("container") String container);

    List<ObuFirmwareUpload> findByManufacturerIdAndFileNameAndStatus(Integer manufacturerId, String fileName,
            FirmwareUploadStatus status);
}
