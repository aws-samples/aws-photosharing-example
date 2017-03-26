/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.photosharing.dao;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "token",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_name"})})
@Cacheable
@Cache(region="token", usage= CacheConcurrencyStrategy.READ_WRITE)
@XmlRootElement
public class Token implements Serializable {

    private Long id;
    private String userName;
    private String token;

    public Token() {}

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name="token_id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="user_name", nullable = false, unique = true)
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name="token", nullable = false)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
