/*
 * Copyright (c) 2018, BENMA and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.mybatis.generator.xsili.outputdependence.base.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author 叶鹏
 * @date 2018年11月13日
 */
@MappedSuperclass
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** */
    // @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private Date gmtCreate;

    /** 修改时间 */
    @Column(nullable = false)
    private Date gmtModified;

    /** 是否已删除 */
    @Column(nullable = false)
    private Boolean isDeleted;

    /** */
    public Long getId() {
        return id;
    }

    /** */
    public void setId(Long id) {
        this.id = id;
    }

    /** 创建时间 */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /** 创建时间 */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /** 修改时间 */
    public Date getGmtModified() {
        return gmtModified;
    }

    /** 修改时间 */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /** 是否已删除 */
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    /** 是否已删除 */
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
