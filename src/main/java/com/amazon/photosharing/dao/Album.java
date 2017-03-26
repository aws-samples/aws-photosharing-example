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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "album")
public class Album extends Creatable implements Serializable {

    private static final long serialVersionUID = -237489612297251158L;
    
	private Long id;
    private String name;    
    private transient List<Comment> comment = new ArrayList<Comment>();
    private transient List<Share> shares = new ArrayList<Share>();
    private transient List<Media> media = new ArrayList<Media>();

    private transient long size = 0; 
    
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)    
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    @Column(length=255)
    public String getName() {return name;}
    public void setName(String p_name) {this.name = p_name;}
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    
    @OneToMany(mappedBy = "album")
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<Comment> getComments() {return comment;}
    public void setComments(List<Comment> comment) {this.comment = comment;}

    @OneToMany(mappedBy = "album", orphanRemoval=true)    
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<Share> getShares() {return shares;}
    public void setShares(List<Share> shares) {this.shares = shares;}
    
    @XmlTransient
    @LazyCollection(LazyCollectionOption.EXTRA)  
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "albums", cascade=CascadeType.PERSIST)    
    public List<Media> getMedia() {return media;}    
    public void setMedia(List<Media> media) {this.media = media;}
        
    public long getSize() {return size;}
    public void setSize(long p_size) {this.size = p_size;}
    
    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +                                 
                '}';
    }
    
    @PostLoad
    private void postLoad() {
    	setSize(getMedia().size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        return getId() != null ? getId().equals(album.getId()) : album.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
