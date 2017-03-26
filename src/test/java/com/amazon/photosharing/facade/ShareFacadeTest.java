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

import com.amazon.photosharing.dao.*;
import com.amazon.photosharing.listener.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.amazon.photosharing.enums.Role.*;

// TODO: implement test

public class ShareFacadeTest {

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());
    private ShareFacade shareFacade;
    private ContentFacade contentFacade;

    private User testUser, shareUser1, shareUser2;
    private Role adminRole;
    private Media media;

    private List<Share> shares;
    private Share publicShare;

    private EntityManager _em = Persistence.createEntityManager();

    @BeforeClass
    public void initTest() throws IOException {
        _logger.info("Init ShareFacadeTest");

        shareFacade = new ShareFacade();
        contentFacade = new ContentFacade();

        // Creating users, roles, media and shares
        adminRole = createRole();
        List<Role> rolesList = new ArrayList<>();
        rolesList.add(adminRole);
        testUser = createUser("jon@doe.com", "jondoe", rolesList);
        shareUser1 = createUser("jane@doe.com", "janedoe", rolesList);
        shareUser2 = createUser("lilly@doe.com", "lillydoe", rolesList);

        _em.getTransaction().begin();
        _em.persist(testUser);
        _em.persist(shareUser1);
        _em.persist(shareUser2);
        _em.getTransaction().commit();

        List<User> shareUsers = new ArrayList<>();
        shareUsers.add(shareUser1);
        shareUsers.add(shareUser2);

        media = this.uploadMedia(testUser);
        shares = shareFacade.shareWithUsers(media, shareUsers);
    }

    private Media uploadMedia(User _user) throws IOException {
        URL url = this.getClass().getResource("../../../../amazon-aws-logo.jpg");
        String tmpFileName = url.getFile();

        File file = new File(tmpFileName);
        String fileName = file.getName();
        InputStream is = url.openStream();
        String contentType = URLConnection.guessContentTypeFromStream(is);

        Comment comment = new Comment();
        comment.setText("My comment");
        Set<Comment> comments = new HashSet<>();
        comments.add(comment);

        Media tmpMedia = contentFacade.uploadPictureToS3(_user, fileName, is, contentType, comment);
        return tmpMedia;
    }

    private Role createRole() {
        Role role = new Role();
        role.setRole(ADMINISTRATOR);

        TypedQuery<Role> query =_em.createQuery("select r from Role r where r.role=:roleName", Role.class);

        query.setParameter("roleName", ADMINISTRATOR);
        Role tmpRole = null;

        try {
            tmpRole = query.getSingleResult();
        }

        catch (javax.persistence.NoResultException exc) {
            exc.printStackTrace();
        }

        if (tmpRole != null)
            role = tmpRole;

        return role;
    }

    private User createUser(String email, String userName, List<Role> roles) {

        User user = new User();
        user.setSalt("salt123".getBytes());
        user.setPassword("password");
        user.setUserName(userName);
        user.setEmail(email);
        user.setRoles(roles);

        return user;
    }

    @Test
    public void testCreatedShares() {
        Assert.assertNotNull(shares);
        Assert.assertTrue(shares.size() == 2);
    }

    @Test
    public void testValidateSharePermissions() {
        Share testShare = shares.get(0);
        String hash = testShare.getHash();
        Share resShare = shareFacade.validateSharePermissions(hash, shareUser1);

        Assert.assertNotNull(resShare);
        Assert.assertEquals(resShare.getSharedWith().getEmail(), shareUser1.getEmail());
        Assert.assertEquals(resShare.getMedia().getId(), media.getId());
    }

    @Test
    public void testGeneratePublicShareURL() {
        publicShare = shareFacade.generatePublicShareURL(media);

        Assert.assertNotNull(publicShare);
        Assert.assertNotNull(publicShare.getShareUrl());
        Assert.assertEquals(publicShare.getUser().getEmail(), testUser.getEmail());

        shareFacade.deletePublicShare(publicShare.getId());

        TypedQuery<Share> query =_em.createQuery("select s from Share s where s.id=:id", Share.class);
        query.setParameter("id", publicShare.getId());

        boolean isException = false;
        try {
            Share tmpshare = query.getSingleResult();
        }

        catch (Exception exc) {
            exc.printStackTrace();
            isException = true;
        }

        Assert.assertTrue(isException);
    }

    @AfterClass
    public void cleanUp() throws InterruptedException {
        _logger.info("Clean up after test");

        // Deleting media
        List<Long> mediaIds = new ArrayList<>();
        mediaIds.add(media.getId());

        for (Share share : shares) {
            shareFacade.deletePublicShare(share.getId());
        }
        shareFacade.deletePublicShare(publicShare.getId());

        contentFacade.deleteMedia(mediaIds);
        Thread.sleep(5000);

        // Deleting users
        _em.getTransaction().begin();
        _em.remove(testUser);
        _em.remove(shareUser1);
        _em.remove(shareUser2);
        _em.getTransaction().commit();
    }
}
