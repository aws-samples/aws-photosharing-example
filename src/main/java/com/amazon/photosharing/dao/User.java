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
import java.security.SecureRandom;
import java.util.*;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.amazon.photosharing.utils.Security;

@Entity
@Table(name = "user",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_name"})})
@XmlRootElement
public class User implements Serializable {
	
	private static final long serialVersionUID = -2625751048924913356L;
	
    private Long id;
    private String userName;
    private String email;        
    private String password;       
    private byte[] salt;           

    private Date   lastLogin;

    private List<Media> media = new ArrayList<>();
	private List<Album> albums = new ArrayList<>();
	private List<Share> shares = new ArrayList<>();
	
    private List<Role> _roles = new ArrayList<>();
    private Media	_pic;
    
    public User() {}

    public User(String p_username, String p_password, String p_email) {    	
    	setUserName(p_username);
    	updatePassword(p_password);
    	setEmail(p_email);
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name="user_id")
    public Long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    @Column(name="user_name", nullable = false)
    public String getUserName() {return userName;}
    public void setUserName(String userName) {this.userName = userName;}
    
    @Column(nullable = false)
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
        
    @XmlTransient
    @Column(nullable = false)
    public String getPassword() {return password;}
    public void setPassword(String p_password ) {this.password = p_password;}

    public void updatePassword(String p_password) {
    	this.salt= new byte[16];
    	
    	Random random = new SecureRandom();
    	random.nextBytes(this.salt);
    	
    	this.password = Security.getPasswordHash(p_password, this.salt);
    }
    
    @XmlTransient
    @Column(name = "salt", nullable = false)
    public byte[] getSalt() {return salt;}  
    public void setSalt(byte[] p_salt) {this.salt = p_salt;}

    @XmlTransient
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "user", orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    public List<Media> getMedia() {return media;}
    public void setMedia(List<Media> media) {this.media = media;}
    
    @XmlTransient
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "user", orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    public List<Share> getShares() {return shares;}
    public void setShares(List<Share> shares) {this.shares = shares;}

    @XmlTransient
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "user", orphanRemoval=true, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    public List<Album> getAlbums() {return albums;}
    public void setAlbums(List<Album> albums) {this.albums = albums;}
    
    @XmlTransient
    @LazyCollection(LazyCollectionOption.EXTRA)
    @ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinTable(name = "role_mappings", joinColumns = { 
			@JoinColumn(name = "user_id", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "role", 
					nullable = false, updatable = false) })
    public List<Role> getRoles() {return _roles;}
    public void setRoles(List<Role> p_roles) {this._roles = p_roles;}

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastLogin() {return lastLogin;}
	public void setLastLogin(Date lastLogin) {this.lastLogin = lastLogin;}
	
	@XmlTransient
	@OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, optional=true, fetch=FetchType.LAZY)	
	public Media getProfilePicture() {return _pic;}	
	public void setProfilePicture(Media p_pic) {this._pic = p_pic;}

	@Transient
	public String getProfilePictureUrl() {return getProfilePicture()!=null?"":"";}

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", salt=" + Arrays.toString(salt) +
                ", lastLogin=" + lastLogin +               
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return getUserName().equals(user.getUserName());
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode();
    }
}
