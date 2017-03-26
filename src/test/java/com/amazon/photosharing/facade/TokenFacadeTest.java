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

package com.amazon.photosharing.facade;

import com.amazon.photosharing.dao.Token;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;

public class TokenFacadeTest {

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());
    private TokenFacade tokenFacade;
    private Token token, testToken, valToken;

    private static final String USER_NAME = "johndoe";

    private EntityManager _em = Persistence.createEntityManager();

    private Token createToken(String userName) {
        Token tmpToken = new Token();
        tmpToken.setToken(TokenGenerator.getInstance().issueToken(userName));
        tmpToken.setUserName(userName);

        return tmpToken;
    }

    @BeforeClass
    public void initTest() throws IOException {
        tokenFacade = new TokenFacade();
        _logger.info("Init TokenFacadeTest");
        this.token = createToken(USER_NAME);
        this.valToken = createToken("validateToken");
        this.testToken = createToken("testToken");
    }

    @Test
    public void testStoreToken() {
        tokenFacade.storeToken(USER_NAME, token.getToken());

        Query query = _em.createQuery("select t from Token t where t.userName='" + USER_NAME + "'");
        Token retToken = (Token)query.getSingleResult();

        Assert.assertEquals(retToken.getToken(), token.getToken());
    }

    @Test
    public void testFindToken() {

        _em.getTransaction().begin();
        _em.persist(testToken);
        _em.getTransaction().commit();

        testToken = tokenFacade.findToken("testToken");

        Assert.assertNotNull(testToken);
    }

    @Test
    public void testValidateToken() {

        String uName = "validateToken";
        tokenFacade.storeToken(uName, valToken.getToken());

        Token retToken = tokenFacade.findToken(uName);
        boolean isValid = tokenFacade.validateToken(retToken.getToken());

        Assert.assertTrue(isValid);
        Assert.assertNotNull(retToken);

        valToken = retToken;
    }

    @Test
    public void testValidateWrongToken() {
        String token = "qwkdjqwdj";
        boolean isValid = tokenFacade.validateToken(token);
        Assert.assertFalse(isValid);
    }

    @AfterClass
    public void cleanUp() {

        Query query = _em.createQuery("select t from Token t where t.userName='testToken'");
        testToken = (Token)query.getSingleResult();

        query = _em.createQuery("select t from Token t where t.userName='" + USER_NAME + "'");
        token = (Token)query.getSingleResult();

        _em.getTransaction().begin();
        _em.remove(token);
        _em.remove(testToken);
        _em.getTransaction().commit();

        query = _em.createQuery("select t from Token t where t.id='" + valToken.getId() + "'");
        valToken = (Token)query.getSingleResult();

        _em.getTransaction().begin();
        _em.remove(valToken);
        _em.getTransaction().commit();
    }

}
