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

import com.amazon.photosharing.listener.Persistence;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Locale;

public class DictionaryTest {

    private static final String VALUE = "myText";
    private static final String ID = "idKey";

    private void checkDictionary(Dictionary dictionary) {
        Assert.assertEquals(dictionary.getId().getId(), ID);
        Assert.assertEquals(dictionary.getId().getLocale(), Locale.GERMAN);
        Assert.assertEquals(dictionary.getText(), VALUE);
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            Dictionary dictionary = new Dictionary();
            dictionary.setText(VALUE);

            DictionaryPK pk = new DictionaryPK();
            pk.setId(ID);
            pk.setLocale(Locale.GERMAN);
            dictionary.setId(pk);

            _em.getTransaction().begin();
            _em.persist(dictionary);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select d from Dictionary d where d.text='myText'");
            Dictionary retDictionary = (Dictionary)query.getSingleResult();

            checkDictionary(retDictionary);

            _em.getTransaction().begin();
            _em.remove(retDictionary);
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

            Query query = _em.createQuery("select d from Dictionary d where d.text='myText'");
            Dictionary retDictionary = (Dictionary)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }
}
