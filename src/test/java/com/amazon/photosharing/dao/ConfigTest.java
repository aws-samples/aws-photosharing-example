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

package com.amazon.photosharing.dao;

import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.listener.Persistence;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ConfigTest {

    private static final String VALUE = "testS3Key";

    private void checkConfig(Config config) {
        Assert.assertEquals(config.getKey().toString(), Configuration.FILE_NAME.toString());
        Assert.assertEquals(config.getValue(), VALUE);
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            Config config = new Config();
            config.setKey(Configuration.FILE_NAME);
            config.setValue(VALUE);

            _em.getTransaction().begin();
            _em.persist(config);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select c from Config c where c.config_key='FILE_NAME'");
            Config retConfig = (Config)query.getSingleResult();

            checkConfig(retConfig);

            _em.getTransaction().begin();
            _em.remove(retConfig);
            _em.getTransaction().commit();
        }

        catch (Exception exc) {
            error = true;
            exc.printStackTrace();
        }

        Assert.assertFalse(error);

        error = false;
        try {
            _em = Persistence.createEntityManager();

            Query query = _em.createQuery("select c from Config c where c.config_key='FILE_NAME'");
            Config retConfig = (Config)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }
}
