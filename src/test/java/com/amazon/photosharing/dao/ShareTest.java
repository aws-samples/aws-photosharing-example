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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ShareTest {

    private User user, shareUser;
    private Media media;
    private EntityManager em;

    @BeforeClass
    private void init() {

        em = Persistence.createEntityManager();

        // Create user
        user = new User();
        user.setEmail("jon@doe.com");
        user.setUserName("jon");
        user.setPassword("password");
        user.setSalt("salt123".getBytes());

        // Create share user
        shareUser = new User();
        shareUser.setEmail("alice@doe.com");
        shareUser.setUserName("alice");
        shareUser.setPassword("password");
        shareUser.setSalt("salt123".getBytes());

        // Create media
        media = new Media();
        media.setUser(user);
        media.setName("mediaName");

        em.getTransaction().begin();
        em.persist(user);
        em.persist(shareUser);
        em.persist(media);
        em.getTransaction().commit();
    }

    private void checkShare(Share share) {
        Assert.assertEquals(share.getName(), "myShare");
        Assert.assertEquals(share.getUser().getId(), user.getId());
        Assert.assertEquals(share.getSharedWith().getId(), shareUser.getId());
        Assert.assertEquals(share.getMedia().getId(), media.getId());
    }

    private Share createData() {

        // Create Share
        Share share = new Share();
        share.setMedia(media);
        share.setSharedWith(shareUser);
        share.setUser(user);
        share.setName("myShare");
        share.setSharedWith(shareUser);

        return share;
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {

            Share share = createData();

            em.getTransaction().begin();
            em.persist(share);
            em.getTransaction().commit();

            Query query = em.createQuery("select s from Share s where s.name='myShare'");
            Share retShare = (Share) query.getSingleResult();

            checkShare(retShare);

            em.getTransaction().begin();
            em.remove(retShare);
            em.getTransaction().commit();
        } catch (Exception exc) {
            error = true;
            exc.printStackTrace();
        }

        Assert.assertFalse(error);

        error = false;
        try {

            Query query = em.createQuery("select s from Share s where s.name='myShare'");
            Share retShare = (Share) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }

    @AfterClass
    private void cleanUp() {

        em.getTransaction().begin();
        em.remove(media);
        em.remove(user);
        em.remove(shareUser);
        em.getTransaction().commit();
    }
}
