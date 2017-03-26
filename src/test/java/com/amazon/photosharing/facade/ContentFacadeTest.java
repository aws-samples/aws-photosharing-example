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

import com.amazon.photosharing.dao.Comment;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.utils.ContentHelper;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
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

public class ContentFacadeTest {

    private UserFacade userFacade;
    private ContentFacade contentFacade;
    private AmazonS3Client s3Client;
    private User _user; // userJohn
    private User sharedUser; // userAlice
    private Media uploadedMedia;

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public void initTest() throws IOException {
        userFacade = new UserFacade();
        contentFacade = new ContentFacade();
        s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain()).withRegion(Regions.EU_WEST_1);
        this.uploadDataToBucket();
    }

    private void uploadDataToBucket() throws IOException {

        URL url = this.getClass().getResource("../../../../amazon-aws-logo.jpg");
        String tmpFileName = url.getFile();

        File file = new File(tmpFileName);
        String fileName = file.getName();
        InputStream is = url.openStream();
        String contentType = URLConnection.guessContentTypeFromStream(is);

        User userJohn = new User();
        userJohn.setEmail("jon@doe.com");
        userJohn.setUserName("jondoe");
        userJohn.updatePassword("mypassword");

        User _sharedUser = new User();
        _sharedUser.setEmail("alice@doe.com");
        _sharedUser.setUserName("aliceN");
        _sharedUser.updatePassword("mypwd");

        _user = userFacade.register(userJohn);
        sharedUser = userFacade.register(_sharedUser);

        Comment comment = new Comment();
        comment.setText("My comment");
        Set<Comment> comments = new HashSet<>();
        comments.add(comment);

        uploadedMedia = contentFacade.uploadPictureToS3(_user, fileName, is, contentType, comment);
    }

    @Test(priority=4)
    public void testMediaUpload() throws InterruptedException {
        boolean error = false;

        try {
            String s3Filename = null;
            _user = userFacade.findUser(_user.getId());
            
            Assert.assertEquals(_user.getEmail(), "jon@doe.com");
            Assert.assertEquals(_user.getUserName(), "jondoe");
            
            List<Media> media = _user.getMedia();
            
            Assert.assertTrue(media.size() == 1);
            if (media.iterator().hasNext()) {
                Media _media = media.iterator().next();

                URL publicUrl = new URL(_media.getS3Url());

                int cnt = 0;
                while (publicUrl.openConnection().getContentLength() == 0 && cnt < 3) {
                    System.out.println(publicUrl);
                    System.out.println("Content length: " + publicUrl.openConnection().getContentLength());
                    Thread.sleep(50);
                    cnt++;
                }

                Assert.assertTrue(publicUrl.openConnection().getContentLength() > 0);

                s3Filename = _media.getS3FileName();
                Assert.assertNotNull(s3Filename);
                Assert.assertTrue(s3Filename.trim().length() > 0);
                List<Comment> commentSet =_media.getComments();

                if (commentSet.iterator().hasNext()) {
                    Comment _comment = commentSet.iterator().next();
                    Assert.assertEquals(_comment.getText(), "My comment");
                }
            }

            Assert.assertTrue(s3Client.doesObjectExist(ContentHelper.getInstance().getConfiguredBucketName(), s3Filename));

        }

        catch (Exception exc) {
            error = true;
        }

        Assert.assertFalse(error);
    }

    @Test(priority=5)
    public void testMediaDeletion() {
        boolean error = false;
        try {
            List<Long> mediaList = new ArrayList<>();
            String s3Bucket = uploadedMedia.getS3Bucket();
            String s3Filename = uploadedMedia.getS3FileName();

            _logger.info("Deleting file " + s3Filename + " from bucket " + s3Bucket + " with thumbnail " +
                    uploadedMedia.getS3ThumbFileName());

            mediaList.add(uploadedMedia.getId());
            contentFacade.deleteMedia(mediaList);

            // Now we try to access the file after waiting for 5s

            Thread.sleep(5000);

            boolean doesObjectExist = s3Client.doesObjectExist(s3Bucket, s3Filename);
            Assert.assertFalse(doesObjectExist);
        }

        catch (Exception exc) {
            error = true;
        }

        Assert.assertFalse(error);
    }


    @AfterClass
    public void cleanUp() {

        // Delete S3 bucket
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(ContentHelper.getInstance().getConfiguredBucketName());
        s3Client.deleteBucket(deleteBucketRequest);

        EntityManager _em = Persistence.createEntityManager();
        TypedQuery<User> query =_em.createQuery("select u from User u where u.userName=:userName", User.class);

        query.setParameter("userName", _user.getUserName());
        _user = query.getSingleResult();

        query.setParameter("userName", sharedUser.getUserName());
        sharedUser = query.getSingleResult();

        _em.getTransaction().begin();
        _em.remove(_user);
        _em.remove(sharedUser);
        _em.getTransaction().commit();
    }
}
