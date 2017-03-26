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

package com.amazon.photosharing.enums;

public enum Configuration {
	
	S3_BUCKET_FORMAT("photosharing"),
	S3_REGION("eu-west-1"),
	IMG_THUMB_MAX_HEIGHT("400"),
	IMG_THUMB_MAX_WIDTH("400"),
	IMG_THUMB_PRESIGNED_TIMEOUT("300000"), //5 mins
	IMG_FULL_PRESIGNED_TIMEOUT("60000"), //1 mins
    FILE_NAME("filename"),
    S3_KEY("s3_key"),
    MIME_TYPE("mimetype"),
    PRIVATE_MEDIA_URL_FORMAT("/private/media/{0}"),
    PRIVATE_MEDIA_URL_FORMAT_THUMB("/private/media/{0}?format=thumb"),
	SHARE_ALBUM_PUBLIC_URL_FORMAT("/share/album.xhtml?q={0}"),	
	SHARE_MEDIA_PUBLIC_URL_FORMAT("/share/media.xhtml?q={0}"),
	ELASTICACHE_CONFIG_ENDPOINT("photosharing.oytu1w.cfg.euw1.cache.amazonaws.com");
	
	private final String value;

	Configuration(String p_value) {value = p_value;}
	public String toString() {return this.value;}
		
}
