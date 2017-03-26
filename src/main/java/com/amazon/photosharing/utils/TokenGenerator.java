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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
public final class TokenGenerator {

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

    private static final TokenGenerator tokenGenerator = new TokenGenerator();

    private final DateFormat format = new SimpleDateFormat("ddMMyyHHmmss");
    private final StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();

    private final int LOGIN_TIME = 30;

    private TokenGenerator() {
        jasypt.setPassword("tokenpassword");
    }

    public static TokenGenerator getInstance() {
        return tokenGenerator;
    }

    public String issueToken(String userName) {

        Date creationDate = new Date();

        String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + userName +
                "|" + format.format(creationDate);

        // this is the authentication token user will send in order to use the web service
        String authenticationToken = jasypt.encrypt(key);

        return authenticationToken;
    }

    public String getUsernameFromToken(String token) {
        try {
            String key = jasypt.decrypt(token);

            String [] keyParts = key.split("\\|");

            for (String part : keyParts)
                _logger.info(part);

            return keyParts[1];

        }

        catch (Exception exc) {
            _logger.info("Could not extract username from token: " + token);

            return null;
        }
    }

    public boolean validateToken(String token, Integer loginTime, TimeUnit timeUnit) {
        try {
            String key = jasypt.decrypt(token);

            String [] keyParts = key.split("\\|");
            String strDate = keyParts[keyParts.length - 1];
            Date date = format.parse(strDate);

            Date currentTime = new Date();

            long duration = currentTime.getTime() - date.getTime();
            long diff;

            if (timeUnit.equals(TimeUnit.MINUTES))
                diff = TimeUnit.MILLISECONDS.toMinutes(duration);
            else if (timeUnit.equals(TimeUnit.SECONDS))
                diff = TimeUnit.MILLISECONDS.toSeconds(duration);
            else
                throw new UnsupportedOperationException(timeUnit.toString() + " not supported");

            if (diff > loginTime)
                return false;

            return true;
        }

        catch (Exception exc) {
            exc.printStackTrace();

            return false;
        }
    }

    public boolean validateToken(String token) {
        return this.validateToken(token, LOGIN_TIME, TimeUnit.MINUTES);
    }
}
