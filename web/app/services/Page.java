package services;

import java.util.List;

public interface Page<T> {

    public Long getTotalRowCount();
    public Integer getPageIndex();
    public Integer getPageSize();
    public Long getPageCount();
    public List<T> getList();
    public Boolean getHasPrev();
    public Boolean getHasNext();
    public Integer getStart();
    public Integer getEnd();

    public String getSortBy();
    public String getOrder();

}
