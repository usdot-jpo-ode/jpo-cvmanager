package us.dot.its.jpo.ode.api.models;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DataResponse<T> {
    @JsonProperty("data")
    private List<T> data;
    @JsonProperty("totalElements")
    private Long totalElements;
    @JsonProperty("totalPages")
    private Long totalPages;
    @JsonProperty("hasMore")
    private Boolean hasMore;
    @JsonProperty("timestamp")
    private Long timestamp;

    public DataResponse(List<T> data) {
        this.data = data;
        this.totalElements = (long) data.size();
        this.totalPages = 1L;
        this.hasMore = false;
        this.timestamp = System.currentTimeMillis();
    }

    public DataResponse(PageWithProperties<T> data) {
        this.data = data.getContent();
        this.totalElements = data.getTotalElements();
        this.totalPages = (long) data.getTotalPages();
        this.hasMore = data.hasNext();
        this.timestamp = data.getQueryTimestamp();
    }

    public ResponseEntity<DataResponse<T>> getResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalElements));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        headers.add("X-Has-More", String.valueOf(hasMore));
        return new ResponseEntity<DataResponse<T>>(this, headers, HttpStatus.OK);
    }
}
