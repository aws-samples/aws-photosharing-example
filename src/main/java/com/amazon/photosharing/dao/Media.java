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
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.amazon.photosharing.enums.Configuration;

@Entity
@Table(name = "media",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"media_id"})})
public class Media extends Creatable implements Serializable {

	private static final long serialVersionUID = -5143521542024344590L;
	
	private static final String S3_URL_PREFIX = "https://s3-eu-west-1.amazonaws.com";	

    private Long id;
    private String name;
    private String s3Bucket;
    private String s3FileName;
    private String s3ThumbFileName;
    private List<Comment> comment = new ArrayList<>();
    private List<Share> shares = new ArrayList<>();
    private List<Album> albums = new ArrayList<>();

    private URL presignedThumbUrl;
    private URL presignedUrl;
    public  Date presignedThumbUrlExpires;
    public  Date presignedUrlExpires;
    
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "media_id", nullable = false, unique = true, length = 11)
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    @Column(length=255)
    public String getName() {return name;}
    public void setName(String p_name) {this.name = p_name;}
    
    @Column(name = "s3_bucket")
    public String getS3Bucket() {return s3Bucket;}
    public void setS3Bucket(String s3Bucket) {this.s3Bucket = s3Bucket;}

    @Column(name = "s3_file_name")
    public String getS3FileName() {return s3FileName;}
    public void setS3FileName(String s3FileName) {this.s3FileName = s3FileName;}

    @Column(name = "s3_thumb_file_name")
    public String getS3ThumbFileName() {return s3ThumbFileName;}
    public void setS3ThumbFileName(String s3FileName) {this.s3ThumbFileName = s3FileName;}
    
    @Column(name = "s3_presigned_thumb_url", length=2048)
    public URL getPresignedThumbUrl() {return this.presignedThumbUrl;}
    public void setPresignedThumbUrl(URL p_url) {this.presignedThumbUrl = p_url;}
    
    @Column(name = "s3_presigned_url", length=2048)
    public URL getPresignedUrl() {return this.presignedUrl;}
    public void setPresignedUrl(URL p_url) {this.presignedUrl = p_url;}
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getPresignedThumbUrlExpires() {return this.presignedThumbUrlExpires;}
    public void setPresignedThumbUrlExpires(Date p_date) {this.presignedThumbUrlExpires = p_date;}
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getPresignedUrlExpires() {return this.presignedUrlExpires;}
    public void setPresignedUrlExpires(Date p_date) {this.presignedUrlExpires = p_date;}
    
    @OneToMany(mappedBy = "media")
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<Comment> getComments() {return comment;}
    public void setComments(List<Comment> comment) {this.comment = comment;}
    
    @OneToMany(mappedBy = "media", orphanRemoval=true)    
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<Share> getShares() {return shares;}
    public void setShares(List<Share> shares) {this.shares = shares;}

    @ManyToOne
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}

    @ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
    @JoinTable(name = "album_media", 
    		   joinColumns = { 
						@JoinColumn(name = "media_id", nullable = false, updatable = false) }, 
				inverseJoinColumns = { 
						@JoinColumn(name = "album_id", nullable = false, updatable = false) })    
    public List<Album> getAlbums() {return albums;}
    public void setAlbums(List<Album> albums) {this.albums = albums;}    
    
    @Transient
    public String getS3Url() {
        String publicUrl = "";
        if (this.getS3FileName() != null && this.getS3FileName().trim().length() > 0)
            publicUrl = MessageFormat.format("{0}/{1}/{2}", S3_URL_PREFIX, this.getS3Bucket(), this.getS3FileName());

        return publicUrl;
    }
    
    @Transient
    public String getS3ThumbUrl() {
        String publicUrl = "";
        if (this.getS3ThumbFileName() != null && this.getS3ThumbFileName().trim().length() > 0)
            publicUrl = MessageFormat.format("{0}/{1}/{2}", S3_URL_PREFIX, this.getS3Bucket(), this.getS3ThumbFileName());

        return publicUrl;
    }
    
    @Transient
    public String getPrivateThumbUrl() {
        String publicUrl = "";
        if (this.getS3ThumbFileName() != null && this.getS3ThumbFileName().trim().length() > 0)
            publicUrl = MessageFormat.format(Configuration.PRIVATE_MEDIA_URL_FORMAT_THUMB.toString(), this.getId());

        return publicUrl;
    }
    
    @Transient
    public String getPrivateUrl() {
        String publicUrl = "";
        if (this.getS3ThumbFileName() != null && this.getS3ThumbFileName().trim().length() > 0)
            publicUrl = MessageFormat.format(Configuration.PRIVATE_MEDIA_URL_FORMAT.toString(), this.getId());

        return publicUrl;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", s3Bucket='" + s3Bucket + '\'' +
                ", s3FileName='" + s3FileName + '\'' +                              
                '}';
    }
}
