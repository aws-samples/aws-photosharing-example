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

package com.amazon.photosharing.utils.content;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.InputStream;

public class UploadThread implements Runnable {

    private String p_content_type, p_bucket_name, p_s3_key;
    private long p_size;
    private InputStream p_file_stream;
    private AmazonS3Client s3Client;

    public UploadThread(String p_content_type, long p_size, String p_bucket_name, String p_s3_key,
                        InputStream p_file_stream, AmazonS3Client s3Client) {
        this.p_bucket_name = p_bucket_name;
        this.p_content_type = p_content_type;
        this.p_s3_key = p_s3_key;
        this.p_file_stream = p_file_stream;
        this.s3Client = s3Client;
        this.p_size = p_size;
    }

    @Override
    public void run() {
        ObjectMetadata meta_data = new ObjectMetadata();
        if (p_content_type != null)
            meta_data.setContentType(p_content_type);

        meta_data.setContentLength(p_size);

        PutObjectRequest putObjectRequest = new PutObjectRequest(p_bucket_name, p_s3_key, p_file_stream, meta_data);
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        PutObjectResult res = s3Client.putObject(putObjectRequest);       
    }
}
