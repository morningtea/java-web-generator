package org.mybatis.generator.xsili.outputdependence.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

public class SimplePage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据集合 */
    private List<T> rows;

    /** 当前页码 */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 总页数 */
    private int totalPage;

    /** 总记录数 */
    private long totalCount;

    private SimplePage() {
    }

    public List<T> getRows() {
        if (rows == null) {
            return new ArrayList<T>();
        }
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public static <T> SimplePage<T> getPage(Page<T> page) {
        return getPage(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    /**
     * 
     * @param rows
     * @param queryParam
     * @return
     */
    public static <T> SimplePage<T> getPage(List<T> rows, QueryParam queryParam) {
        return getPage(rows, queryParam.getPage(), queryParam.getLimit(), queryParam.getTotalCount());
    }

    /**
     * 
     * @param rows
     * @param pageNum 当前页码
     * @param pageSize 限制记录条数
     * @param totalCount 总纪录数
     * @return
     */
    public static <T> SimplePage<T> getPage(List<T> rows, int pageNum, int pageSize, long totalCount) {
        SimplePage<T> simplePage = new SimplePage<T>();
        simplePage.setRows(rows);
        simplePage.setPageNum(pageNum);
        simplePage.setPageSize(pageSize);
        simplePage.setTotalCount(totalCount);
        simplePage.setTotalPage(getTotalPage(totalCount, pageSize));
        return simplePage;
    }

    /**
     * 默认pageSize 10
     * 
     * @return
     */
    public static <T> SimplePage<T> emptyPage() {
        SimplePage<T> simplePage = new SimplePage<T>();
        simplePage.setRows(new ArrayList<T>());
        simplePage.setPageNum(0);
        simplePage.setPageSize(10);
        simplePage.setTotalCount(0);
        simplePage.setTotalPage(0);
        return simplePage;
    }

    /**
     * 默认pageSize -1
     * 
     * @param rows
     * @return
     */
    public static <T> SimplePage<T> singlePage(List<T> rows) {
        SimplePage<T> simplePage = new SimplePage<T>();
        simplePage.setRows(rows);
        simplePage.setPageNum(0);
        simplePage.setPageSize(0);
        simplePage.setTotalCount(rows.size());
        simplePage.setTotalPage(rows.size() > 0 ? 1 : 0);
        return simplePage;
    }

    /**
     * 
     * @param totalCount
     * @param pageSize
     * @return
     */
    public static int getTotalPage(long totalCount, int pageSize) {
        return pageSize <= 0 ? 1 : (int) Math.ceil((double) totalCount / (double) pageSize);
    }
}
