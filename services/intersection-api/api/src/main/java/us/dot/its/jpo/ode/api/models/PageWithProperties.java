package us.dot.its.jpo.ode.api.models;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class PageWithProperties<T> implements Page<T> {

    private final Page<T> delegate;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final long queryTimestamp;

    public PageWithProperties(List<T> result, Pageable pageable, Long total, long queryTimestamp) {
        this.delegate = new PageImpl<>(result, pageable, total);
        this.totalElements = delegate.getTotalElements();
        this.totalPages = delegate.getTotalPages();
        this.hasNext = delegate.hasNext();
        this.queryTimestamp = queryTimestamp;
    }

    public PageWithProperties(Page<T> delegate, long queryTimestamp) {
        this.delegate = delegate;
        this.totalElements = delegate.getTotalElements();
        this.totalPages = delegate.getTotalPages();
        this.hasNext = delegate.hasNext();
        this.queryTimestamp = queryTimestamp;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public long getQueryTimestamp() {
        return queryTimestamp;
    }

    @Override
    public int getNumber() {
        return delegate.getNumber();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public int getNumberOfElements() {
        return delegate.getNumberOfElements();
    }

    @Override
    public List<T> getContent() {
        return delegate.getContent();
    }

    @Override
    public boolean hasContent() {
        return delegate.hasContent();
    }

    @Override
    public Sort getSort() {
        return delegate.getSort();
    }

    @Override
    public boolean isFirst() {
        return delegate.isFirst();
    }

    @Override
    public boolean isLast() {
        return delegate.isLast();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public Pageable nextPageable() {
        return delegate.nextPageable();
    }

    @Override
    public Pageable previousPageable() {
        return delegate.previousPageable();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return delegate.map(converter);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }
}