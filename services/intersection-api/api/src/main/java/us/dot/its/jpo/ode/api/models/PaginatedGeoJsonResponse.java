package us.dot.its.jpo.ode.api.models;

import lombok.Data;
import org.springframework.data.domain.Page;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeatureCollection;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.converters.HaasLocationConverter;

@Data
public class PaginatedGeoJsonResponse {
    private GeoJsonFeatureCollection data;
    private PageMetadata pagination;

    @Data
    public static class PageMetadata {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
    }

    public static PaginatedGeoJsonResponse from(Page<HaasLocation> page) {
        PaginatedGeoJsonResponse response = new PaginatedGeoJsonResponse();
        response.setData(HaasLocationConverter.toGeoJson(page.getContent()));

        PageMetadata metadata = new PageMetadata();
        metadata.setPageNumber(page.getNumber());
        metadata.setPageSize(page.getSize());
        metadata.setTotalElements(page.getTotalElements());
        metadata.setTotalPages(page.getTotalPages());
        response.setPagination(metadata);

        return response;
    }
}