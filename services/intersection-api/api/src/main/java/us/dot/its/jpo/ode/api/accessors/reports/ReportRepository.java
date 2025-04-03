package us.dot.its.jpo.ode.api.accessors.reports;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.ReportDocument;

public interface ReportRepository extends DataLoader<ReportDocument> {
    long count(String reportName, Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<ReportDocument> findLatest(String reportName, Integer intersectionID, Long startTime, Long endTime,
            boolean includeReportContents);

    Page<ReportDocument> find(String reportName, Integer intersectionID, Long startTime, Long endTime,
            boolean includeReportContents, Pageable pageable);

}