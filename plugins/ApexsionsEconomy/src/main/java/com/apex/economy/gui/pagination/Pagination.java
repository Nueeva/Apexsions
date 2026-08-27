package com.apex.economy.gui.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pagination<T> {

    private final int pageSize;
    private final List<T> items;

    public Pagination(int pageSize, List<T> items) {
        this.pageSize = Math.max(1, pageSize);
        this.items = items != null ? items : Collections.emptyList();
    }

    public int getMaxPages() {
        if (items.isEmpty()) return 1;
        return (int) Math.ceil((double) items.size() / pageSize);
    }

    public List<T> getPage(int page) {
        if (items.isEmpty()) return Collections.emptyList();
        int validPage = Math.max(1, Math.min(getMaxPages(), page));
        int fromIndex = (validPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        if (fromIndex >= items.size()) return Collections.emptyList();
        return items.subList(fromIndex, toIndex);
    }

    public boolean hasPrevious(int page) {
        return page > 1;
    }

    public boolean hasNext(int page) {
        return page < getMaxPages();
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return items.size();
    }
}
