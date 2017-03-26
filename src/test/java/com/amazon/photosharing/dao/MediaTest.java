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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.amazon.photosharing.listener.Persistence;

public class MediaTest {

    private void checkMedia(Media _media) {
        Assert.assertEquals(_media.getS3FileName(), "s3FileName__");
        Assert.assertEquals(_media.getS3Bucket(), "s3Bucket");
        List<Comment> tmpComments = _media.getComments();
        Assert.assertTrue(tmpComments.size() == 1);

        Comment tmpComment = tmpComments.get(0);
        Assert.assertEquals(tmpComment.getText(), "mycomment");
    }

    private void prepareData(EntityManager _em) {
        // First we create three different users
        // - John
        // - Bob
        // - Alice

        // John has two pictures
        // One picture is shared with Bob and Alice
        // One picture is shared with Alice

        // Alice has one picture
        // This picture is shared with John

        User userJohn = new User();
        userJohn.setUserName("john");
        userJohn.setEmail("jon@test.com");
        userJohn.setPassword("test1234");
        userJohn.setSalt("salt".getBytes());

        User userBob = new User();
        userBob.setUserName("bob");
        userBob.setEmail("bob@test.com");
        userBob.setPassword("test1234");
        userBob.setSalt("salt".getBytes());

        User userAlice = new User();
        userAlice.setUserName("alice");
        userAlice.setEmail("alice@test.com");
        userAlice.setPassword("test1234");
        userAlice.setSalt("salt".getBytes());

        // Creating Media for John
        Media pictureOne = new Media();
        pictureOne.setS3FileName("PicOne");
        pictureOne.setS3Bucket("s3Bucket");

        // Sharing picture with Bob and Alice
        Media pictureTwo = new Media();
        pictureTwo.setS3FileName("PicTwo");
        pictureTwo.setS3Bucket("s3Bucket");

        List<Media> johnMedia = new ArrayList<>();
        johnMedia.add(pictureOne);
        johnMedia.add(pictureTwo);

        userJohn.setMedia(johnMedia);

        // Creating media for Alice
        Media pictureThree = new Media();
        pictureThree.setS3FileName("PicThree");
        pictureThree.setS3Bucket("s3Bucket");

        List<Media> aliceMedia = new ArrayList<>();
        aliceMedia.add(pictureThree);

        userAlice.setMedia(aliceMedia);

        _em.getTransaction().begin();
        _em.persist(userJohn);
        _em.persist(userBob);
        _em.persist(userAlice);
        _em.getTransaction().commit();

        List<Share> johnShares = new ArrayList<>();

        // Sharing picture one with Alice
        Share jaShareOne = new Share();
        jaShareOne.setUser(userJohn);
        jaShareOne.setSharedWith(userAlice);
        jaShareOne.setMedia(pictureOne);

        // Sharing picture one with Bob
        Share jbShareOne = new Share();
        jbShareOne.setUser(userJohn);
        jbShareOne.setSharedWith(userBob);
        jbShareOne.setMedia(pictureOne);

        // Sharing picture two with Alice
        Share jaShareTwo = new Share();
        jaShareTwo.setUser(userJohn);
        jaShareTwo.setSharedWith(userAlice);
        jaShareTwo.setMedia(pictureTwo);

        johnShares.add(jaShareOne);
        johnShares.add(jbShareOne);
        johnShares.add(jaShareTwo);

        userJohn.getShares().addAll(johnShares);

        List<User> sharingListTwo = new ArrayList<>();
        sharingListTwo.add(userAlice);

        // Sharing picture with John
        List<User> sharingListThree = new ArrayList<>();
        sharingListThree.add(userJohn);

        List<Share> aliceShares = new ArrayList<>();

        Share aShare = new Share();
        aShare.setMedia(pictureThree);
        aShare.setSharedWith(userJohn);
        aShare.setUser(userAlice);

        userAlice.getShares().addAll(aliceShares);

        _em.getTransaction().begin();
        _em.merge(userJohn);
        _em.merge(userBob);
        _em.merge(userAlice);
        _em.getTransaction().commit();
    }

    @Test
    public void testShareMedia() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            this.prepareData(_em);

            // Now fetching John
            TypedQuery<User> query =_em.createQuery("select u from User u where u.userName=:userName", User.class);

            query.setParameter("userName", "john");
            User john = query.getSingleResult();

            query.setParameter("userName", "bob");
            User bob = query.getSingleResult();

            query.setParameter("userName", "alice");
            User alice = query.getSingleResult();

            Assert.assertEquals(john.getMedia().size(), 2);
            Assert.assertEquals(john.getShares().size(), 3);
            List<Share> johnShares = john.getShares();
            int picOneCnt = 0;
            int picTwoCnt = 0;
            for (Share s : johnShares) {
                if (s.getMedia().getS3FileName().equals("PicOne")) {
                   picOneCnt++;
                } else if (s.getMedia().getS3FileName().equals("PicTwo")) {
                    picTwoCnt++;
                }
            }

            Assert.assertEquals(picOneCnt, 2);
            Assert.assertEquals(picTwoCnt, 1);

            _em.getTransaction().begin();
            _em.remove(john);
            _em.remove(bob);
            _em.remove(alice);
            _em.getTransaction().commit();
        }

        catch (Exception exc) {
            error = true;
            exc.printStackTrace();
        }

        Assert.assertFalse(error);
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            List<Comment> comments = new ArrayList<>();
            Comment comment = new Comment();
            comment.setText("mycomment");
            comments.add(comment);

            Media media = new Media();
            media.setS3FileName("s3FileName__");
            media.setS3Bucket("s3Bucket");
            media.setComments(comments);

            _em.getTransaction().begin();
            _em.persist(media);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select m from Media m where m.s3FileName='s3FileName__'");
            Media retMedia = (Media)query.getSingleResult();

            checkMedia(retMedia);

            _em.getTransaction().begin();
            _em.remove(retMedia);
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

            Query query = _em.createQuery("select m from Media m where m.s3FileName='s3FileName'");
            Media retMedia = (Media)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);

    }
}
