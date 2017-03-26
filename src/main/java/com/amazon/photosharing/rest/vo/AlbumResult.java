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

package com.amazon.photosharing.rest.vo;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Comment;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class AlbumResult {

    Long id, size;
    List<CommentResult> comment = new ArrayList<>();
    List<MediaResult> media = new ArrayList<>();
    String name;
    User user;

    public AlbumResult(Album album) {
        if (album != null) {

            id = album.getId();
            for (Media med : album.getMedia()) {
                MediaResult mr = new MediaResult(med);
                media.add(mr);
            }
            name = album.getName();
            size = album.getSize();
            user = album.getUser();
            for (Comment cmt : album.getComments()) {
                CommentResult cmtRes = new CommentResult(cmt);
                comment.add(cmtRes);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<CommentResult> getComment() {
        return comment;
    }

    public void setComment(List<CommentResult> comment) {
        this.comment = comment;
    }

    public List<MediaResult> getMedia() {
        return media;
    }

    public void setMedia(List<MediaResult> media) {
        this.media = media;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
