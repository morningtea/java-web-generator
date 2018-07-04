/*
 * Copyright (c) 2016, XSILI and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.mybatis.generator.xsili.outputdependence.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * jpa model, 如果使用 mybatis 请删除相应注解<br>
 * 如果loginPhone不为空, 则相应地loginInfo有相应的登录信息
 */
@MappedSuperclass
public class CommonUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true)
    private String loginPhone;

    @Column(length = 50)
    private String userName;

    /** */
    @Column
    private Date createdDate;

    /** */
    @Column
    private Date updatedDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLoginPhone() {
        return loginPhone;
    }

    public void setLoginPhone(String loginPhone) {
        this.loginPhone = loginPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}


}
