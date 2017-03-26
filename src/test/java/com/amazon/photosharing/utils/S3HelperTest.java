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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class S3HelperTest {

    @Test
    public void testExtractUserFromKey() {

        String testKey = "1e9f-2016-04-25-11-09-27-353/jondoe/firehose.jpg";

        String userName = S3Helper.extractUserFromKey(testKey);

        Assert.assertNotNull(userName);
        Assert.assertEquals(userName, "jondoe");

        userName = S3Helper.extractUserFromKey("sfsc" + testKey);
        Assert.assertNotNull(userName);
        Assert.assertEquals(userName, "");
    }

    @Test
    public void testCreateS3Key()  {

        try {
            String filename = "myFile";
            String username = "myUserName";

            SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
            String dateInString = "01-01-2013";
            Date date = formatter.parse(dateInString);

            String s3KeyName = S3Helper.createS3Key(filename, username, date);

            Assert.assertEquals(s3KeyName, "1426-2013-01-01-12-01-00-000/myUserName/myFile");
        }

        catch (ParseException exc) {
            Assert.assertTrue(false);
        }
    }
}
