package us.dot.its.jpo.ode.api.services.firmware;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadOptions;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadOptions.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadOptions.ModelOption;
import us.dot.its.jpo.ode.api.repositories.RsuModelRepository;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

@Service
@RequiredArgsConstructor
public class FirmwareUploadOptionsService {
    private final RsuModelRepository rsuModels;

    public FirmwareUploadOptions getOptions() {
        List<ManufacturerOption> manufacturers = new ArrayList<>();

        // The repository ordering lets adjacent models be grouped under one
        // manufacturer without additional database queries
        for (var model : rsuModels.findAllWithManufacturerOrdered()) {
            var manufacturer = model.getManufacturer();

            // The ota storage hierarchy is reserved for the separate OBU firmware
            // workflow and must not be offered as an RSU firmware upload destination
            if (ObuFirmwareStorageLayout.isReservedName(manufacturer.getName())) {
                continue;
            }

            var modelOption = new ModelOption(model.getId(), model.getName());
            if (manufacturers.isEmpty()
                    || !manufacturers.getLast().manufacturerId().equals(manufacturer.getId())) {
                manufacturers.add(new ManufacturerOption(
                        manufacturer.getId(), manufacturer.getName(), manufacturer.getFirmwareFileExtension(),
                        new ArrayList<>(List.of(modelOption))));
            } else {
                manufacturers.getLast().models().add(modelOption);
            }
        }

        return new FirmwareUploadOptions(manufacturers);
    }
}
