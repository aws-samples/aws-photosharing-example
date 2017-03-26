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

package com.amazon.photosharing.facade;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.Role;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.model.SelectModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlbumFacadeTest {

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

    private AlbumFacade albumFacade;
    private Album testAlbum;
    private User testUser;
    private EntityManager _em = Persistence.createEntityManager();

    @BeforeClass
    public void initTest() throws IOException {
        albumFacade = new AlbumFacade();
        _logger.info("Init AlbumFacadeTest");
        this.testAlbum = createAlbum();
    }

    private Album createAlbum() {
        Album album = new Album();
        Role role = new Role();
        role.setRole(com.amazon.photosharing.enums.Role.ADMINISTRATOR);

        List<Role> roles = new ArrayList<>();
        roles.add(role);

        testUser = new User();
        testUser.setSalt("salt123".getBytes());
        testUser.setPassword("password");
        testUser.setUserName("jondoe");
        testUser.setEmail("jon@doe.com");
        testUser.setRoles(roles);

        _em.getTransaction().begin();
        _em.persist(testUser);
        _em.getTransaction().commit();

        album.setUser(testUser);
        album.setName("__testAlbum");

        // Creating Media for John
        Media pictureOne = new Media();
        pictureOne.setS3FileName("PicOne");
        pictureOne.setS3Bucket("s3Bucket_11223344sfasfsc");
        pictureOne.setName("___test_one");
        pictureOne.setUser(testUser);
        pictureOne.setAlbums(Stream.of(album).collect(Collectors.toList()));

        // Sharing picture with Bob and Alice
        Media pictureTwo = new Media();
        pictureTwo.setS3FileName("PicTwo");
        pictureTwo.setS3Bucket("s3Bucket_11223344sfasfsc");
        pictureTwo.setName("___test_two");
        pictureTwo.setUser(testUser);
        pictureTwo.setAlbums(Stream.of(album).collect(Collectors.toList()));

        List<Media> mediaList = new ArrayList<>();
        mediaList.add(pictureOne);
        mediaList.add(pictureTwo);

        album.setMedia(mediaList);
        return album;
    }

    @Test(priority = 1)
    public void testStoreAlbum() {

        Album album = albumFacade.storeAlbum(testAlbum, testUser);
        Assert.assertNotNull(album);
        Assert.assertEquals(album, testAlbum);
        Assert.assertEquals(album.getMedia().size(), testAlbum.getMedia().size());
    }

    @Test(priority = 2)
    public void testFindAlbum() {
        Album album = albumFacade.findAlbum(testAlbum.getId());
        Assert.assertNotNull(album);

        Assert.assertEquals(album, testAlbum);
        Assert.assertEquals(album.getMedia().size(), testAlbum.getMedia().size());
    }

    @Test(priority = 3)
    public void testAddToAlbum() {
        SelectModel selectModel = new SelectModel();

        Media pictureThree = new Media();
        pictureThree.setS3FileName("PicThree");
        pictureThree.setS3Bucket("s3Bucket_11223344sfasfsc");
        pictureThree.setName("___test_three");
        pictureThree.setUser(testUser);

        _em.getTransaction().begin();
        _em.persist(pictureThree);
        _em.getTransaction().commit();

        Media tmpMedia = Persistence.createEntityManager().find(Media.class, pictureThree.getId());

        _logger.info("Media ID: " + tmpMedia.getId());

        Assert.assertNotNull(tmpMedia.getId());

        selectModel.put(tmpMedia.getId(), true);

        albumFacade.addToAlbum(testAlbum.getId(), selectModel);

        Album album = albumFacade.findAlbum(testAlbum.getId());
        Assert.assertEquals(album.getMedia().size(), 3);
    }

    @Test(priority = 4)
    public void testDeleteAlbums() throws Exception {
        // Delete Album and Media
        albumFacade.deleteAlbums(Stream.of(testAlbum.getId()).collect(Collectors.toList()), true);

        Thread.sleep(1000);

        int num = ((Number)Persistence.createEntityManager().createQuery("select count(m) from Media m where m.name like '%___test_%'").getSingleResult()).intValue();
        Assert.assertEquals(num, 0);

        num = ((Number)Persistence.createEntityManager().createQuery("select count(a) from Album a where a.name = '__testAlbum'").getSingleResult()).intValue();
        Assert.assertEquals(num, 0);
    }

    @AfterClass
    public void cleanUp() throws Exception {

        // Delete User

        _em.getTransaction().begin();
        _em.remove(testUser);
        _em.flush();
        _em.getTransaction().commit();
        _em.close();
    }
}
