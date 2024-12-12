package us.dot.its.jpo.ode.api.accessors.reports;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.ReportDocument;

public interface ReportRepository extends DataLoader<ReportDocument> {
    Query getQuery(String reportName, Integer intersectionID, Integer roadRegulatorID, Long startTime, Long endTime,
            boolean includeReportContents, boolean latest);

    long getQueryResultCount(Query query);

    long getQueryFullCount(Query query);

    List<ReportDocument> find(Query query);

}