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
import com.amazon.photosharing.utils.TokenGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.concurrent.TimeUnit;

public class TokenTest {

    private String tokenString;

    private void checkToken(Token token) throws InterruptedException {
        Assert.assertEquals(tokenString, token.getToken());
        Assert.assertTrue(TokenGenerator.getInstance().validateToken(token.getToken()));
        Thread.sleep(2000);
        Assert.assertFalse(TokenGenerator.getInstance().validateToken(token.getToken(), 1, TimeUnit.SECONDS));
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            Token token = new Token();
            tokenString = TokenGenerator.getInstance().issueToken("jondoe");
            token.setUserName("jondoe");
            token.setToken(tokenString);

            _em.getTransaction().begin();
            _em.persist(token);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select t from Token t where t.userName='jondoe'");
            Token retToken = (Token)query.getSingleResult();

            this.checkToken(retToken);

            _em.getTransaction().begin();
            _em.remove(retToken);
            _em.getTransaction().commit();

        } catch (Exception exc) {
            exc.printStackTrace();
            error = true;
        }

        Assert.assertFalse(error);

        error = false;
        try {
            _em = Persistence.createEntityManager();

            Query query = _em.createQuery("select t from Token t where t.userName='jondoe'");
            Token retToken = (Token)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }

}
