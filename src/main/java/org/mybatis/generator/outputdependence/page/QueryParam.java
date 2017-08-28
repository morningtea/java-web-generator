package org.mybatis.generator.outputdependence.page;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 查询条件类，分页查询条件类需要继承该类
 */
public class QueryParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录页码 */
    @JsonIgnore
    private int page = 1;

    /** 限制纪录条数 */
    @JsonIgnore
    private int limit = 10;

    /** 总纪录数 */
    @JsonIgnore
    private int totalCount;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @JsonIgnore
    public int getStart() {
        int start = (page - 1) * limit;
        return start;
    }

    @JsonIgnore
    public int getTotalPage() {
        if (this.totalCount == 0) {
            return 0;
        }
        int totalPage = this.totalCount / this.limit;
        if (this.totalCount % this.limit != 0) {
            totalPage++;
        }
        return totalPage;
    }
}
