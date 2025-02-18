package us.dot.its.jpo.ode.api.utils;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PaginationUtil {

    public static <T> ResponseEntity<List<T>> createResponseEntityWithPaginationHeaders(Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        headers.add("X-Has-More", String.valueOf(page.hasNext()));
        return new ResponseEntity<List<T>>(page.getContent(), headers, HttpStatus.OK);
    }
}