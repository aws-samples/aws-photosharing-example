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

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.facade.ConfigFacade;
import com.amazon.photosharing.utils.content.UploadThread;
import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public final class ContentHelper {

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

    private static final ContentHelper CONTENT_HELPER = new ContentHelper();
    private static final int NTHREDS = 10;

    private AmazonS3Client s3Client;
    private ExecutorService executor;

    private ContentHelper() {
        this.initS3Client();
        this.initExecutorService();
    }

    public static ContentHelper getInstance() {
        return CONTENT_HELPER;
    }

    private void initExecutorService() {
        executor = Executors.newFixedThreadPool(NTHREDS);
    }

    private void initS3Client() {
            s3Client = new AmazonS3Client(
                    new DefaultAWSCredentialsProviderChain()).withRegion(
                    Regions.fromName(
                            ConfigFacade.get(Configuration.S3_REGION)));
    }

    public synchronized String getConfiguredBucketName() {
    	return ConfigFacade.get(Configuration.S3_BUCKET_FORMAT);
    }

    public synchronized void createS3BucketIfNotExists(String p_bucket_name) {
        _logger.debug("Searching for bucket " + p_bucket_name);
        if (!s3Client.doesBucketExist(p_bucket_name)) {
            Bucket bucket = s3Client.createBucket(p_bucket_name);
           _logger.info("Created bucket: " + bucket.getName());
        } else {
            _logger.debug("Bucket detected. Verifying permissions.");
            try {
                s3Client.getBucketAcl(p_bucket_name);
            } catch (AmazonClientException ex) {
                _logger.warn("Permission check failed. Randomizing.");
                ConfigFacade.set(Configuration.S3_BUCKET_FORMAT, p_bucket_name + "-" + Security.getRandomHash(8));
                _logger.debug("Reiterating with: " + p_bucket_name);
                createS3BucketIfNotExists(getConfiguredBucketName());
            }
        }
    }

    public void uploadContent(String p_content_type, long p_size, String p_bucket_name, String p_s3_key, InputStream p_file_stream) {
        UploadThread uploadThread = new UploadThread(p_content_type, p_size, p_bucket_name, p_s3_key, p_file_stream, s3Client);
        executor.submit(uploadThread);
    }
    
    public void deleteContent(String p_bucket_name, String p_s3_key) throws AmazonClientException {
        if (s3Client.doesBucketExist(p_bucket_name)) {
            DeleteObjectRequest delete_req = new DeleteObjectRequest(p_bucket_name, p_s3_key);
            s3Client.deleteObject(delete_req);
        }
    }


    public S3ObjectInputStream downloadContent(String p_bucket_name, String p_s3_key) {

        _logger.debug("Downloading file " + p_s3_key + " from bucket " + p_bucket_name);

        S3ObjectInputStream stream = null;

        if (s3Client.doesObjectExist(p_bucket_name, p_s3_key)) {
            S3Object object = s3Client.getObject(p_bucket_name, p_s3_key);
            stream = object.getObjectContent();
        }

        return stream;
    }
    
    public URL getSignedUrl(String p_s3_bucket, String p_s3_file, Date p_exires) {    	   
    	
    	GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(p_s3_bucket, p_s3_file);
		generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
		generatePresignedUrlRequest.setExpiration(p_exires);
        
		return s3Client.generatePresignedUrl(generatePresignedUrlRequest); 
    }
}
