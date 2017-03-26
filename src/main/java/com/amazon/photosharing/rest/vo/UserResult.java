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
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.Share;
import com.amazon.photosharing.dao.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class UserResult {

    private List<Long> shares = new ArrayList<>();
    private List<Long> media = new ArrayList<>();
    private List<Long> albums = new ArrayList<>();
    private Long id;
    private String userName, email;
    private Date lastLogin;

    public UserResult(User user) {
        if (user != null) {
            for (Share share : user.getShares()) {
                shares.add(share.getId());
            }

            id = user.getId();
            for (Media med : user.getMedia()) {
                media.add(med.getId());
            }

            userName = user.getUserName();
           for (Album album : user.getAlbums()) {
               albums.add(album.getId());
           }

            email = user.getEmail();
            lastLogin = user.getLastLogin();
        }
    }

    public List<Long> getShares() {
        return shares;
    }

    public void setShares(List<Long> shares) {
        this.shares = shares;
    }

    public List<Long> getMedia() {
        return media;
    }

    public void setMedia(List<Long> media) {
        this.media = media;
    }

    public List<Long> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Long> albums) {
        this.albums = albums;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
