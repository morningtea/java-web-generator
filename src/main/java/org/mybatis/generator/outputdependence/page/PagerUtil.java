package org.mybatis.generator.outputdependence.page;

import java.util.ArrayList;
import java.util.List;

public class PagerUtil {
    public static <T> Pager<T> getPager(List<T> rows, QueryParam queryParam) {
        return getPager(rows, queryParam.getPage(), queryParam.getLimit(), queryParam.getTotalCount());
    }

    /**
     * 
     * @param rows
     * @param page 当前页码
     * @param limit 限制记录条数
     * @param totalCount 总纪录数
     * @return
     */
    public static <T> Pager<T> getPager(List<T> rows, int page, int limit, int totalCount) {
        Pager<T> pagerModel = new Pager<T>();
        pagerModel.setRows(rows);
        pagerModel.setPage(page);
        pagerModel.setTotalCount(totalCount);
        int totalPage = getTotalPage(limit, totalCount);
        pagerModel.setTotalPage(totalPage);
        return pagerModel;
    }

    public static <T> Pager<T> emptyPager() {
        Pager<T> pager = new Pager<T>();
        pager.setRows(new ArrayList<T>());
        pager.setPage(1);
        pager.setTotalCount(0);
        pager.setTotalPage(0);
        return pager;
    }

    public static int getTotalPage(int limit, int records) {
        return records % limit == 0 ? records / limit : records / limit + 1;
    }
}
