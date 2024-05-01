package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class ShareItPageRequest extends PageRequest {

    private final int from;

    public ShareItPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        this.from = from;
    }

    public ShareItPageRequest(int from, int size) {
        super(from / size, size, Sort.unsorted());
        this.from = from;
    }

    @Override
    public long getOffset() {
        return from;
    }
}
