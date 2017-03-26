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

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "role")
@Cacheable
@Cache(region="role", usage=CacheConcurrencyStrategy.READ_WRITE)
@XmlRootElement
public class Role implements Serializable {
	
	private static final long serialVersionUID = -6670814053304500082L;

	private com.amazon.photosharing.enums.Role _role;	
	
	private Set<User> _users;	
	
	public Role() {}
	public Role(com.amazon.photosharing.enums.Role p_role) {
		setRole(p_role);
	}

	@Id
	@Enumerated(EnumType.STRING)	
	public com.amazon.photosharing.enums.Role getRole() {return this._role;}
	public void setRole(com.amazon.photosharing.enums.Role p_role) {this._role = p_role;}	
	
	@XmlTransient
	@LazyCollection(LazyCollectionOption.EXTRA)
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="roles")	
	public Set<User> getUsers() {return this._users;}
	public void setUsers(Set<User> p_users) {this._users = p_users;}
		
}
