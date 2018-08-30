package services;

import services.Page;

import java.util.List;

public class PageImpl<T> implements Page<T> {

    private final int pageSize;
    private final long totalRowCount;
    private final int pageIndex;
    private final List<T> list;

    private final String sortBy;
    private final String order;

    public PageImpl(List<T> data, Long total, Integer page, Integer pageSize) {
        this.pageSize = pageSize;
        this.totalRowCount = total;
        this.pageIndex = page;
        this.list = data;
        this.sortBy = null;
        this.order = null;
    }

    public PageImpl(List<T> data, Long total, Integer page, Integer pageSize, String sortBy, String order) {
        this.pageSize = pageSize;
        this.totalRowCount = total;
        this.pageIndex = page;
        this.list = data;
        this.sortBy = sortBy;
        this.order = order;
    }

    public Long getTotalRowCount() {
        return totalRowCount;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Long getPageCount() {
        return Double.valueOf(Math.ceil(Double.valueOf(totalRowCount) / pageSize)).longValue();
    }

    public List<T> getList() {
        return list;
    }

    public Boolean getHasPrev() {
        return pageIndex > 1;
    }

    public Boolean getHasNext() {
        return getPageCount() > pageIndex;
    }

    public Integer getStart() {
        return (pageIndex - 1) * pageSize + 1;
    }

    public Integer getEnd() {
        return getStart() + Math.min(pageSize, list.size()) - 1;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    @Override
    public String getSortBy() {
        return this.sortBy;
    }

    @Override
    public String getOrder() {
        return this.order;
    }
}
