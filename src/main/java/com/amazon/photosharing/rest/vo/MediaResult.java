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

import com.amazon.photosharing.dao.Media;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MediaResult {

    private String name;
    private String s3Bucket;
    private String s3FileName;
    private String s3ThumbFileName;

    public MediaResult(Media media) {

        if (media != null) {
            this.name = media.getName();
            this.s3Bucket = media.getS3Bucket();
            this.s3FileName = media.getS3FileName();
            this.s3ThumbFileName = media.getS3ThumbFileName();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3FileName() {
        return s3FileName;
    }

    public void setS3FileName(String s3FileName) {
        this.s3FileName = s3FileName;
    }

    public String getS3ThumbFileName() {
        return s3ThumbFileName;
    }

    public void setS3ThumbFileName(String s3ThumbFileName) {
        this.s3ThumbFileName = s3ThumbFileName;
    }
}
