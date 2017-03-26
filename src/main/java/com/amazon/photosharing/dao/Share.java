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
import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.utils.Security;

@Entity
@Table(name = "share", uniqueConstraints={
		@UniqueConstraint(columnNames={"user_user_id", "sharedWith_user_id", "album_id"}),
		@UniqueConstraint(columnNames={"user_user_id", "sharedWith_user_id", "media_media_id"})},
		indexes = @Index(columnList = "hash", name = "share_hash"))
@Cacheable
@Cache(region="share", usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Share extends Creatable implements Serializable {

    private static final long serialVersionUID = -237489612297251158L;
    
	private Long id;
    private String name;    
    private String hash;
    private boolean fleeting = false;
    private boolean listed = false;
    
    private User user;
    private transient User sharedWith;
    
    private Date expires;
    
    private transient Media media;
    private transient Album album;       

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)    
    public Long getId() {return id;}
    public void setId(long id) {this.id = id;}
    
    @Column(length=255)
    public String getName() {return name;}
    public void setName(String p_name) {this.name = p_name;}
    
    @Column(length=255)
    public String getHash() {return hash;}
    public void setHash(String p_hash) {this.hash = p_hash;}
        
    public boolean isFleeting() {return fleeting;}
    public void setFleeting(boolean fleeting) {this.fleeting = fleeting;}
        
	public boolean isListed() {return listed;}
	public void setListed(boolean listed) {this.listed = listed;}

    @Temporal(TemporalType.TIMESTAMP)
    public Date getExpires() {return expires;}
    public void setExpires(Date expires) {this.expires = expires;}
    
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
            
    @ManyToOne(fetch=FetchType.EAGER, optional=true)
    public User getSharedWith() {return sharedWith;}
    public void setSharedWith(User user) {this.sharedWith = user;}
    
    @ManyToOne(optional=true)
    public Media getMedia() {return media;}
    public void setMedia(Media media) {this.media = media;}
    
    @ManyToOne(fetch=FetchType.LAZY, optional=true)
    public Album getAlbum() {return album;}
    public void setAlbum(Album album) {this.album= album;}
        
    @Transient
    public String getShareUrl() {
    	if (getAlbum() != null)
    		return MessageFormat.format(Configuration.SHARE_ALBUM_PUBLIC_URL_FORMAT.toString(), getHash());
    	else
    		return MessageFormat.format(Configuration.SHARE_MEDIA_PUBLIC_URL_FORMAT.toString(), getHash());
    }
    
    @PrePersist //create a hash to make brute force harder for unique hidden urls
    private void generateHash() {
    	if (getHash() == null)
    		setHash(Security.getRandomHash(255));
    }

    @Override
    public String toString() {
        return "Share{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hash='" + hash + '\'' +
                ", fleeting=" + fleeting +
                ", listed=" + listed +
                ", user=" + user +
                ", sharedWith=" + sharedWith +
                ", expires=" + expires +
                ", media=" + media +
                ", album=" + album +
                '}';
    }
}
