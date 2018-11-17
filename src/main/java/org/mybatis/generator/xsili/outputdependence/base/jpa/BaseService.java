package org.mybatis.generator.xsili.outputdependence.base.jpa;

import java.util.List;

public interface BaseService<T extends BaseEntity> {

    /**
     * 更新所有字段
     * 
     * @param t
     * @return
     */
    T updateAll(T t);

    /**
     * 只更新not null字段
     * 
     * @param t
     * @return
     */
    T updateNotNull(T t);

    /**
     * 逻辑删除
     * 
     * @param id
     * @return
     */
    void deleteById(Long id);

    /**
     * 
     * 根据id查询
     * 
     * @param id
     * @return isDeleted==true的记录
     */
    T getById(Long id);

    /**
     * 根据id集合查询
     * 
     * @param idList
     * @return isDeleted==true的记录
     */
    List<T> list(List<Long> idList);

}