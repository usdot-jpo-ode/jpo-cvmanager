package us.dot.its.jpo.ode.api.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.mappers.RsuGeoInfoMapper;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class RsuInfoService {

    private final RsuRepository rsuRepository;
    private final RsuGeoInfoMapper rsuGeoInfoMapper;

    /**
     * Returns a list of GeoJSON Feature DTOs for all RSUs that belong to the
     * given organisation.
     */
    public List<RsuGeoInfoDto> getRsuGeoInfoByOrganization(String orgName) {
        List<Rsu> rsus = rsuRepository.findAllRsusByOrganizationName(orgName);
        log.debug("Fetched {} RSUs for organisation '{}'", rsus.size(), orgName);
        return rsus.stream()
                .map(rsuGeoInfoMapper::toDto)
                .collect(Collectors.toList());
    }
}
