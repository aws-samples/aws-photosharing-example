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

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "comments")
@Cacheable
@Cache(region="comment", usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Comment extends Creatable {
	
    private Long id;    
    private String text;
    
    private Media media;    
    private Album album;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name="comment_id", nullable = false, unique = true, length = 11)    
    public Long getId() {return id;}
    public void setId(long id) {this.id = id;}

    @Column(columnDefinition="TEXT")
    public String getText() {return text;}
    public void setText(String text) {this.text = text;}

    @ManyToOne
    public Media getMedia() {return media;}
    public void setMedia(Media media) {this.media = media;}
    
    @ManyToOne
    public Album getAlbum() {return album;}
    public void setAlbum(Album p_album) {this.album = p_album;}

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
