package us.dot.its.jpo.ode.api.accessors.map;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;

public interface ProcessedMapRepository extends DataLoader<ProcessedMap<LineString>> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<ProcessedMap<LineString>> findLatest(Integer intersectionID, Long startTime, Long endTime, boolean compact);

    Page<ProcessedMap<LineString>> find(Integer intersectionID, Long startTime, Long endTime, boolean compact,
            Pageable pageable);

    List<IntersectionReferenceData> getIntersectionIDs();

    List<IDCount> getMapBroadcastRates(int intersectionID, Long startTime, Long endTime);

    /**
     * Retrieves the distribution of map broadcast rates for a given intersection
     * within a specified time range. The method performs an aggregation on the
     * MongoDB collection to calculate the number of messages per decisecond and
     * groups the results into buckets.
     *
     * @param intersectionID the ID of the intersection for which the broadcast rate
     *                       distribution is to be retrieved
     * @param startTime      the start time of the time range (in milliseconds since
     *                       epoch). If null, defaults to the epoch start time.
     * @param endTime        the end time of the time range (in milliseconds since
     *                       epoch). If null, defaults to the current time.
     * @return a list of {@link IDCount} objects representing the distribution of
     *         map broadcast rates
     */
    List<IDCount> getMapBroadcastRateDistribution(int intersectionID, Long startTime, Long endTime);

    List<IntersectionReferenceData> getIntersectionsContainingPoint(double longitude, double latitude);
}