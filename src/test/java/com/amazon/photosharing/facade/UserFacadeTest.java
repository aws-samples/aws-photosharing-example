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

import com.amazon.photosharing.dao.Role;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.listener.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class UserFacadeTest {

    private UserFacade userFacade;
    private User registeredUser;

    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public void initTest() throws IOException {
        userFacade = new UserFacade();
        _logger.info("Init UserFacadeTest");
    }

    @Test(priority=6)
    public void testRegister() {

        Role role = new Role();
        role.setRole(com.amazon.photosharing.enums.Role.ADMINISTRATOR);

        List<Role> roles = new ArrayList<>();
        roles.add(role);

        User user = new User();
        user.setSalt("salt123".getBytes());
        user.setPassword("password");
        user.setUserName("jondoe");
        user.setEmail("jon@doe.com");
        user.setRoles(roles);

        registeredUser = userFacade.register(user);

        User checkUser = userFacade.findUser(registeredUser.getUserName());

        Assert.assertEquals(registeredUser, checkUser);
    }

    @Test(priority=7)
    public void testFindUser() {

        Assert.assertNotNull(registeredUser);
        User checkUser = userFacade.findUser(registeredUser.getUserName());

        Assert.assertEquals(registeredUser, checkUser);
    }

    @Test(priority=7)
    public void testFindUserByEmail() {

        Assert.assertNotNull(registeredUser);
        User checkUser = userFacade.findUserByEmail(registeredUser.getEmail());

        Assert.assertEquals(registeredUser, checkUser);
    }

    @Test(priority=8)
    public void testLogin() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        boolean isLoggedIn = userFacade.login(registeredUser.getUserName(), registeredUser.getPassword(), mockRequest);
        Assert.assertTrue(isLoggedIn);
    }

    @AfterClass
    public void cleanUp() {
        _logger.info("Clean up after test");

        EntityManager _em = Persistence.createEntityManager();

        TypedQuery<User> query =_em.createQuery("select u from User u where u.userName=:userName", User.class);

        query.setParameter("userName", registeredUser.getUserName());
        User _user = query.getSingleResult();

        _em.getTransaction().begin();
        _em.remove(_user);
        _em.getTransaction().commit();
    }
}
