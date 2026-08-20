package us.dot.its.jpo.ode.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import us.dot.its.jpo.ode.api.repositories.ScmsHealthRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScmsHealthService {
    private final ScmsHealthRepository scmsHealthRepository;

    public List<ScmsHealthRsuProjection> getScmsStatuses(String organization) {
        return scmsHealthRepository.findLatestScmsHealthByOrganization(organization);
    }
}
