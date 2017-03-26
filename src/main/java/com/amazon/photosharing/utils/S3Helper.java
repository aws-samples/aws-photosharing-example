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

import org.jboss.logging.Logger;

import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.amazon.photosharing.utils.Security.SHA1;

public final class S3Helper {

    private static final Logger _logger = Logger.getLogger(S3Helper.class);

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS");

    public static String extractUserFromKey(String s3Key) {
        String userName;

        // key has the following pattern
        // 4digit-yyyy-MM-dd-hh-mm-ss-SSS/username/image

        String [] keyParts = s3Key.split("/");

        if (keyParts.length < 3)
            return "";

        userName = keyParts[1];

        ParsePosition position = new ParsePosition(5);
        Date date = FORMATTER.parse(keyParts[0], position);

        if (date == null)
            return "";

        return userName;
    }

    public static String createS3Key(String _fileName, String _userName, Date _date) {

        String path = "";

        try {

            Date date;
            if (null == _date) date = new Date();
            else date = _date;

            String datePath = FORMATTER.format(date);
            String sha1File = SHA1(datePath).substring(0, 4);

            path = MessageFormat.format("{0}-{1}/{2}/{3}", sha1File, datePath, _userName, _fileName);
        }

        catch (NoSuchAlgorithmException exc) {
            _logger.error(exc);
        }

        return path;
    }


}
