package org.mybatis.generator.xsili.outputdependence.base.jpa;

import java.util.List;

public interface BaseService<T extends BaseEntity> {

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