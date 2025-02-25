package us.dot.its.jpo.ode.api.accessors;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public interface PageWrapper {

    /**
     * Wrap the given object in a page object. Intended to be used when applying a
     * limit of 1 to a query, while attempting to return a Paged response
     * 
     * @param <T>    the type of the object to wrap
     * @param latest the object to wrap
     * @return a page object containing the given object
     */
    default <T> Page<T> wrapSingleResultWithPage(T latest) {
        return new PageImpl<>(Collections.singletonList(latest));
    }
}
