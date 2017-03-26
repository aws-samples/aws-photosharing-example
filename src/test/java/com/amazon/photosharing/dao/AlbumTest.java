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

import com.amazon.photosharing.listener.Persistence;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class AlbumTest {

    private void checkAlbum(Album _album) {
        Assert.assertEquals(_album.getName(), "testAlbum");
        this.checkUser(_album.getUser());
        Assert.assertEquals(_album.getMedia().size(), 2);
        List<Comment> _comments = _album.getComments();
        Assert.assertEquals(_comments.size(), 1);
    }

    private void checkUser(User _user) {
        Assert.assertEquals(_user.getUserName(), "uname");
        Assert.assertEquals(_user.getEmail(), "jon@doe.com");
        Assert.assertEquals(_user.getPassword(), "test1234");
        Assert.assertEquals(_user.getRoles().size(), 1);
        Assert.assertEquals(_user.getRoles().get(0).getRole(), com.amazon.photosharing.enums.Role.ADMINISTRATOR);
    }

    private User createUser() {

        List<Role> rolesList = new ArrayList<>();
        Role role = new Role();
        role.setRole(com.amazon.photosharing.enums.Role.ADMINISTRATOR);
        rolesList.add(role);

        User user = new User();
        user.setUserName("uname");
        user.setEmail("jon@doe.com");
        user.setPassword("test1234");
        user.setSalt("salt".getBytes());
        user.setRoles(rolesList);

        return user;
    }

    private List<Media> createMediaList() {

        List<Media> mediaList = new ArrayList<>();

        Media media1, media2;

        media1 = new Media();
        media1.setS3FileName("s3FileName");
        media1.setS3Bucket("s3Bucket");

        media2 = new Media();
        media2.setS3FileName("s3FileName2");
        media2.setS3Bucket("s3Bucket");

        mediaList.add(media1);
        mediaList.add(media2);

        return mediaList;
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();
            Album album = new Album();
            album.setName("testAlbum");

            List<Comment> comments = new ArrayList<>();
            Comment comment = new Comment();
            comment.setText("mycomment");
            comments.add(comment);

            User user = this.createUser();

            _em.getTransaction().begin();
            _em.persist(user);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select u from User u where u.userName='uname'");
            User retUser = (User)query.getSingleResult();

            album.setUser(retUser);

            List<Media> mediaList = this.createMediaList();
            album.setComments(comments);
            album.setMedia(mediaList);

            _em.getTransaction().begin();
            _em.merge(album);
            _em.getTransaction().commit();

            query = _em.createQuery("select a from Album a where a.name='testAlbum'");
            Album retAlbum = (Album)query.getSingleResult();

            this.checkAlbum(retAlbum);

            _em.getTransaction().begin();
            retAlbum.getMedia().forEach(_em::remove);
            _em.remove(retAlbum);
            _em.remove(retUser);

            for (Role role : retUser.getRoles())
                _em.remove(role);

            _em.getTransaction().commit();

        }

        catch (Exception exc) {
            error = true;
            exc.printStackTrace();
        }

        Assert.assertFalse(error);

        error = false;
        try {
            _em = Persistence.createEntityManager();

            Query query = _em.createQuery("select a from Album a where a.name='testAlbum'");
            Album retAlbum = (Album)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }

}
