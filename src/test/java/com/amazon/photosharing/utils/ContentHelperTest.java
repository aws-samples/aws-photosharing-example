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

package com.amazon.photosharing.utils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ContentHelperTest {

    private ContentHelper contentHelper;
    private AmazonS3Client s3Client;
    private String bucketName;

    @BeforeClass
    private void init() throws Exception {
        contentHelper = ContentHelper.getInstance();
        s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain()).withRegion(Regions.EU_WEST_1);

        long millis = System.currentTimeMillis() % 1000;

        bucketName = "testbucket-" + millis;

        contentHelper.createS3BucketIfNotExists(bucketName);

        Thread.sleep(1000);
    }

    @AfterClass
    private void cleanUp() throws Exception {
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucketName);

        s3Client.deleteBucket(deleteBucketRequest);
    }

    @Test
    public void createS3BucketIfNotExistsTest() throws Exception {
        long millis = System.currentTimeMillis() % 1000;

        String testBucketName = "testbucket-" + millis;

        contentHelper.createS3BucketIfNotExists(testBucketName);
        Thread.sleep(1000);
        boolean doesBucketExist = s3Client.doesBucketExist(testBucketName);

        Assert.assertTrue(doesBucketExist);

        s3Client.deleteBucket(testBucketName);
    }

    @Test
    public void contentTest() throws Exception {

        URL url = this.getClass().getResource("../../../../amazon-aws-logo.jpg");
        String tmpFileName = url.getFile();

        File file = new File(tmpFileName);
        String fileName = file.getName();
        InputStream is = url.openStream();
        String contentType = URLConnection.guessContentTypeFromStream(is);

        contentHelper.uploadContent(contentType, file.length(), bucketName, fileName, is);

        Thread.sleep(500);
        boolean doesObjectExist = s3Client.doesObjectExist(bucketName, fileName);
        Assert.assertTrue(doesObjectExist);

        S3ObjectInputStream inputStream = contentHelper.downloadContent(bucketName, fileName);
        Assert.assertNotNull(inputStream);

        contentHelper.deleteContent(bucketName, fileName);
        Thread.sleep(500);

        doesObjectExist = s3Client.doesObjectExist(bucketName, fileName);
        Assert.assertFalse(doesObjectExist);
    }

}
